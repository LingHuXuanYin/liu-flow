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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TrendingUp
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
                item { TodaySummaryCard(state.todayCount, state.todayMinutes, state.streakDays) }
                item { WeekChartCard(state.weekBars, state.weekTrendPct) }
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
        Spacer(Modifier.weight(1f))
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
private fun TodaySummaryCard(count: Int, minutes: Int, streakDays: Int) {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.primaryContainer)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Kpi(label = "完成次数", value = count.toString(), accent = colors.onPrimaryContainer)
        Kpi(
            label = "总时长",
            value = friendlyHoursMinutes(minutes),
            accent = colors.onPrimaryContainer,
        )
        Kpi(
            label = "连续天数",
            value = streakDays.toString(),
            accent = colors.primary,
        )
    }
}

@Composable
private fun Kpi(label: String, value: String, accent: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = accent.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            color = accent,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
            ),
        )
    }
}

@Composable
private fun WeekChartCard(week: List<StatsCalculator.DailyCount>, trendPct: Float) {
    val colors = LocalFlowColors.current
    val maxCount = (week.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainer)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "本周",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = week.sumOf { it.count }.toString(),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Light,
                        ),
                    )
                    Spacer(Modifier.width(8.dp))
                    val mins = week.sumOf { it.minutes }
                    Text(
                        text = "次 · ${friendlyHoursMinutes(mins)}",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (trendPct != 0f) {
                val sign = if (trendPct > 0) "↑" else "↓"
                val color = if (trendPct > 0) colors.primary else colors.error
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "$sign ${(kotlin.math.abs(trendPct) * 100).toInt()}%",
                        color = color,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Bar chart with grid lines + color layer (past = light, today = primary)
        Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 3 dashed horizontal grid lines (0, 50%, 100%)
                val rowYs = listOf(size.height, size.height / 2f, 0f)
                rowYs.forEach { y ->
                    drawLine(
                        color = colors.outlineVariant.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 6f)),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                week.forEachIndexed { idx, d ->
                    val ratio = d.count.toFloat() / maxCount
                    val isToday = idx == week.lastIndex
                    val barColor = when {
                        d.count == 0 -> colors.outlineVariant.copy(alpha = 0.3f)
                        isToday -> colors.primary
                        else -> colors.primaryContainer
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height((ratio * 80).dp.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(barColor),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = DateUtils.formatWeekdayShort(d.date).takeLast(1),
                            color = if (isToday) colors.primary else colors.onSurfaceVariant,
                            style = if (isToday) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                            else MaterialTheme.typography.labelSmall,
                        )
                    }
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
                .clip(CircleShape)
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

private fun friendlyHoursMinutes(minutes: Int): String {
    if (minutes < 60) return "${minutes}m"
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0) "${h}h" else "${h}h${m}m"
}
