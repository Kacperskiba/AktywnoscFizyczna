package com.example.projekt.data.database.dao

import androidx.room.*
import com.example.projekt.data.database.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insert(activity: ActivityEntity): Long

    @Update
    suspend fun update(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getById(id: Long): ActivityEntity?

    @Query("SELECT * FROM activities WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveActivity(): ActivityEntity?

    @Query("SELECT * FROM activities WHERE isActive = 1 LIMIT 1")
    fun getActiveActivityFlow(): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities ORDER BY startTime DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime DESC")
    fun getActivitiesForDay(startOfDay: Long, endOfDay: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities ORDER BY startTime DESC LIMIT :limit")
    fun getRecentActivities(limit: Int): Flow<List<ActivityEntity>>

    @Query("UPDATE activities SET isActive = 0, endTime = :endTime, totalSteps = :steps, distanceMeters = :distance, durationSeconds = :duration, averagePaceSecondsPerKm = :pace WHERE id = :id")
    suspend fun finishActivity(id: Long, endTime: Long, steps: Int, distance: Float, duration: Long, pace: Float?)
}
