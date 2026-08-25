package com.liuflow.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.data.db.SessionEntity
import com.liuflow.app.data.repository.FlowRepository
import com.liuflow.app.data.stats.StatsCalculator
import com.liuflow.app.util.DateUtils
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val recent: List<SessionEntity> = emptyList(),
    /** Today summary — for the top "今日" row of three KPIs. */
    val todayCount: Int = 0,
    val todayMinutes: Int = 0,
    /** Current consecutive-day streak. */
    val streakDays: Int = 0,
    /** Week's bars in calendar order: Mon, Tue, ..., Sun. */
    val weekBars: List<StatsCalculator.DailyCount> = emptyList(),
    val totalWeekCount: Int = 0,
    val totalWeekMinutes: Int = 0,
    /** Same window one week earlier — used to render the +X% trend chip. */
    val prevWeekCount: Int = 0,
    val prevWeekMinutes: Int = 0,
)

class HistoryViewModel(
    private val repo: FlowRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // 31 days covers the 7-day bar chart + recent list.
            val since = DateUtils.dateString(java.time.LocalDate.now().minusDays(31))
            repo.observeSince(since).collect { sessions ->
                val now = LocalDate.now()
                val completed = sessions.filter { it.status == "completed" }
                val todayList = completed.filter { it.date == DateUtils.dateString(now) }
                val rawWeek = StatsCalculator.last7Days(sessions, now)
                val monday = now.minusDays((now.dayOfWeek.value - 1).toLong())
                val byDate = rawWeek.associateBy { it.date }
                val weekMonToSun = (0..6).map { i ->
                    val d = monday.plusDays(i.toLong())
                    byDate[d] ?: StatsCalculator.DailyCount(d, 0, 0)
                }
                val prevRaw = StatsCalculator.lastNDays(completed, days = 7, today = now.minusDays(7))
                val streak = StatsCalculator.streak(sessions).current
                _state.value = HistoryUiState(
                    recent = completed.take(20),
                    todayCount = todayList.size,
                    todayMinutes = todayList.sumOf { it.actualDuration },
                    streakDays = streak,
                    weekBars = weekMonToSun,
                    totalWeekCount = weekMonToSun.sumOf { it.count },
                    totalWeekMinutes = weekMonToSun.sumOf { it.minutes },
                    prevWeekCount = prevRaw.sumOf { it.count },
                    prevWeekMinutes = prevRaw.sumOf { it.minutes },
                )
            }
        }
    }
}
