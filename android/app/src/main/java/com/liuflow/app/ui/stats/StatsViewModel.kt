package com.liuflow.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.data.db.SessionEntity
import com.liuflow.app.data.prefs.SettingsRepository
import com.liuflow.app.data.prefs.UserSettings
import com.liuflow.app.data.repository.FlowRepository
import com.liuflow.app.data.stats.StatsCalculator
import com.liuflow.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatsUiState(
    val overview: StatsCalculator.Overview = StatsCalculator.Overview(0, 0, 0, 0f, 0, 0),
    val streak: StatsCalculator.Streak = StatsCalculator.Streak(0, 0),
    val weekBars: List<StatsCalculator.DailyCount> = emptyList(),
    val categories7d: List<StatsCalculator.CategoryBreakdown> = emptyList(),
    val categories30d: List<StatsCalculator.CategoryBreakdown> = emptyList(),
    val dailyTarget: Int = 4,
    val todayCount: Int = 0,
    val workdayMinutes: Int = 0,
    val weekendMinutes: Int = 0,
    /** 7×24 grid; first dimension is weekday (0=Mon), second is hour (0..23). */
    val heatmap: Array<IntArray> = Array(7) { IntArray(24) },
    val heatmapMax: Int = 0,
)

class StatsViewModel(
    private val repo: FlowRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    val settingsState: StateFlow<UserSettings> = settings.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    init {
        viewModelScope.launch {
            // We need the full history for streak / overview.
            repo.observeAll().collect { all ->
                val today = DateUtils.todayDateString()
                val todayList = all.filter { it.date == today && it.status == "completed" }
                val (workMin, weekMin) = StatsCalculator.workdayVsWeekendMinutes(all, days = 30)
                val heatmap = StatsCalculator.heatmap(all)
                val heatmapMax = heatmap.maxOf { row -> row.maxOrNull() ?: 0 }
                _state.value = StatsUiState(
                    overview = StatsCalculator.overview(all),
                    streak = StatsCalculator.streak(all),
                    weekBars = StatsCalculator.last7Days(all),
                    categories7d = StatsCalculator.categoryBreakdown(all, days = 7),
                    categories30d = StatsCalculator.categoryBreakdown(all, days = 30),
                    dailyTarget = settingsState.value.dailyTarget,
                    todayCount = todayList.size,
                    workdayMinutes = workMin,
                    weekendMinutes = weekMin,
                    heatmap = heatmap,
                    heatmapMax = heatmapMax,
                )
            }
        }
    }
}
