package com.example.projekt.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val totalSteps: Int = 0,
    val distanceMeters: Float = 0f,
    val durationSeconds: Long = 0,
    val averagePaceSecondsPerKm: Float? = null,
    val isActive: Boolean = true
)
