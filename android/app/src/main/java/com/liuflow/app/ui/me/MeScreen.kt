package com.liuflow.app.ui.me

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuflow.app.R
import com.liuflow.app.data.export.DataExporter
import com.liuflow.app.ui.theme.LocalFlowColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MeScreen(
    viewModel: MeViewModel,
    onOpenSettings: () -> Unit,
) {
    val colors = LocalFlowColors.current
    val s by viewModel.settingsState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showClearConfirm by remember { mutableStateOf(false) }

    val jsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val sessions = (context.applicationContext as com.liuflow.app.FlowApp)
                        .container.flowRepository.observeAll().first()
                    DataExporter.writeJson(context, uri, sessions)
                }
            }
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val sessions = (context.applicationContext as com.liuflow.app.FlowApp)
                        .container.flowRepository.observeAll().first()
                    DataExporter.writeCsv(context, uri, sessions)
                }
            }
        }
    }

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
                Text("ME", color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.me_title), color = colors.onSurface, style = MaterialTheme.typography.headlineMedium)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SectionHeader(stringResource(R.string.me_section_personalize))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainer)
                        .clickable(onClick = onOpenSettings)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = colors.onSurfaceVariant)
                    Spacer(Modifier.size(12.dp))
                    Text(stringResource(R.string.me_settings), color = colors.onSurface, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
                }

                Spacer(Modifier.height(8.dp))
                SectionHeader(stringResource(R.string.me_section_data))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainer)
                        .clickable { jsonLauncher.launch("flow-${System.currentTimeMillis() / 1000}.json") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.DataObject, contentDescription = null, tint = colors.onSurfaceVariant)
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.me_export_json), color = colors.onSurface, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.me_export_json_sub),
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainer)
                        .clickable { csvLauncher.launch("flow-${System.currentTimeMillis() / 1000}.csv") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.TableView, contentDescription = null, tint = colors.onSurfaceVariant)
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.me_export_csv), color = colors.onSurface, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.me_export_csv_sub),
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainer)
                        .clickable { showClearConfirm = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = colors.error)
                    Spacer(Modifier.size(12.dp))
                    Text(stringResource(R.string.me_clear_data), color = colors.error, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
                }

                Spacer(Modifier.height(8.dp))
                SectionHeader(stringResource(R.string.me_section_about))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainer)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = colors.onSurfaceVariant)
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.me_about), color = colors.onSurface, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.me_version, viewModel.version),
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.confirm_clear_data_title)) },
            text = { Text(stringResource(R.string.confirm_clear_data_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    viewModel.clearAllData()
                }) {
                    Text(stringResource(R.string.action_confirm), color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    val colors = LocalFlowColors.current
    Text(
        text = text,
        color = colors.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
    )
}
