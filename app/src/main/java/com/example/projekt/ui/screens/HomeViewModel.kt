package com.example.projekt.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.data.database.FitnessDatabase
import com.example.projekt.data.database.entity.ActivityEntity
import com.example.projekt.data.database.entity.DailyStepsEntity
import com.example.projekt.data.preferences.UserPreferences
import com.example.projekt.data.repository.ActivityRepository
import com.example.projekt.data.repository.StepsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val todaySteps: Int = 0,
    val dailyGoal: Int = 10000,
    val dailyProgress: Float = 0f,
    val weeklySteps: List<DailyStepsEntity> = emptyList(),
    val weeklyTotal: Int = 0,
    val weeklyAverage: Float = 0f,
    val recentActivities: List<ActivityEntity> = emptyList(),
    val totalActivities: Int = 0,
    val totalDistance: Float = 0f
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FitnessDatabase.getDatabase(application)
    private val activityRepository = ActivityRepository(
        database.activityDao(),
        database.routePointDao(),
        database.photoDao()
    )
    private val stepsRepository = StepsRepository(database.stepDao())
    private val userPreferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Load daily goal from preferences
        viewModelScope.launch {
            userPreferences.dailyStepGoal.collect { goal ->
                _uiState.update { it.copy(dailyGoal = goal) }
            }
        }

        // Load today's steps
        viewModelScope.launch {
            stepsRepository.getTodayStepsFlow().collect { dailySteps ->
                dailySteps?.let { data ->
                    val progress = if (data.goal > 0) {
                        (data.steps.toFloat() / data.goal).coerceIn(0f, 1f)
                    } else 0f

                    _uiState.update {
                        it.copy(
                            todaySteps = data.steps,
                            dailyProgress = progress
                        )
                    }
                }
            }
        }

        // Load weekly steps
        viewModelScope.launch {
            stepsRepository.getStepsForWeek().collect { weeklySteps ->
                val total = weeklySteps.sumOf { it.steps }
                val average = if (weeklySteps.isNotEmpty()) {
                    total.toFloat() / weeklySteps.size
                } else 0f

                _uiState.update {
                    it.copy(
                        weeklySteps = weeklySteps,
                        weeklyTotal = total,
                        weeklyAverage = average
                    )
                }
            }
        }

        // Load recent activities
        viewModelScope.launch {
            activityRepository.getRecentActivities(5).collect { activities ->
                val totalDistance = activities.sumOf { it.distanceMeters.toDouble() }.toFloat()
                _uiState.update {
                    it.copy(
                        recentActivities = activities,
                        totalActivities = activities.size,
                        totalDistance = totalDistance
                    )
                }
            }
        }
    }

    fun formatDistance(meters: Float): String {
        return if (meters >= 1000) {
            String.format("%.2f km", meters / 1000)
        } else {
            String.format("%.0f m", meters)
        }
    }

    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return if (hours > 0) {
            "${hours}h ${minutes}min"
        } else {
            "${minutes}min"
        }
    }
}
