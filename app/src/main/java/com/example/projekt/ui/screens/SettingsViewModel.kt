package com.example.projekt.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.data.preferences.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val dailyStepGoal: Int = 10000,
    val isSaving: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            userPreferences.dailyStepGoal.collect { goal ->
                _uiState.update { it.copy(dailyStepGoal = goal) }
            }
        }
    }

    fun updateDailyStepGoal(goal: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            userPreferences.setDailyStepGoal(goal)
            _uiState.update { it.copy(dailyStepGoal = goal, isSaving = false) }
        }
    }
}
