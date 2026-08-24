package com.liuflow.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: DailyGoalEntity)

    @Query("SELECT * FROM daily_goals WHERE date = :date")
    fun observe(date: String): Flow<DailyGoalEntity?>

    @Query("SELECT * FROM daily_goals WHERE date = :date")
    suspend fun get(date: String): DailyGoalEntity?

    @Query("DELETE FROM daily_goals")
    suspend fun deleteAll()
}
