package com.liuflow.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.data.db.SessionEntity
import com.liuflow.app.data.repository.FlowRepository
import com.liuflow.app.data.stats.StatsCalculator
import com.liuflow.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val todayCount: Int = 0,
    val todayMinutes: Int = 0,
    val streakDays: Int = 0,
    val recent: List<SessionEntity> = emptyList(),
    val weekBars: List<StatsCalculator.DailyCount> = emptyList(),
    val prevWeekBars: List<StatsCalculator.DailyCount> = emptyList(),
    val totalWeekCount: Int = 0,
    val totalWeekMinutes: Int = 0,
    /** % change of this week count vs previous week (e.g. 0.18 = +18%). */
    val weekTrendPct: Float = 0f,
)

class HistoryViewModel(
    private val repo: FlowRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // 45 days covers both this week (7) and previous week (7) for trend.
            val since = DateUtils.dateString(java.time.LocalDate.now().minusDays(45))
            repo.observeSince(since).collect { sessions ->
                val today = DateUtils.todayDateString()
                val todayList = sessions.filter { it.date == today && it.status == "completed" }
                val week = StatsCalculator.last7Days(sessions)
                val prevWeek = StatsCalculator.lastNDays(sessions, days = 7, today = java.time.LocalDate.now().minusDays(7))
                val weekTotal = week.sumOf { it.count }
                val prevTotal = prevWeek.sumOf { it.count }
                val trend = if (prevTotal == 0) {
                    if (weekTotal > 0) 1f else 0f
                } else {
                    (weekTotal - prevTotal).toFloat() / prevTotal
                }
                _state.value = HistoryUiState(
                    todayCount = todayList.size,
                    todayMinutes = todayList.sumOf { it.actualDuration },
                    streakDays = StatsCalculator.streak(sessions).current,
                    recent = sessions.filter { it.status == "completed" }.take(20),
                    weekBars = week,
                    prevWeekBars = prevWeek,
                    totalWeekCount = weekTotal,
                    totalWeekMinutes = week.sumOf { it.minutes },
                    weekTrendPct = trend,
                )
            }
        }
    }
}

