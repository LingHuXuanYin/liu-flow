package com.liuflow.app.ui.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.data.repository.FlowRepository
import com.liuflow.app.data.stats.StatsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeeklyUiState(
    val week: List<StatsCalculator.DailyCount> = emptyList(),
    val totalCount: Int = 0,
    val totalMinutes: Int = 0,
    val longestMinutes: Int = 0,
    val categories: List<StatsCalculator.CategoryBreakdown> = emptyList(),
    /** Extra minutes this week vs last week. */
    val deltaMinutesVsPrev: Int = 0,
    val topCategoryId: String? = null,
    val periodLabel: String = "",
)

class WeeklyViewModel(
    private val repo: FlowRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WeeklyUiState())
    val state: StateFlow<WeeklyUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeAll().collect { all ->
                val today = java.time.LocalDate.now()
                val weekStart = today.minusDays(6)
                val week = StatsCalculator.last7Days(all)
                val prevWeek = StatsCalculator.lastNDays(all, days = 7, today = today.minusDays(7))
                val cats = StatsCalculator.categoryBreakdown(all, days = 7)
                val totalMins = week.sumOf { it.minutes }
                val prevMins = prevWeek.sumOf { it.minutes }
                _state.value = WeeklyUiState(
                    week = week,
                    totalCount = week.sumOf { it.count },
                    totalMinutes = totalMins,
                    longestMinutes = week.maxOfOrNull { it.minutes } ?: 0,
                    categories = cats,
                    deltaMinutesVsPrev = totalMins - prevMins,
                    topCategoryId = cats.firstOrNull()?.category?.id,
                    periodLabel = "${monthDay(weekStart)} - ${monthDay(today)}",
                )
            }
        }
    }

    private fun monthDay(d: java.time.LocalDate): String = "${d.monthValue}/${d.dayOfMonth}"
}

