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
import com.liuflow.app.data.model.Category
import com.liuflow.app.data.stats.StatsCalculator
import com.liuflow.app.ui.theme.LocalFlowColors
import com.liuflow.app.util.DateUtils
import com.liuflow.app.util.TimeFormat
import java.time.LocalDate
import kotlin.math.roundToInt

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
            TopHeader(onOpenStats = onOpenStats)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 24.dp,
                    vertical = 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    TodayCard(
                        count = state.todayCount,
                        minutes = state.todayMinutes,
                        streak = state.streakDays,
                    )
                }
                item {
                    WeekCard(
                        bars = state.weekBars,
                        totalCount = state.totalWeekCount,
                        totalMinutes = state.totalWeekMinutes,
                        prevCount = state.prevWeekCount,
                        prevMinutes = state.prevWeekMinutes,
                    )
                }
                item {
                    RecentHeader()
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
private fun TopHeader(onOpenStats: () -> Unit) {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.history_title),
            color = colors.onSurface,
            style = MaterialTheme.typography.headlineMedium,
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.surfaceContainer)
                .clickable(onClick = onOpenStats),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.BarChart, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun TodayCard(count: Int, minutes: Int, streak: Int) {
    val colors = LocalFlowColors.current
    Column {
        Text(
            text = "今日",
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TodayKpi(value = count.toString(), label = "完成次数", modifier = Modifier.weight(1f))
            TodayKpi(
                value = TimeFormat.friendlyMinutes(minutes),
                label = "总时长",
                modifier = Modifier.weight(1f),
            )
            TodayKpi(value = streak.toString(), label = "连续天数", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TodayKpi(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = LocalFlowColors.current
    Column(modifier = modifier) {
        Text(
            text = value,
            color = colors.onSurface,
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
            ),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun WeekCard(
    bars: List<StatsCalculator.DailyCount>,
    totalCount: Int,
    totalMinutes: Int,
    prevCount: Int,
    prevMinutes: Int,
) {
    val colors = LocalFlowColors.current
    val today = LocalDate.now()
    val maxCount = (bars.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)

    // "+X%" chip — compare current week minutes vs previous week same window.
    val trendPct = when {
        prevMinutes == 0 && totalMinutes > 0 -> 100
        prevMinutes == 0 -> 0
        else -> ((totalMinutes - prevMinutes) * 100.0 / prevMinutes).roundToInt()
    }
    val trendPositive = trendPct >= 0

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.history_weekly_section),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = totalCount.toString(),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Light,
                        ),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.history_week_summary, totalCount, TimeFormat.friendlyMinutes(totalMinutes)),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }
            if (trendPct != 0) {
                Text(
                    text = (if (trendPositive) "+" else "") + trendPct.toString() + "%",
                    color = if (trendPositive) colors.primary else colors.error,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 7-day bar chart: horizontal grid lines + 7 vertical bars + day labels.
        // Today's bar is rendered in the strong primary color; the rest are
        // in the soft primaryContainer so today visually pops.
        Row(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            bars.forEach { d ->
                val isToday = d.date == today
                val ratio = d.count.toFloat() / maxCount
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .height((ratio * 110).dp.coerceAtLeast(8.dp))
                            .width(18.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isToday -> colors.primary
                                    d.count == 0 -> colors.outlineVariant
                                    else -> colors.primaryContainer
                                }
                            ),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = weekdaySingleChar(d.date),
                        color = if (isToday) colors.onSurface else colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isToday) FontWeight.Medium else FontWeight.Normal,
                        ),
                    )
                }
            }
        }
    }
}

private fun weekdaySingleChar(d: LocalDate): String = when (d.dayOfWeek.value) {
    1 -> "一"
    2 -> "二"
    3 -> "三"
    4 -> "四"
    5 -> "五"
    6 -> "六"
    7 -> "日"
    else -> ""
}

@Composable
private fun RecentHeader() {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.history_recent_done),
            color = colors.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.history_see_all),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable { /* TODO: future */ },
        )
    }
}

@Composable
private fun RecentRow(s: SessionEntity) {
    val colors = LocalFlowColors.current
    val cat = Category.fromId(s.category)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(cat?.accent?.copy(alpha = 0.15f) ?: colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            if (cat != null) {
                Icon(cat.icon, contentDescription = null, tint = cat.accent, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(s.task, color = colors.onSurface, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(2.dp))
            val catName = cat?.let { stringResource(it.labelRes) } ?: ""
            val subtitle = if (catName.isNotEmpty()) "$catName · ${s.actualDuration} 分钟" else "${s.actualDuration} 分钟"
            Text(
                text = subtitle,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = DateUtils.formatTime(s.startedAt),
                color = colors.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = relativeLabel(s.startedAt),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun relativeLabel(startedAt: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = now - startedAt
    val minutes = diffMs / 60_000L
    if (minutes < 1) return "刚刚"
    if (minutes < 60) return "${minutes} 分钟前"

    val nowDate = LocalDate.now()
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    val startedDate = runCatching { LocalDate.parse(sdf.format(java.util.Date(startedAt))) }.getOrNull()
        ?: return ""

    return when {
        startedDate == nowDate -> "今天"
        startedDate == nowDate.minusDays(1) -> "昨天"
        else -> "${startedDate.monthValue}月${startedDate.dayOfMonth}日"
    }
}
