package com.example.projekt

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.data.database.FitnessDatabase
import com.example.projekt.data.preferences.UserPreferences
import com.example.projekt.data.repository.ActivityRepository
import com.example.projekt.data.repository.StepsRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class ActivityUiState(
    val steps: Int = 0,
    val sessionSteps: Int = 0,
    val distanceMeters: Float = 0f,
    val isTracking: Boolean = false,
    val durationSeconds: Long = 0,
    val paceSecondsPerKm: Float? = null,
    val photos: List<Uri> = emptyList(),
    val dailyGoal: Int = 10000,
    val dailyProgress: Float = 0f,
    val currentActivityId: Long? = null,
    val chartData: List<Float> = emptyList()
)

class ActivityViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState = _uiState.asStateFlow()

    private val database = FitnessDatabase.getDatabase(application)
    private val activityRepository = ActivityRepository(
        database.activityDao(),
        database.routePointDao(),
        database.photoDao()
    )
    private val stepsRepository = StepsRepository(database.stepDao())
    private val userPreferences = UserPreferences(application)

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private var initialStepCount: Int? = null
    private var lastLocation: Location? = null
    private var trackingStartTime: Long = 0
    private var timerJob: Job? = null

    // Minimalna dokładność GPS w metrach (ignoruj punkty o gorszej dokładności)
    private val minAccuracyMeters = 20f
    // Minimalny dystans między punktami (filtruj szum GPS)
    private val minDistanceMeters = 2f

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (!_uiState.value.isTracking) return

            for (location in result.locations) {
                // Filtruj punkty o niskiej dokładności
                if (location.hasAccuracy() && location.accuracy > minAccuracyMeters) {
                    continue
                }

                viewModelScope.launch {
                    val activityId = _uiState.value.currentActivityId ?: return@launch

                    // Calculate distance from last point
                    val lastLoc = lastLocation
                    if (lastLoc != null) {
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            lastLoc.latitude, lastLoc.longitude,
                            location.latitude, location.longitude,
                            results
                        )
                        val distanceDelta = results[0]

                        // Filtruj bardzo małe ruchy (szum GPS)
                        if (distanceDelta >= minDistanceMeters) {
                            _uiState.update { state ->
                                val newDistance = state.distanceMeters + distanceDelta
                                state.copy(distanceMeters = newDistance)
                            }

                            // Save route point only for significant movement
                            activityRepository.addRoutePoint(
                                activityId = activityId,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                altitude = if (location.hasAltitude()) location.altitude else null,
                                speed = if (location.hasSpeed()) location.speed else null
                            )

                            lastLocation = location
                        }
                    } else {
                        // Pierwszy punkt - zapisz bez obliczania dystansu
                        activityRepository.addRoutePoint(
                            activityId = activityId,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            altitude = if (location.hasAltitude()) location.altitude else null,
                            speed = if (location.hasSpeed()) location.speed else null
                        )
                        lastLocation = location
                    }
                }
            }
        }
    }

    init {
        loadPreferences()
        loadTodayData()
        loadChartData()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferences.dailyStepGoal.collect { goal ->
                _uiState.update { it.copy(dailyGoal = goal) }
            }
        }
    }

    private fun loadTodayData() {
        viewModelScope.launch {
            stepsRepository.getTodayStepsFlow().collect { dailySteps ->
                dailySteps?.let { data ->
                    val progress = if (data.goal > 0) {
                        (data.steps.toFloat() / data.goal).coerceIn(0f, 1f)
                    } else 0f

                    _uiState.update {
                        it.copy(
                            steps = data.steps,
                            dailyGoal = data.goal,
                            dailyProgress = progress
                        )
                    }
                }
            }
        }
    }

    private fun loadChartData() {
        viewModelScope.launch {
            stepsRepository.getStepsForWeek().collect { weeklySteps ->
                val chartData = weeklySteps.map { it.steps.toFloat() }
                _uiState.update { it.copy(chartData = chartData) }
            }
        }
    }

    fun toggleTracking(hasPermissions: Boolean) {
        if (!hasPermissions) return

        viewModelScope.launch {
            val isNowTracking = !_uiState.value.isTracking

            if (isNowTracking) {
                startTracking()
            } else {
                stopTracking()
            }
        }
    }

    private suspend fun startTracking() {
        val activityId = activityRepository.startNewActivity()
        trackingStartTime = System.currentTimeMillis()
        initialStepCount = null
        lastLocation = null

        _uiState.update {
            it.copy(
                isTracking = true,
                currentActivityId = activityId,
                sessionSteps = 0,
                distanceMeters = 0f,
                durationSeconds = 0,
                paceSecondsPerKm = null,
                photos = emptyList()
            )
        }

        startSensors()
        startTimer()
    }

    private suspend fun stopTracking() {
        stopTimer()
        stopSensors()

        _uiState.value.currentActivityId?.let { activityId ->
            val state = _uiState.value
            activityRepository.finishActivity(
                activityId = activityId,
                totalSteps = state.sessionSteps,
                distanceMeters = state.distanceMeters,
                durationSeconds = state.durationSeconds
            )
        }

        _uiState.update {
            it.copy(
                isTracking = false,
                currentActivityId = null
            )
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L) // Aktualizuj co sekundę
                if (_uiState.value.isTracking) {
                    val elapsedSeconds = (System.currentTimeMillis() - trackingStartTime) / 1000
                    val distance = _uiState.value.distanceMeters

                    // Calculate pace (seconds per km)
                    val pace = if (distance > 0 && elapsedSeconds > 0) {
                        (elapsedSeconds.toFloat() / (distance / 1000f))
                    } else null

                    _uiState.update {
                        it.copy(
                            durationSeconds = elapsedSeconds,
                            paceSecondsPerKm = pace
                        )
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    @SuppressLint("MissingPermission")
    private fun startSensors() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // GPS request - częstsze aktualizacje dla lepszej dokładności
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1000L) // Nie częściej niż co 1 sekundę
            .setMinUpdateDistanceMeters(2f) // Minimum 2 metry ruchu
            .build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun stopSensors() {
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && _uiState.value.isTracking) {
            val totalSteps = event.values[0].toInt()

            if (initialStepCount == null) {
                initialStepCount = totalSteps
            }

            val sessionSteps = totalSteps - (initialStepCount ?: totalSteps)

            viewModelScope.launch {
                // Update session steps
                _uiState.update { it.copy(sessionSteps = sessionSteps) }

                // Update daily steps in database
                val currentDailySteps = _uiState.value.steps
                val newDailySteps = currentDailySteps + 1
                stepsRepository.setTodaySteps(newDailySteps, _uiState.value.dailyGoal)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun addPhoto(uri: Uri) {
        viewModelScope.launch {
            val activityId = _uiState.value.currentActivityId

            activityRepository.addPhoto(
                activityId = activityId,
                filePath = uri.toString()
            )

            _uiState.update { it.copy(photos = it.photos + uri) }
        }
    }

    fun formatPace(paceSecondsPerKm: Float?): String {
        if (paceSecondsPerKm == null || paceSecondsPerKm <= 0 || paceSecondsPerKm > 3600) return "--:--"
        val minutes = (paceSecondsPerKm / 60).toInt()
        val seconds = (paceSecondsPerKm % 60).toInt()
        return String.format("%d:%02d min/km", minutes, seconds)
    }

    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        stopSensors()
    }
}
