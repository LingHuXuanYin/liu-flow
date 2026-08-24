package com.liuflow.app.ui.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuflow.app.R
import com.liuflow.app.ui.theme.LocalFlowColors

@Composable
fun HeatmapScreen(
    viewModel: HeatmapViewModel,
    onBack: () -> Unit,
) {
    val colors = LocalFlowColors.current
    val s by viewModel.state.collectAsStateWithLifecycle()

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                }
                Spacer(Modifier.weight(1f))
                Text(stringResource(R.string.heatmap_title), color = colors.onSurface, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            ) {
                Text(
                    text = "近 7 天 × 24 小时 = 168 格",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                HeatmapGrid(grid = s.grid, max = s.max)
                Spacer(Modifier.height(16.dp))
                HeatmapLegend()
            }
        }
    }
}

@Composable
private fun HeatmapGrid(grid: Array<IntArray>, max: Int) {
    val colors = LocalFlowColors.current
    val cellSize = 14.dp
    val gap = 2.dp
    val rowHeight = cellSize + gap

    Column(modifier = Modifier.fillMaxWidth()) {
        // Hour axis (top): 0, 6, 12, 18
        Row(modifier = Modifier.fillMaxWidth().padding(start = 28.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(0, 6, 12, 18).forEach { h ->
                Text(
                    text = "$h",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        for (w in 0 until 7) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = weekdayLabel(w),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.size(width = 24.dp, height = cellSize),
                )
                Spacer(Modifier.size(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    for (h in 0 until 24) {
                        val count = grid[w][h]
                        val ratio = if (max == 0) 0f else count.toFloat() / max
                        val baseColor = colors.primary
                        val cellColor = when {
                            count == 0 -> colors.surfaceContainerHigh
                            else -> baseColor.copy(alpha = 0.15f + ratio * 0.85f)
                        }
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(RoundedCornerShape(2.dp))
                                .background(cellColor),
                        )
                    }
                }
            }
            Spacer(Modifier.height(gap))
        }
    }
}

@Composable
private fun HeatmapLegend() {
    val colors = LocalFlowColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.heatmap_legend), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf(0.15f, 0.35f, 0.55f, 0.75f, 0.95f).forEach { a ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.primary.copy(alpha = a)),
                )
            }
        }
    }
}

private fun weekdayLabel(w: Int) = when (w) {
    0 -> "一"; 1 -> "二"; 2 -> "三"; 3 -> "四"; 4 -> "五"; 5 -> "六"; 6 -> "日"
    else -> ""
}
