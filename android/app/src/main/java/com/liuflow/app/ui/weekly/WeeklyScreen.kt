package com.liuflow.app.ui.weekly

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun WeeklyScreen(
    viewModel: WeeklyViewModel,
    onBack: () -> Unit,
) {
    val colors = LocalFlowColors.current
    val s by viewModel.state.collectAsStateWithLifecycle()
    val max = (s.week.maxOfOrNull { it.minutes } ?: 0).coerceAtLeast(1)

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                }
                Spacer(Modifier.weight(1f))
                Text(stringResource(R.string.weekly_title), color = colors.onSurface, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(40.dp))
            }

            // Cover
            CoverBlock(
                periodLabel = s.periodLabel,
                totalMinutes = s.totalMinutes,
                deltaMinutes = s.deltaMinutesVsPrev,
                topCategoryId = s.topCategoryId,
            )

            // 4 KPI 2x2
            FourKpiGrid(s)

            Spacer(Modifier.height(20.dp))

            // 7-day minutes bar chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceContainer)
                    .padding(20.dp),
            ) {
                Column {
                    Text("每日专注分钟", color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        s.week.forEachIndexed { idx, d ->
                            val ratio = d.minutes.toFloat() / max
                            val isToday = idx == s.week.lastIndex
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "${d.minutes}m",
                                    color = if (d.minutes == 0) Color.Transparent else colors.onSurface,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                )
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height((ratio * 100).dp.coerceAtLeast(4.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                d.minutes == 0 -> colors.outlineVariant.copy(alpha = 0.3f)
                                                isToday -> colors.primary
                                                else -> colors.primaryContainer
                                            }
                                        ),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = DateUtils.formatWeekdayShort(d.date).takeLast(1),
                                    color = if (isToday) colors.primary else colors.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Category donut
            CategoryDonutCard(s.categories)

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CoverBlock(
    periodLabel: String,
    totalMinutes: Int,
    deltaMinutes: Int,
    topCategoryId: String?,
) {
    val colors = LocalFlowColors.current
    val topCategory = topCategoryId?.let { id ->
        com.liuflow.app.data.model.Category.entries.firstOrNull { it.id == id }
    }
    val deltaText = when {
        deltaMinutes > 0 -> "比上周多专注了 ${TimeFormat.decimalHours(deltaMinutes)} 小时"
        deltaMinutes < 0 -> "比上周少专注了 ${TimeFormat.decimalHours(-deltaMinutes)} 小时"
        else -> "与上周持平"
    }
    val highlight = topCategory?.let { "${TimeFormat.friendlyMinutes(totalMinutes)} 偏${it.name}" } ?: TimeFormat.friendlyMinutes(totalMinutes)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.primary, colors.primary.copy(alpha = 0.75f)),
                ),
            )
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Column {
            Text(
                text = "$periodLabel · 本周报",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "本周你$deltaText",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Normal),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (topCategory != null)
                    "${topCategory.name} 是你的主旋律，$highlight"
                else
                    "总专注 $highlight",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun FourKpiGrid(s: WeeklyUiState) {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainer)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        KpiCell("本周次数", s.totalCount.toString(), null, modifier = Modifier.weight(1f))
        KpiCell("本周时长", TimeFormat.friendlyMinutes(s.totalMinutes), null, modifier = Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainer)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        KpiCell("最长单次", "${s.longestMinutes}m", null, modifier = Modifier.weight(1f))
        val completion = if (s.totalCount > 0) {
            // Use a rough completion ratio: totalCount is sessions, longestMinutes is the longest
            "${(s.totalMinutes.toFloat() / s.totalCount / s.longestMinutes.coerceAtLeast(1) * 100).toInt().coerceAtMost(100)}%"
        } else "0%"
        KpiCell("完成率", completion, null, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun KpiCell(label: String, value: String, trend: Float?, modifier: Modifier) {
    val colors = LocalFlowColors.current
    Column(modifier = modifier) {
        Text(label, color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            color = colors.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
            ),
        )
    }
}

/** 3-segment category donut showing the top 3 categories by minutes. */
@Composable
private fun CategoryDonutCard(cats: List<StatsCalculator.CategoryBreakdown>) {
    val colors = LocalFlowColors.current
    val total = cats.sumOf { it.minutes }.coerceAtLeast(1)
    val top3 = cats.take(3)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainer)
            .padding(20.dp),
    ) {
        Column {
            Text("分类分布", color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            if (top3.isEmpty()) {
                Text("本周还没有分类数据", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(112.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 14.dp.toPx()
                            val topLeft = Offset(stroke / 2, stroke / 2)
                            val arcSize = Size(size.width - stroke, size.height - stroke)
                            // Background ring
                            drawArc(
                                color = colors.surfaceContainerHigh,
                                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                                topLeft = topLeft, size = arcSize,
                                style = Stroke(width = stroke),
                            )
                            var startAngle = -90f
                            top3.forEach { c ->
                                val sweep = 360f * (c.minutes.toFloat() / total)
                                drawArc(
                                    color = c.category.accent,
                                    startAngle = startAngle, sweepAngle = sweep, useCenter = false,
                                    topLeft = topLeft, size = arcSize,
                                    style = Stroke(width = stroke),
                                )
                                startAngle += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${cats.sumOf { it.count }}",
                                color = colors.onSurface,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Light,
                                ),
                            )
                            Text("次", color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        top3.forEach { c ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(10.dp).clip(CircleShape).background(c.category.accent),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(c.category.labelRes),
                                    color = colors.onSurface,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "${(c.minutes / 60.0).format1()}h",
                                    color = colors.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun Double.format1(): String = String.format("%.1f", this)
