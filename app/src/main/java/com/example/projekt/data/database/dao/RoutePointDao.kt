package com.example.projekt.data.database.dao

import androidx.room.*
import com.example.projekt.data.database.entity.RoutePointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePointDao {
    @Insert
    suspend fun insert(routePoint: RoutePointEntity): Long

    @Insert
    suspend fun insertAll(routePoints: List<RoutePointEntity>)

    @Delete
    suspend fun delete(routePoint: RoutePointEntity)

    @Query("SELECT * FROM route_points WHERE activityId = :activityId ORDER BY timestamp ASC")
    fun getPointsForActivity(activityId: Long): Flow<List<RoutePointEntity>>

    @Query("SELECT * FROM route_points WHERE activityId = :activityId ORDER BY timestamp ASC")
    suspend fun getPointsForActivitySync(activityId: Long): List<RoutePointEntity>

    @Query("SELECT * FROM route_points WHERE activityId = :activityId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPointForActivity(activityId: Long): RoutePointEntity?

    @Query("DELETE FROM route_points WHERE activityId = :activityId")
    suspend fun deletePointsForActivity(activityId: Long)

    @Query("SELECT COUNT(*) FROM route_points WHERE activityId = :activityId")
    suspend fun getPointCountForActivity(activityId: Long): Int
}
