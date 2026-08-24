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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuflow.app.R
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
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
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

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card {
                    Text(
                        text = stringResource(R.string.weekly_total_minutes, s.totalMinutes),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Light,
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.weekly_total_count, s.totalCount) + "  ·  " +
                            stringResource(R.string.weekly_longest, s.longestMinutes),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Card {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        s.week.forEach { d ->
                            val ratio = d.minutes.toFloat() / max
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "${d.minutes}m",
                                    color = colors.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height((ratio * 100).dp.coerceAtLeast(4.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (d.minutes == 0) colors.outlineVariant else colors.primary),
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
        }
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
