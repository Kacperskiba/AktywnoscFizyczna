package com.example.projekt.data.database.dao

import androidx.room.*
import com.example.projekt.data.database.entity.DailyStepsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dailySteps: DailyStepsEntity)

    @Query("SELECT * FROM daily_steps WHERE date = :date")
    suspend fun getByDate(date: String): DailyStepsEntity?

    @Query("SELECT * FROM daily_steps WHERE date = :date")
    fun getByDateFlow(date: String): Flow<DailyStepsEntity?>

    @Query("SELECT * FROM daily_steps ORDER BY date DESC LIMIT :days")
    fun getLastDays(days: Int): Flow<List<DailyStepsEntity>>

    @Query("SELECT * FROM daily_steps WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getStepsBetweenDates(startDate: String, endDate: String): Flow<List<DailyStepsEntity>>

    @Query("UPDATE daily_steps SET steps = steps + :stepsToAdd, lastUpdated = :timestamp WHERE date = :date")
    suspend fun addSteps(date: String, stepsToAdd: Int, timestamp: Long)

    @Query("SELECT SUM(steps) FROM daily_steps WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalStepsBetweenDates(startDate: String, endDate: String): Int?

    @Query("SELECT AVG(steps) FROM daily_steps WHERE date >= :startDate AND date <= :endDate")
    suspend fun getAverageStepsBetweenDates(startDate: String, endDate: String): Float?
}
