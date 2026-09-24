package co.japl.android.ev_ride_connect.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.com.japl.ui.components.MaintenanceBanner
import co.com.japl.ui.components.TelemetryBentoCard
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.core.domain.BatteryMode
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.navigation.AppNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    navigator: AppNavigator,
    modifier: Modifier = Modifier
) {
    val latestEvData by viewModel.latestEvData.collectAsState()
    val showApiKeyPrompt by viewModel.showApiKeyPrompt.collectAsState()
    val resumedSession by viewModel.resumedSession.collectAsState()

    val isTracking by viewModel.isTracking.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val evConfig = viewModel.evConfig.collectAsState().value
    val batteryMode = evConfig?.batteryMode ?: BatteryMode.PERCENTAGE

    var showUpdateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        DashboardContent(
            latestEvData = latestEvData,
            isTracking = isTracking,
            isPaused = isPaused,
            onManualInputClick = { showUpdateDialog = true },
            onStartTrackingClick = { navigator.navigateToTrip() },
            modifier = Modifier.padding(paddingValues)
        )
    }

    if (showUpdateDialog) {
        EvDataUpdateDialog(
            initialKm = latestEvData?.km ?: 0L,
            initialBatteryLevel = latestEvData?.batteryLevel ?: 0,
            batteryMode = batteryMode,
            onDismiss = { showUpdateDialog = false },
            onSave = { km, batteryValue ->
                viewModel.saveEvData(km, batteryValue)
                showUpdateDialog = false
            }
        )
    }

    if (showApiKeyPrompt) {
        StartupApiKeyDialog(
            onDismiss = { viewModel.dismissApiKeyPrompt() },
            onConfigureClick = {
                viewModel.dismissApiKeyPrompt()
                navigator.navigateToLlmConfig()
            }
        )
    }
}

@Composable
private fun DashboardContent(
    latestEvData: EvData?,
    isTracking: Boolean,
    isPaused: Boolean,
    onManualInputClick: () -> Unit,
    onStartTrackingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStartTrackingClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (isTracking) {
                    if (isPaused) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isTracking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = if (isTracking) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isTracking) {
                                if (isPaused) stringResource(R.string.trip_status_paused) else stringResource(R.string.trip_live_telemetry)
                            } else {
                                stringResource(R.string.trip_title)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if(isTracking) {
                            Text(
                                text = stringResource(R.string.scaffold_tracking_title),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Button(
                    onClick = onStartTrackingClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isTracking) stringResource(R.string.trip_live_telemetry) else stringResource(R.string.trip_start_button),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        TelemetryBentoCard(
            title = stringResource(R.string.trip_distance_label),
            titleIcon = Icons.Default.Timeline,
            accentColor = MaterialTheme.colorScheme.primary,
            value = (latestEvData?.km ?: 0L).toString(),
            unit = stringResource(R.string.km_unit),
            onEditClick = onManualInputClick,
            modifier = Modifier.fillMaxWidth()
        )

        MaintenanceBanner(
            title = "Maintenance",
            badgeText = "System Health",
            detailMessage = "All systems operational",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StartupApiKeyDialog(
    onDismiss: () -> Unit,
    onConfigureClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(stringResource(R.string.startup_api_key_title))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.startup_api_key_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfigureClick) {
                Text(stringResource(R.string.startup_api_key_configure_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

@Composable
fun EvDataUpdateDialog(
    initialKm: Long,
    initialBatteryLevel: Short,
    batteryMode: BatteryMode = BatteryMode.PERCENTAGE,
    onDismiss: () -> Unit,
    onSave: (Long, Double) -> Unit
) {
    val isVoltageMode = batteryMode == BatteryMode.VOLTAGE
    var kmInput by remember { mutableStateOf(initialKm.toString()) }
    var batteryInput by remember { mutableStateOf(if (isVoltageMode) "" else initialBatteryLevel.toString()) }

    val labelRes = if (isVoltageMode) R.string.enter_voltage else R.string.enter_battery
    val suffixRes = if (isVoltageMode) R.string.voltage_postfix else R.string.battery_postfix

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_ev_data_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = kmInput,
                    onValueChange = { kmInput = it.filter { char -> char.isDigit() } },
                    label = { Text(stringResource(R.string.enter_km)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = batteryInput,
                    onValueChange = { input ->
                        if (isVoltageMode) {
                            batteryInput = input.filter { char -> char.isDigit() || char == '.' }
                        } else {
                            val filtered = input.filter { char -> char.isDigit() }
                            val num = filtered.toIntOrNull()
                            if (filtered.isEmpty() || (num != null && num in 0..100)) {
                                batteryInput = filtered
                            }
                        }
                    },
                    label = { Text(stringResource(labelRes)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    suffix = { Text(stringResource(suffixRes)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = kmInput.toLongOrNull() ?: 0L
                    val battery = batteryInput.toDoubleOrNull() ?: 0.0
                    onSave(km, battery)
                }
            ) {
                Text(stringResource(R.string.save_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}
