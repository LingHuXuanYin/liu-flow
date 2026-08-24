package com.liuflow.app.ui.heatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.data.repository.FlowRepository
import com.liuflow.app.data.stats.StatsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HeatmapUiState(
    val grid: Array<IntArray> = Array(7) { IntArray(24) },
    val max: Int = 0,
)

class HeatmapViewModel(
    private val repo: FlowRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HeatmapUiState())
    val state: StateFlow<HeatmapUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeAll().collect { all ->
                val grid = StatsCalculator.heatmap(all)
                val max = grid.maxOf { row -> row.maxOrNull() ?: 0 }
                _state.value = HeatmapUiState(grid = grid, max = max)
            }
        }
    }
}
