package com.liuflow.app.data.stats

import com.liuflow.app.data.db.SessionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StatsCalculatorTest {

    private fun makeSession(
        date: String,
        weekday: Int,
        hour: Int,
        actualDuration: Int,
        status: String = "completed",
        category: String? = "writing",
    ) = SessionEntity(
        id = "$date-$hour",
        task = "t",
        category = category,
        plannedDuration = 25,
        actualDuration = actualDuration,
        status = status,
        startedAt = 0L,
        endedAt = 0L,
        hour = hour,
        weekday = weekday,
        date = date,
    )

    @Test
    fun `overview aggregates completed only`() {
        val today = LocalDate.of(2026, 8, 23)
        val sessions = listOf(
            makeSession("2026-08-23", 6, 9, 25),
            makeSession("2026-08-23", 6, 14, 25),
            makeSession("2026-08-22", 5, 11, 25, status = "abandoned"),
        )
        val o = StatsCalculator.overview(sessions)
        assertEquals(2, o.totalCount)
        assertEquals(50, o.totalMinutes)
        assertEquals(1, o.totalDays)
        assertEquals(0.666f, o.completionRate, 0.01f)
        assertEquals(2, o.bestDayCount)
        assertEquals(25, o.longestMinutes)
    }

    @Test
    fun `streak counts consecutive days back from today`() {
        val today = LocalDate.of(2026, 8, 23)
        // Today + yesterday + 2 days back, with a gap.
        val s = listOf(
            makeSession("2026-08-23", 6, 9, 25),
            makeSession("2026-08-22", 5, 9, 25),
            makeSession("2026-08-21", 4, 9, 25),
            makeSession("2026-08-19", 2, 9, 25),
        )
        val st = StatsCalculator.streak(s, today)
        assertEquals(3, st.current)
        assertEquals(3, st.best)
    }

    @Test
    fun `heatmap produces 7x24 grid`() {
        val today = LocalDate.of(2026, 8, 23)
        val sessions = listOf(
            makeSession("2026-08-23", 6, 9, 25),
            makeSession("2026-08-22", 5, 9, 25),
            makeSession("2026-08-22", 5, 10, 25),
        )
        val grid = StatsCalculator.heatmap(sessions, today)
        assertEquals(7, grid.size)
        assertEquals(24, grid[0].size)
        assertEquals(1, grid[6][9])
        assertEquals(2, grid[5][9] + grid[5][10])
    }

    @Test
    fun `last7Days fills missing days with zeros`() {
        val today = LocalDate.of(2026, 8, 23)
        val sessions = listOf(makeSession("2026-08-23", 6, 9, 25))
        val week = StatsCalculator.last7Days(sessions, today)
        assertEquals(7, week.size)
        assertEquals(1, week.last().count)
        assertEquals(0, week.first().count)
    }
}
