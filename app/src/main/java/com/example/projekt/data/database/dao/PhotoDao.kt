package com.example.projekt.data.database.dao

import androidx.room.*
import com.example.projekt.data.database.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Delete
    suspend fun delete(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getById(id: Long): PhotoEntity?

    @Query("SELECT * FROM photos WHERE activityId = :activityId ORDER BY timestamp DESC")
    fun getPhotosForActivity(activityId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPhotos(limit: Int): Flow<List<PhotoEntity>>

    @Query("SELECT COUNT(*) FROM photos WHERE activityId = :activityId")
    suspend fun getPhotoCountForActivity(activityId: Long): Int

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
