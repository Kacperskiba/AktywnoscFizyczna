package com.example.projekt.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        const val DEFAULT_STEP_GOAL = 10000
    }

    val dailyStepGoal: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_STEP_GOAL] ?: DEFAULT_STEP_GOAL
    }

    suspend fun setDailyStepGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_STEP_GOAL] = goal
        }
    }
}
