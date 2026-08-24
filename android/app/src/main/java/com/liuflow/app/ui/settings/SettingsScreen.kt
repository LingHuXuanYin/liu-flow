package com.liuflow.app.ui.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.liuflow.app.data.model.DarkMode
import com.liuflow.app.data.model.FlowTheme
import com.liuflow.app.ui.theme.LocalFlowColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
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
                Text(stringResource(R.string.settings_title), color = colors.onSurface, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(40.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { SectionHeader(stringResource(R.string.settings_theme)) }
                item {
                    ThemeGrid(
                        selected = s.theme,
                        onSelect = viewModel::setTheme,
                    )
                }

                item { SectionHeader(stringResource(R.string.settings_dark_mode)) }
                item {
                    SegmentedRow(
                        options = DarkMode.entries,
                        label = { it.labelRes },
                        selected = s.darkMode,
                        onSelect = viewModel::setDarkMode,
                    )
                }

                item { SectionHeader(stringResource(R.string.settings_daily_target)) }
                item {
                    ValueSlider(
                        value = s.dailyTarget.toFloat(),
                        range = 1f..12f,
                        steps = 10,
                        valueLabel = stringResource(R.string.settings_daily_target_value, s.dailyTarget),
                        onChange = { viewModel.setDailyTarget(it.toInt()) },
                    )
                }

                item { SectionHeader(stringResource(R.string.settings_default_duration)) }
                item {
                    ValueSlider(
                        value = s.defaultFocusMinutes.toFloat(),
                        range = 5f..90f,
                        steps = 16,
                        valueLabel = stringResource(R.string.settings_default_duration_value, s.defaultFocusMinutes),
                        onChange = { viewModel.setDefaultFocus(it.toInt()) },
                    )
                }

                item { SectionHeader(stringResource(R.string.settings_rest_duration)) }
                item {
                    ValueSlider(
                        value = s.restMinutes.toFloat(),
                        range = 1f..15f,
                        steps = 13,
                        valueLabel = stringResource(R.string.settings_rest_duration_value, s.restMinutes),
                        onChange = { viewModel.setRest(it.toInt()) },
                    )
                }

                item { SectionHeader(stringResource(R.string.settings_sound)) }
                item { SwitchRow(label = stringResource(R.string.settings_sound), checked = s.soundEnabled, onChange = viewModel::setSound) }
                item { SwitchRow(label = stringResource(R.string.settings_vibrate), checked = s.vibrateEnabled, onChange = viewModel::setVibrate) }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    val colors = LocalFlowColors.current
    Text(
        text = text,
        color = colors.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun ThemeGrid(selected: FlowTheme, onSelect: (FlowTheme) -> Unit) {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FlowTheme.entries.forEach { t ->
            val active = t == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) colors.primaryContainer else colors.surfaceContainer)
                    .clickable { onSelect(t) }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(themeSwatchColor(t.id)),
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = stringResource(t.labelRes),
                    color = if (active) colors.onPrimaryContainer else colors.onSurface,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                )
                if (active) {
                    Spacer(Modifier.size(2.dp))
                    Icon(Icons.Filled.Check, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

private fun themeSwatchColor(id: String): androidx.compose.ui.graphics.Color = when (id) {
    "night" -> androidx.compose.ui.graphics.Color(0xFF4A6FA5)
    "forest" -> androidx.compose.ui.graphics.Color(0xFF2D6A4F)
    "twilight" -> androidx.compose.ui.graphics.Color(0xFF7A4E7C)
    else -> androidx.compose.ui.graphics.Color(0xFF6750A4)
}

@Composable
private fun SegmentedRow(
    options: List<DarkMode>,
    label: (DarkMode) -> Int,
    selected: DarkMode,
    onSelect: (DarkMode) -> Unit,
) {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainer)
            .padding(4.dp),
    ) {
        options.forEach { opt ->
            val active = opt == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (active) colors.primary else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onSelect(opt) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(label(opt)),
                    color = if (active) colors.onPrimary else colors.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ValueSlider(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String,
    onChange: (Float) -> Unit,
) {
    val colors = LocalFlowColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainer)
            .padding(16.dp),
    ) {
        Text(
            text = valueLabel,
            color = colors.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Light,
            ),
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            steps = steps,
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = colors.onSurface, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
