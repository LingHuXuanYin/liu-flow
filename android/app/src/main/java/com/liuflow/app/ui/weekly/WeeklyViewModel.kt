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
)

class WeeklyViewModel(
    private val repo: FlowRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WeeklyUiState())
    val state: StateFlow<WeeklyUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeAll().collect { all ->
                val week = StatsCalculator.last7Days(all)
                _state.value = WeeklyUiState(
                    week = week,
                    totalCount = week.sumOf { it.count },
                    totalMinutes = week.sumOf { it.minutes },
                    longestMinutes = week.maxOfOrNull { it.minutes } ?: 0,
                )
            }
        }
    }
}
