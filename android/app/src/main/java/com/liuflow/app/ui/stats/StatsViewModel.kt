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
    val prevWeekBars: List<StatsCalculator.DailyCount> = emptyList(),
    val categories7d: List<StatsCalculator.CategoryBreakdown> = emptyList(),
    val categories30d: List<StatsCalculator.CategoryBreakdown> = emptyList(),
    val dailyTarget: Int = 4,
    val todayCount: Int = 0,
    val workdayMinutes: Int = 0,
    val weekendMinutes: Int = 0,
    val heatmap: Array<IntArray> = Array(7) { IntArray(24) },
    val heatmapMax: Int = 0,
    /** Trend vs previous period: this-week count delta as fraction of previous week. */
    val weekCountTrend: Float = 0f,
    val weekMinutesTrend: Float = 0f,
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
            // We need the full history for streak / overview / trend.
            repo.observeAll().collect { all ->
                val today = DateUtils.todayDateString()
                val todayList = all.filter { it.date == today && it.status == "completed" }
                val (workMin, weekMin) = StatsCalculator.workdayVsWeekendMinutes(all, days = 30)
                val heatmap = StatsCalculator.heatmap(all)
                val heatmapMax = heatmap.maxOf { row -> row.maxOrNull() ?: 0 }
                val week = StatsCalculator.last7Days(all)
                val prevWeek = StatsCalculator.lastNDays(all, days = 7, today = java.time.LocalDate.now().minusDays(7))
                val weekCount = week.sumOf { it.count }
                val prevCount = prevWeek.sumOf { it.count }
                val weekMins = week.sumOf { it.minutes }
                val prevMins = prevWeek.sumOf { it.minutes }
                _state.value = StatsUiState(
                    overview = StatsCalculator.overview(all),
                    streak = StatsCalculator.streak(all),
                    weekBars = week,
                    prevWeekBars = prevWeek,
                    categories7d = StatsCalculator.categoryBreakdown(all, days = 7),
                    categories30d = StatsCalculator.categoryBreakdown(all, days = 30),
                    dailyTarget = settingsState.value.dailyTarget,
                    todayCount = todayList.size,
                    workdayMinutes = workMin,
                    weekendMinutes = weekMin,
                    heatmap = heatmap,
                    heatmapMax = heatmapMax,
                    weekCountTrend = if (prevCount == 0) (if (weekCount > 0) 1f else 0f)
                        else (weekCount - prevCount).toFloat() / prevCount,
                    weekMinutesTrend = if (prevMins == 0) (if (weekMins > 0) 1f else 0f)
                        else (weekMins - prevMins).toFloat() / prevMins,
                )
            }
        }
    }
}
