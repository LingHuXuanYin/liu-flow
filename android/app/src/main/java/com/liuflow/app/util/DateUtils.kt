package com.liuflow.app.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {

    private val ZONE: ZoneId = ZoneId.systemDefault()
    private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val MONTH_DAY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
    private val WEEKDAY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.CHINA)
    private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)

    fun nowMillis(): Long = System.currentTimeMillis()

    fun toLocalDate(epochMs: Long): LocalDate = Instant.ofEpochMilli(epochMs).atZone(ZONE).toLocalDate()

    fun toLocalDateTime(epochMs: Long): LocalDateTime = Instant.ofEpochMilli(epochMs).atZone(ZONE).toLocalDateTime()

    fun todayDateString(): String = LocalDate.now(ZONE).format(DATE_FMT)

    fun dateString(date: LocalDate): String = date.format(DATE_FMT)

    fun formatDate(epochMs: Long): String = toLocalDate(epochMs).format(MONTH_DAY_FMT)

    fun formatTime(epochMs: Long): String = toLocalDateTime(epochMs).format(TIME_FMT)

    fun formatWeekday(epochMs: Long): String = toLocalDate(epochMs).format(WEEKDAY_FMT)

    fun formatWeekdayShort(date: LocalDate): String = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
        else -> ""
    }

    /** Returns 0..6 where 0 = Monday, aligning with PRD §5A.1. */
    fun weekdayMonFirst(date: LocalDate): Int = date.dayOfWeek.value - 1

    /** Inclusive start of the last [days] days, oldest first. */
    fun lastNDates(days: Int, today: LocalDate = LocalDate.now(ZONE)): List<LocalDate> =
        (0 until days).map { today.minusDays((days - 1 - it).toLong()) }

    fun daysBetween(start: LocalDate, end: LocalDate): Long = ChronoUnit.DAYS.between(start, end)
}
