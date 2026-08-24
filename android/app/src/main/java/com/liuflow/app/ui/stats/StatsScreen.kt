package com.liuflow.app.ui.stats

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuflow.app.R
import com.liuflow.app.data.stats.StatsCalculator
import com.liuflow.app.ui.theme.LocalFlowColors
import com.liuflow.app.util.DateUtils
import com.liuflow.app.util.TimeFormat

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onBack: () -> Unit,
    onOpenHeatmap: () -> Unit,
) {
    val colors = LocalFlowColors.current
    val s by viewModel.state.collectAsStateWithLifecycle()

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            TopBar(onBack)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { OverviewCard(s.overview) }
                item { StreakCard(s.streak) }
                item { DailyGoalCard(s.todayCount, s.dailyTarget) }
                item { WeekBarCard(s.weekBars) }
                item { CategoryBarsCard(s.categories7d) }
                item { MiniHeatmapCard(grid = s.heatmap, max = s.heatmapMax, onOpen = onOpenHeatmap) }
                item { WorkdayWeekendCard(s.workdayMinutes, s.weekendMinutes) }
                item { BestRecordsCard(s.overview) }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.stats_title),
            color = colors.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.weight(1f))
        // Symmetric spacer; the heatmap now has its own card entry below.
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun OverviewCard(o: StatsCalculator.Overview) {
    val colors = LocalFlowColors.current
    Card {
        Text(stringResource(R.string.stats_overview), color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Kpi(o.totalCount.toString(), R.string.stats_kpi_count)
            Kpi(TimeFormat.friendlyMinutes(o.totalMinutes), R.string.stats_kpi_minutes)
            Kpi(o.totalDays.toString(), R.string.stats_kpi_days)
            Kpi("${(o.completionRate * 100).toInt()}%", R.string.stats_kpi_completion)
        }
    }
}

@Composable
private fun Kpi(value: String, labelRes: Int) {
    val colors = LocalFlowColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = colors.onSurface,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
            ),
        )
        Text(
            text = stringResource(labelRes),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun StreakCard(streak: StatsCalculator.Streak) {
    val colors = LocalFlowColors.current
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = colors.error)
            Spacer(Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.stats_streak_days, streak.current),
                color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.stats_streak_best, streak.best),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun WeekBarCard(week: List<StatsCalculator.DailyCount>) {
    val colors = LocalFlowColors.current
    val maxCount = (week.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    Card {
        Text(stringResource(R.string.stats_weekly_chart_title), color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            week.forEach { d ->
                val ratio = d.count.toFloat() / maxCount
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .height((ratio * 80).dp.coerceAtLeast(4.dp))
                            .size(width = 14.dp, height = (ratio * 80).dp.coerceAtLeast(4.dp))
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
private fun CategoryBarsCard(cats: List<StatsCalculator.CategoryBreakdown>) {
    val colors = LocalFlowColors.current
    Card {
        Text(stringResource(R.string.stats_category_7d_title), color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        if (cats.isEmpty()) {
            Text("近 7 天还没有分类数据", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        } else {
            cats.forEach { c ->
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(CircleShape).background(c.category.accent),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = stringResource(c.category.labelRes),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(64.dp),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceContainerHigh),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(c.ratio.coerceIn(0f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(c.category.accent),
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "${(c.ratio * 100).toInt()}%",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyGoalCard(today: Int, target: Int) {
    val colors = LocalFlowColors.current
    val ratio = (today.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    val remaining = (target - today).coerceAtLeast(0)
    val hint = when {
        today == 0 -> "开始第一次专注吧"
        today >= target -> "今日目标完成 🎉"
        else -> "还差 $remaining 次"
    }
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.stats_daily_goal_title),
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.stats_daily_goal_progress, today, target),
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = hint,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 10.dp.toPx()
                    val topLeft = Offset(stroke / 2, stroke / 2)
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    drawArc(
                        color = colors.surfaceContainerHigh,
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        topLeft = topLeft, size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = colors.primary,
                        startAngle = -90f, sweepAngle = 360f * ratio, useCenter = false,
                        topLeft = topLeft, size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
                Text(
                    "${(ratio * 100).toInt()}%",
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }
    }
}

@Composable
private fun MiniHeatmapCard(grid: Array<IntArray>, max: Int, onOpen: () -> Unit) {
    val colors = LocalFlowColors.current
    Card {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onOpen)) {
            Icon(Icons.Filled.GridView, contentDescription = null, tint = colors.onSurfaceVariant)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.stats_heatmap_title),
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "近 7 天 × 24 小时 · 168 格",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = stringResource(R.string.stats_heatmap_open),
                color = colors.primary,
                style = MaterialTheme.typography.labelLarge,
            )
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        // Mini 7×24 grid: cell 10dp + 1dp gap; total ≈ 73×263 dp
        Column {
            for (w in 0 until 7) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (w) { 0 -> "一"; 1 -> "二"; 2 -> "三"; 3 -> "四"; 4 -> "五"; 5 -> "六"; else -> "日" },
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.size(width = 14.dp, height = 11.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        for (h in 0 until 24) {
                            val count = grid[w][h]
                            val ratio = if (max == 0) 0f else count.toFloat() / max
                            val cell = when {
                                count == 0 -> colors.surfaceContainerHigh
                                else -> colors.primary.copy(alpha = 0.18f + ratio * 0.82f)
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(cell),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(1.dp))
            }
        }
    }
}

@Composable
private fun WorkdayWeekendCard(workMin: Int, weekendMin: Int) {
    val colors = LocalFlowColors.current
    val total = (workMin + weekendMin).coerceAtLeast(1)
    val workRatio = workMin.toFloat() / total
    Card {
        Text(stringResource(R.string.stats_workday_weekend), color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.stats_workday, workMin / 60.0), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.stats_weekend, weekendMin / 60.0), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(workRatio.coerceAtLeast(0.001f))
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.primary),
            )
            Box(
                modifier = Modifier
                    .weight((1f - workRatio).coerceAtLeast(0.001f))
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.tertiary),
            )
        }
    }
}

@Composable
private fun BestRecordsCard(o: StatsCalculator.Overview) {
    val colors = LocalFlowColors.current
    Card {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RecordColumn(stringResource(R.string.stats_records_best_day), "${o.bestDayCount} 次")
            RecordColumn(stringResource(R.string.stats_records_longest), "${o.longestMinutes}m")
            RecordColumn(stringResource(R.string.stats_records_total_days), "${o.totalDays} 天")
            RecordColumn(stringResource(R.string.stats_records_total_minutes), TimeFormat.friendlyMinutes(o.totalMinutes))
        }
    }
}

@Composable
private fun RecordColumn(label: String, value: String) {
    val colors = LocalFlowColors.current
    Column(horizontalAlignment = Alignment.Start) {
        Text(value, color = colors.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Light))
        Text(label, color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    val colors = LocalFlowColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainer)
            .padding(20.dp),
    ) { content() }
}
