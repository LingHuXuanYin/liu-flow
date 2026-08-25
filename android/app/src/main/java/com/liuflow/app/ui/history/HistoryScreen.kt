package com.liuflow.app.ui.history

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuflow.app.R
import com.liuflow.app.data.db.SessionEntity
import com.liuflow.app.data.stats.StatsCalculator
import com.liuflow.app.ui.theme.LocalFlowColors
import com.liuflow.app.util.DateUtils
import com.liuflow.app.util.TimeFormat

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onOpenStats: () -> Unit,
    onOpenWeekly: () -> Unit,
) {
    val colors = LocalFlowColors.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            TopHeader(onOpenStats = onOpenStats, onOpenWeekly = onOpenWeekly)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { TodaySummaryCard(state.todayCount, state.todayMinutes) }
                item { WeekChartCard(state.weekBars) }
                item {
                    Text(
                        text = stringResource(R.string.history_recent_section),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                if (state.recent.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.history_empty),
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    items(state.recent) { s -> RecentRow(s) }
                }
            }
        }
    }
}

@Composable
private fun TopHeader(onOpenStats: () -> Unit, onOpenWeekly: () -> Unit) {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "HISTORY",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.history_title),
                color = colors.onSurface,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderIcon(icon = Icons.Filled.CalendarMonth, onClick = onOpenWeekly)
            HeaderIcon(icon = Icons.Filled.BarChart, onClick = onOpenStats)
        }
    }
}

@Composable
private fun HeaderIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val colors = LocalFlowColors.current
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(50))
            .background(colors.surfaceContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant)
    }
}

@Composable
private fun TodaySummaryCard(count: Int, minutes: Int) {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primaryContainer)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.history_today_section),
                color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = count.toString(),
                color = colors.onPrimaryContainer,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                ),
            )
            Text(
                text = "次 · ${minutes} 分钟",
                color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun WeekChartCard(week: List<StatsCalculator.DailyCount>) {
    val colors = LocalFlowColors.current
    val maxCount = (week.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainer)
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.history_weekly_section),
            color = colors.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            week.forEach { d ->
                val ratio = d.count.toFloat() / maxCount
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .height((ratio * 96).dp.coerceAtLeast(6.dp))
                            .width(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (d.count == 0) colors.outlineVariant else colors.primary),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = DateUtils.formatWeekdayShort(d.date).takeLast(1),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentRow(s: SessionEntity) {
    val colors = LocalFlowColors.current
    val cat = com.liuflow.app.data.model.Category.fromId(s.category)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(50))
                .background(cat?.accent?.copy(alpha = 0.15f) ?: colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            if (cat != null) {
                Icon(cat.icon, contentDescription = null, tint = cat.accent, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(s.task, color = colors.onSurface, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${DateUtils.formatTime(s.startedAt)} · ${s.actualDuration} 分钟",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
