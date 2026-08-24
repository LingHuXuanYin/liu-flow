package com.liuflow.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Daily focus session target. PK is the date string "YYYY-MM-DD". */
@Entity(tableName = "daily_goals")
data class DailyGoalEntity(
    @PrimaryKey @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "target") val target: Int,
)
