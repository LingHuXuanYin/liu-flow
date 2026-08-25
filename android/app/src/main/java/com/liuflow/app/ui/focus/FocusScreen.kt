package com.liuflow.app.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuflow.app.R
import com.liuflow.app.data.model.Category
import com.liuflow.app.ui.components.FocusRing
import com.liuflow.app.ui.components.RingCenter
import com.liuflow.app.ui.theme.LocalFlowColors
import com.liuflow.app.util.DateUtils
import com.liuflow.app.util.TimeFormat

@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    onStart: () -> Unit,
) {
    val colors = LocalFlowColors.current
    val task by viewModel.taskInput.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    var showCustomDialog by remember { mutableStateOf(false) }

    val dateLine = "${DateUtils.formatWeekdayShort(java.time.LocalDate.now())} · ${DateUtils.formatDate(System.currentTimeMillis())}"

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // TopAppBar Medium-style header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = dateLine.uppercase(),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.focus_title_ready),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.NotificationsNone,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                // The ring is the primary start control: tap anywhere on the
                // 280dp circle to begin a focus session.
                FocusRing(
                    progress = 0f,
                    diameter = 280.dp,
                    strokeWidth = 12.dp,
                    modifier = Modifier.clickable { viewModel.prepareAndStart(onStart) },
                ) {
                    RingCenter(
                        statusLabel = stringResource(R.string.focus_label_status),
                        timeText = TimeFormat.mmss(duration * 60),
                        hint = stringResource(R.string.focus_label_hint),
                    )
                }

                Text(
                    text = "点击圆环开始",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )

                TaskInput(
                    value = task,
                    onChange = viewModel::setTask,
                )

                CategoryRow(
                    selected = category,
                    onSelect = viewModel::setCategory,
                )

                DurationRow(
                    selected = duration,
                    onCustomClick = { showCustomDialog = true },
                )
            }
    // Extended FAB — explicit start entry. Sits above the BottomNavBar (80dp)
    // and the system nav bar so it's never obscured.
    ExtendedFloatingActionButton(
        onClick = { viewModel.prepareAndStart(onStart) },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 24.dp, bottom = 96.dp),
        containerColor = colors.primary,
        contentColor = colors.onPrimary,
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.action_start),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
        )
    }
        }
    }

    if (showCustomDialog) {
        CustomDurationDialog(
            initial = duration,
            onConfirm = { newValue ->
                viewModel.setDuration(newValue)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false },
        )
    }

    }
}

@Composable
private fun TaskInput(value: String, onChange: (String) -> Unit) {
    val colors = LocalFlowColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.focus_input_task_label),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceContainer)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            if (value.isEmpty()) {
                Text(
                    text = stringResource(R.string.focus_input_task_hint),
                    color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = colors.onSurface, fontSize = 16.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CategoryRow(selected: Category?, onSelect: (Category?) -> Unit) {
    val colors = LocalFlowColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.focus_label_category),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) {
            items(Category.entries) { c ->
                CategoryChip(
                    label = stringResource(c.labelRes),
                    icon = c.icon,
                    active = selected == c,
                    onClick = { onSelect(if (selected == c) null else c) },
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalFlowColors.current
    val bg = if (active) colors.primaryContainer else colors.surface
    val fg = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant
    val border = if (active) colors.primary else colors.outlineVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (active) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = fg, modifier = Modifier.size(12.dp))
        }
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
        Text(label, color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun DurationRow(
    selected: Int,
    onCustomClick: () -> Unit,
) {
    val colors = LocalFlowColors.current
    val options = listOf(15, 25, 45, 60)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.focus_label_duration),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEach { v ->
                val active = selected == v
                val bg = if (active) colors.primaryContainer else colors.surface
                val fg = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant
                val border = if (active) colors.primaryContainer else colors.outlineVariant
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(12.dp))
                        .clickable { onCustomClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = v.toString(),
                        color = fg,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            // 自定义 chip - shows current value when not in standard set
            val isCustomSelected = selected !in options
            val bg = if (isCustomSelected) colors.primaryContainer else colors.surface
            val fg = if (isCustomSelected) colors.onPrimaryContainer else colors.onSurfaceVariant
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .border(1.dp, if (isCustomSelected) colors.primaryContainer else colors.outlineVariant, RoundedCornerShape(12.dp))
                    .clickable { onCustomClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isCustomSelected) "${selected}m" else stringResource(R.string.focus_duration_custom),
                    color = fg,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CustomDurationDialog(
    initial: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalFlowColors.current
    var value by remember { mutableFloatStateOf(initial.toFloat().coerceIn(5f, 90f)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.focus_custom_title)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.focus_custom_label, value.toInt()),
                    color = colors.onSurface,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 5f..90f,
                    steps = 84,  // 1-minute steps between 5 and 90
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.focus_custom_range_min), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Text(stringResource(R.string.focus_custom_range_max), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value.toInt()) }) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
