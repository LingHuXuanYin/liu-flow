package com.liuflow.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY started_at DESC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE date = :date ORDER BY started_at DESC")
    fun observeByDate(date: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE date >= :startDate ORDER BY started_at ASC")
    fun observeSince(startDate: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE date >= :startDate AND date <= :endDate ORDER BY started_at ASC")
    fun observeRange(startDate: String, endDate: String): Flow<List<SessionEntity>>

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM sessions WHERE status = 'completed'")
    suspend fun countCompleted(): Int
}
