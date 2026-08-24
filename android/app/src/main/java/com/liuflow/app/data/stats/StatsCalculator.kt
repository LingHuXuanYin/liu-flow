package com.liuflow.app.data.stats

import com.liuflow.app.data.db.SessionEntity
import com.liuflow.app.data.model.Category
import com.liuflow.app.util.DateUtils
import java.time.LocalDate
import kotlin.math.max

/**
 * Pure functions that compute statistics from a list of [SessionEntity] records.
 *
 * Inputs are always kept "as of today" — pass [today] explicitly in tests to
 * make the math deterministic.
 */
object StatsCalculator {

    data class Overview(
        val totalCount: Int,            // total sessions (completed only)
        val totalMinutes: Int,          // sum of actualDuration
        val totalDays: Int,             // distinct dates with >= 1 completed
        val completionRate: Float,      // 0f..1f
        val bestDayCount: Int,          // max sessions in a single day
        val longestMinutes: Int,        // longest single completed session
    )

    data class DailyCount(val date: LocalDate, val count: Int, val minutes: Int)

    data class CategoryBreakdown(
        val category: Category,
        val count: Int,
        val minutes: Int,
        val ratio: Float,        // 0..1 share of total
    )

    data class Streak(
        val current: Int,
        val best: Int,
    )

    fun overview(sessions: List<SessionEntity>): Overview {
        val completed = sessions.filter { it.status == "completed" }
        val abandoned = sessions.count { it.status == "abandoned" }
        val total = completed.size + abandoned
        val totalMinutes = completed.sumOf { it.actualDuration }
        val totalDays = completed.map { it.date }.distinct().size
        val byDate = completed.groupBy { it.date }
        val bestDay = byDate.values.maxOfOrNull { it.size } ?: 0
        val longest = completed.maxOfOrNull { it.actualDuration } ?: 0
        val rate = if (total == 0) 0f else completed.size.toFloat() / total
        return Overview(
            totalCount = completed.size,
            totalMinutes = totalMinutes,
            totalDays = totalDays,
            completionRate = rate,
            bestDayCount = bestDay,
            longestMinutes = longest,
        )
    }

    fun last7Days(sessions: List<SessionEntity>, today: LocalDate = LocalDate.now()): List<DailyCount> {
        val dates = DateUtils.lastNDates(7, today)
        val grouped = sessions.filter { it.status == "completed" }.groupBy { it.date }
        return dates.map { d ->
            val list = grouped[DateUtils.dateString(d)].orEmpty()
            DailyCount(
                date = d,
                count = list.size,
                minutes = list.sumOf { it.actualDuration },
            )
        }
    }

    fun last30Days(sessions: List<SessionEntity>, today: LocalDate = LocalDate.now()): List<DailyCount> {
        val dates = DateUtils.lastNDates(30, today)
        val grouped = sessions.filter { it.status == "completed" }.groupBy { it.date }
        return dates.map { d ->
            val list = grouped[DateUtils.dateString(d)].orEmpty()
            DailyCount(
                date = d,
                count = list.size,
                minutes = list.sumOf { it.actualDuration },
            )
        }
    }

    fun categoryBreakdown(sessions: List<SessionEntity>, days: Int, today: LocalDate = LocalDate.now()): List<CategoryBreakdown> {
        val start = today.minusDays((days - 1).toLong())
        val startStr = DateUtils.dateString(start)
        val completed = sessions.filter { it.status == "completed" && it.date >= startStr }
        val totalMinutes = max(1, completed.sumOf { it.actualDuration })
        return Category.entries
            .map { cat ->
                val list = completed.filter { it.category == cat.id }
                val mins = list.sumOf { it.actualDuration }
                CategoryBreakdown(
                    category = cat,
                    count = list.size,
                    minutes = mins,
                    ratio = mins.toFloat() / totalMinutes,
                )
            }
            .filter { it.count > 0 }
            .sortedByDescending { it.minutes }
    }

    fun streak(sessions: List<SessionEntity>, today: LocalDate = LocalDate.now()): Streak {
        val completed = sessions.filter { it.status == "completed" }
        if (completed.isEmpty()) return Streak(0, 0)
        val dates = completed.map { LocalDate.parse(it.date) }.toSet().sorted()
        val dateSet = dates.toSet()

        var best = 0
        var run = 0
        var prev: LocalDate? = null
        for (d in dates) {
            run = if (prev != null && prev.plusDays(1) == d) run + 1 else 1
            if (run > best) best = run
            prev = d
        }

        // Current streak: walk back from today
        var current = 0
        var cursor: LocalDate? = today
        while (cursor != null && cursor in dateSet) {
            current += 1
            cursor = cursor.minusDays(1)
        }
        // If today has no session but yesterday does, current is from yesterday backwards
        if (current == 0) {
            cursor = today.minusDays(1)
            while (cursor != null && cursor in dateSet) {
                current += 1
                cursor = cursor.minusDays(1)
            }
        }
        return Streak(current = current, best = best)
    }

    fun workdayVsWeekendMinutes(sessions: List<SessionEntity>, days: Int, today: LocalDate = LocalDate.now()): Pair<Int, Int> {
        val start = today.minusDays((days - 1).toLong())
        val startStr = DateUtils.dateString(start)
        val list = sessions.filter { it.status == "completed" && it.date >= startStr }
        val work = list.filter { it.weekday in 0..4 }.sumOf { it.actualDuration }
        val weekend = list.filter { it.weekday in 5..6 }.sumOf { it.actualDuration }
        return work to weekend
    }

    /** 7x24 heatmap matrix indexed as [weekday 0..6][hour 0..23] -> count. */
    fun heatmap(sessions: List<SessionEntity>, today: LocalDate = LocalDate.now()): Array<IntArray> {
        val start = today.minusDays(6)
        val startStr = DateUtils.dateString(start)
        val list = sessions.filter { it.status == "completed" && it.date >= startStr }
        val grid = Array(7) { IntArray(24) }
        list.forEach { s ->
            if (s.weekday in 0..6 && s.hour in 0..23) {
                grid[s.weekday][s.hour] += 1
            }
        }
        return grid
    }
}
