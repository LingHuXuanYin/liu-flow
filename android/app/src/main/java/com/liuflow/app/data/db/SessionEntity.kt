package com.liuflow.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A completed (or abandoned) focus session. Schema mirrors the PRD §5A.1 model
 * with a few Android-friendly adjustments:
 * - [hour] is Int 0..23 (start hour, local time)
 * - [weekday] is Int 0..6 where 0 = Monday
 * - [date] is "YYYY-MM-DD" in local time
 */
@Entity(
    tableName = "sessions",
    indices = [
        Index("date"),
        Index("weekday"),
        Index("status"),
        Index("category"),
    ],
)
data class SessionEntity(
    @PrimaryKey val id: String,

    @ColumnInfo(name = "task") val task: String,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "planned_duration") val plannedDuration: Int,
    @ColumnInfo(name = "actual_duration") val actualDuration: Int,

    /** "completed" | "abandoned" */
    @ColumnInfo(name = "status") val status: String,

    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long,
    @ColumnInfo(name = "hour") val hour: Int,
    @ColumnInfo(name = "weekday") val weekday: Int,
    @ColumnInfo(name = "date") val date: String,
)
