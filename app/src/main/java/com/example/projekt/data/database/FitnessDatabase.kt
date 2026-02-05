package com.example.projekt.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projekt.data.database.dao.ActivityDao
import com.example.projekt.data.database.dao.PhotoDao
import com.example.projekt.data.database.dao.RoutePointDao
import com.example.projekt.data.database.dao.StepDao
import com.example.projekt.data.database.entity.ActivityEntity
import com.example.projekt.data.database.entity.DailyStepsEntity
import com.example.projekt.data.database.entity.PhotoEntity
import com.example.projekt.data.database.entity.RoutePointEntity

@Database(
    entities = [
        ActivityEntity::class,
        DailyStepsEntity::class,
        RoutePointEntity::class,
        PhotoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun stepDao(): StepDao
    abstract fun routePointDao(): RoutePointDao
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
