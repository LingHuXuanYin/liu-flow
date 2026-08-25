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
    val recent: List<SessionEntity> = emptyList(),
    val weekBars: List<StatsCalculator.DailyCount> = emptyList(),
    val totalWeekCount: Int = 0,
    val totalWeekMinutes: Int = 0,
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
                val today = DateUtils.todayDateString()
                val todayList = sessions.filter { it.date == today && it.status == "completed" }
                val week = StatsCalculator.last7Days(sessions)
                _state.value = HistoryUiState(
                    todayCount = todayList.size,
                    todayMinutes = todayList.sumOf { it.actualDuration },
                    recent = sessions.filter { it.status == "completed" }.take(20),
                    weekBars = week,
                    totalWeekCount = week.sumOf { it.count },
                    totalWeekMinutes = week.sumOf { it.minutes },
                )
            }
        }
    }
}
