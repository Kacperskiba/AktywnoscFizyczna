package com.example.projekt.data.repository

import com.example.projekt.data.database.dao.ActivityDao
import com.example.projekt.data.database.dao.PhotoDao
import com.example.projekt.data.database.dao.RoutePointDao
import com.example.projekt.data.database.entity.ActivityEntity
import com.example.projekt.data.database.entity.PhotoEntity
import com.example.projekt.data.database.entity.RoutePointEntity
import kotlinx.coroutines.flow.Flow

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val routePointDao: RoutePointDao,
    private val photoDao: PhotoDao
) {
    // Activity operations
    suspend fun startNewActivity(): Long {
        val activity = ActivityEntity(
            startTime = System.currentTimeMillis(),
            isActive = true
        )
        return activityDao.insert(activity)
    }

    suspend fun finishActivity(
        activityId: Long,
        totalSteps: Int,
        distanceMeters: Float,
        durationSeconds: Long
    ) {
        val pace = if (distanceMeters > 0) {
            (durationSeconds / (distanceMeters / 1000f))
        } else null

        activityDao.finishActivity(
            id = activityId,
            endTime = System.currentTimeMillis(),
            steps = totalSteps,
            distance = distanceMeters,
            duration = durationSeconds,
            pace = pace
        )
    }

    suspend fun getActiveActivity(): ActivityEntity? = activityDao.getActiveActivity()

    fun getActiveActivityFlow(): Flow<ActivityEntity?> = activityDao.getActiveActivityFlow()

    suspend fun getActivityById(id: Long): ActivityEntity? = activityDao.getById(id)

    fun getActivityByIdFlow(id: Long): Flow<ActivityEntity?> = activityDao.getByIdFlow(id)

    fun getAllActivities(): Flow<List<ActivityEntity>> = activityDao.getAllActivities()

    fun getRecentActivities(limit: Int = 10): Flow<List<ActivityEntity>> =
        activityDao.getRecentActivities(limit)

    suspend fun deleteActivity(activity: ActivityEntity) = activityDao.delete(activity)

    // Route points operations
    suspend fun addRoutePoint(
        activityId: Long,
        latitude: Double,
        longitude: Double,
        altitude: Double? = null,
        speed: Float? = null
    ): Long {
        val routePoint = RoutePointEntity(
            activityId = activityId,
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            speed = speed,
            timestamp = System.currentTimeMillis()
        )
        return routePointDao.insert(routePoint)
    }

    fun getRoutePointsForActivity(activityId: Long): Flow<List<RoutePointEntity>> =
        routePointDao.getPointsForActivity(activityId)

    suspend fun getRoutePointsForActivitySync(activityId: Long): List<RoutePointEntity> =
        routePointDao.getPointsForActivitySync(activityId)

    suspend fun getLastRoutePoint(activityId: Long): RoutePointEntity? =
        routePointDao.getLastPointForActivity(activityId)

    // Photo operations
    suspend fun addPhoto(
        activityId: Long?,
        filePath: String,
        latitude: Double? = null,
        longitude: Double? = null
    ): Long {
        val photo = PhotoEntity(
            activityId = activityId,
            filePath = filePath,
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude
        )
        return photoDao.insert(photo)
    }

    fun getPhotosForActivity(activityId: Long): Flow<List<PhotoEntity>> =
        photoDao.getPhotosForActivity(activityId)

    fun getAllPhotos(): Flow<List<PhotoEntity>> = photoDao.getAllPhotos()

    fun getRecentPhotos(limit: Int = 20): Flow<List<PhotoEntity>> = photoDao.getRecentPhotos(limit)

    suspend fun deletePhoto(photo: PhotoEntity) = photoDao.delete(photo)
}
