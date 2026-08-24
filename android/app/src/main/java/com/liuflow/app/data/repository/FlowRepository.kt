package com.liuflow.app.data.repository

import com.liuflow.app.data.db.DailyGoalDao
import com.liuflow.app.data.db.DailyGoalEntity
import com.liuflow.app.data.db.SessionDao
import com.liuflow.app.data.db.SessionEntity
import com.liuflow.app.data.model.Category
import com.liuflow.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

class FlowRepository(
    private val sessionDao: SessionDao,
    private val dailyGoalDao: DailyGoalDao,
) {

    // ----- Sessions -----

    fun observeAll(): Flow<List<SessionEntity>> = sessionDao.observeAll()

    fun observeByDate(date: String): Flow<List<SessionEntity>> = sessionDao.observeByDate(date)

    fun observeRange(startDate: String, endDate: String): Flow<List<SessionEntity>> =
        sessionDao.observeRange(startDate, endDate)

    fun observeSince(startDate: String): Flow<List<SessionEntity>> = sessionDao.observeSince(startDate)

    suspend fun saveCompleted(
        task: String,
        category: Category?,
        plannedMinutes: Int,
        actualMinutes: Int,
        startedAt: Long,
        endedAt: Long,
    ): SessionEntity {
        val localDate = DateUtils.toLocalDate(startedAt)
        val dateTime = DateUtils.toLocalDateTime(startedAt)
        val entity = SessionEntity(
            id = UUID.randomUUID().toString(),
            task = task.ifBlank { "未命名专注" },
            category = category?.id,
            plannedDuration = plannedMinutes,
            actualDuration = actualMinutes,
            status = "completed",
            startedAt = startedAt,
            endedAt = endedAt,
            hour = dateTime.hour,
            weekday = DateUtils.weekdayMonFirst(localDate),
            date = DateUtils.dateString(localDate),
        )
        sessionDao.upsert(entity)
        return entity
    }

    suspend fun saveAbandoned(
        task: String,
        category: Category?,
        plannedMinutes: Int,
        actualMinutes: Int,
        startedAt: Long,
        endedAt: Long,
    ): SessionEntity {
        val localDate = DateUtils.toLocalDate(startedAt)
        val dateTime = DateUtils.toLocalDateTime(startedAt)
        val entity = SessionEntity(
            id = UUID.randomUUID().toString(),
            task = task.ifBlank { "未命名专注" },
            category = category?.id,
            plannedDuration = plannedMinutes,
            actualDuration = actualMinutes,
            status = "abandoned",
            startedAt = startedAt,
            endedAt = endedAt,
            hour = dateTime.hour,
            weekday = DateUtils.weekdayMonFirst(localDate),
            date = DateUtils.dateString(localDate),
        )
        sessionDao.upsert(entity)
        return entity
    }

    suspend fun deleteAll() {
        sessionDao.deleteAll()
        dailyGoalDao.deleteAll()
    }

    // ----- Daily goal -----

    fun observeDailyGoal(date: String): Flow<DailyGoalEntity?> = dailyGoalDao.observe(date)

    suspend fun getDailyGoal(date: String): DailyGoalEntity? = dailyGoalDao.get(date)

    suspend fun setDailyGoal(date: String, target: Int) {
        dailyGoalDao.upsert(DailyGoalEntity(date = date, target = target))
    }
}

/** Returns true if [date] is Mon..Fri. */
fun LocalDate.isWorkday(): Boolean = dayOfWeek.value in 1..5
