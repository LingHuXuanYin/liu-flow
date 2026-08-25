package com.liuflow.app.ui.rest

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuflow.app.R
import com.liuflow.app.timer.TimerController
import com.liuflow.app.ui.components.FocusRing
import com.liuflow.app.ui.components.RingCenter
import com.liuflow.app.ui.theme.LocalFlowColors
import com.liuflow.app.util.TimeFormat

@Composable
fun RestScreen(
    timerState: TimerController.State,
    onFinish: () -> Unit,
) {
    val colors = LocalFlowColors.current
    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.rest_title),
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.rest_subtitle),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                FocusRing(
                    progress = timerState.progress,
                    diameter = 320.dp,
                    strokeWidth = 12.dp,
                    progressColor = colors.outline,
                ) {
                    RingCenter(
                        statusLabel = "BREAK",
                        timeText = TimeFormat.mmss(timerState.remainingSeconds),
                        hint = null,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.primaryContainer)
                        .clickable { onFinish() }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.rest_finish),
                        color = colors.onPrimaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
