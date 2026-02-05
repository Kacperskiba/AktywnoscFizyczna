package com.example.projekt.data.repository

import com.example.projekt.data.database.dao.StepDao
import com.example.projekt.data.database.entity.DailyStepsEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class StepsRepository(private val stepDao: StepDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDateString(): String = dateFormat.format(Date())

    suspend fun getOrCreateTodaySteps(goal: Int = 10000): DailyStepsEntity {
        val today = getTodayDateString()
        return stepDao.getByDate(today) ?: DailyStepsEntity(
            date = today,
            steps = 0,
            goal = goal
        ).also { stepDao.insertOrUpdate(it) }
    }

    fun getTodayStepsFlow(): Flow<DailyStepsEntity?> = stepDao.getByDateFlow(getTodayDateString())

    suspend fun addStepsToday(stepsToAdd: Int) {
        val today = getTodayDateString()
        val existing = stepDao.getByDate(today)
        if (existing != null) {
            stepDao.addSteps(today, stepsToAdd, System.currentTimeMillis())
        } else {
            stepDao.insertOrUpdate(
                DailyStepsEntity(
                    date = today,
                    steps = stepsToAdd,
                    goal = 10000
                )
            )
        }
    }

    suspend fun setTodaySteps(steps: Int, goal: Int = 10000) {
        val today = getTodayDateString()
        stepDao.insertOrUpdate(
            DailyStepsEntity(
                date = today,
                steps = steps,
                goal = goal,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    fun getLastDays(days: Int): Flow<List<DailyStepsEntity>> = stepDao.getLastDays(days)

    fun getStepsForWeek(): Flow<List<DailyStepsEntity>> {
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = dateFormat.format(calendar.time)
        return stepDao.getStepsBetweenDates(startDate, endDate)
    }

    fun getStepsForMonth(): Flow<List<DailyStepsEntity>> {
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -29)
        val startDate = dateFormat.format(calendar.time)
        return stepDao.getStepsBetweenDates(startDate, endDate)
    }

    suspend fun getWeeklyTotal(): Int {
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = dateFormat.format(calendar.time)
        return stepDao.getTotalStepsBetweenDates(startDate, endDate) ?: 0
    }

    suspend fun getWeeklyAverage(): Float {
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = dateFormat.format(calendar.time)
        return stepDao.getAverageStepsBetweenDates(startDate, endDate) ?: 0f
    }
}
