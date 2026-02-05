package com.example.projekt.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class DailyStepsEntity(
    @PrimaryKey
    val date: String, // Format: yyyy-MM-dd
    val steps: Int = 0,
    val goal: Int = 10000,
    val lastUpdated: Long = System.currentTimeMillis()
)
