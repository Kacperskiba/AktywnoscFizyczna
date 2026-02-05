package com.example.projekt.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.data.database.FitnessDatabase
import com.example.projekt.data.database.entity.ActivityEntity
import com.example.projekt.data.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val activities: List<ActivityEntity> = emptyList(),
    val totalActivities: Int = 0,
    val totalSteps: Int = 0,
    val totalDistance: Float = 0f,
    val totalDuration: Long = 0
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FitnessDatabase.getDatabase(application)
    private val activityRepository = ActivityRepository(
        database.activityDao(),
        database.routePointDao(),
        database.photoDao()
    )

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            activityRepository.getAllActivities().collect { activities ->
                val completedActivities = activities.filter { !it.isActive }
                val totalSteps = completedActivities.sumOf { it.totalSteps }
                val totalDistance = completedActivities.sumOf { it.distanceMeters.toDouble() }.toFloat()
                val totalDuration = completedActivities.sumOf { it.durationSeconds }

                _uiState.update {
                    it.copy(
                        activities = completedActivities,
                        totalActivities = completedActivities.size,
                        totalSteps = totalSteps,
                        totalDistance = totalDistance,
                        totalDuration = totalDuration
                    )
                }
            }
        }
    }

    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            activityRepository.deleteActivity(activity)
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
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%dh %02dmin", hours, minutes)
        } else {
            String.format("%dmin %02ds", minutes, secs)
        }
    }

    fun formatPace(paceSecondsPerKm: Float?): String {
        if (paceSecondsPerKm == null || paceSecondsPerKm <= 0) return "--:--"
        val minutes = (paceSecondsPerKm / 60).toInt()
        val seconds = (paceSecondsPerKm % 60).toInt()
        return String.format("%d:%02d min/km", minutes, seconds)
    }
}
