package co.japl.android.ev_ride_connect.ui

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import co.com.japl.ui.components.MaintenanceBanner
import co.com.japl.ui.components.TelemetryBentoCard
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.navigation.AppNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    navigator: AppNavigator? = null,
    modifier: Modifier = Modifier
) {
    val latestEvData by viewModel.latestEvData.collectAsState()
    val showApiKeyPrompt by viewModel.showApiKeyPrompt.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isRecordingTrip by remember { mutableStateOf(false) }
    val vehicles by viewModel.vehicles.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ButtonNavigateTrack(
            isRecordingTrip = isRecordingTrip,
            recordingTrip = { isRecordingTrip = it },
            navigator = navigator
        )

        vehicles.forEach {
            VehicleHeaderPod(
                onManualInputClick = { showUpdateDialog = true },
                onHistoryClick = { navigator?.navigateToEvData() },
                vehicleName = it.brand,
                modifier = Modifier
            )
        }

        Telemetry(
            latestEvData = latestEvData,
            showUpdateDialog = { showUpdateDialog = it }
        )

        MaintenanceBanner(
            title = stringResource(R.string.dashboard_preventive_maintenance),
            badgeText = stringResource(R.string.dashboard_ai_recommendation),
            detailMessage = stringResource(R.string.dashboard_maintenance_detail),
        )
    }

    if (showUpdateDialog) {
        EvDataUpdateDialog(
            initialKm = latestEvData?.km ?: 0L,
            initialBatteryLevel = latestEvData?.batteryLevel ?: 0,
            onDismiss = { showUpdateDialog = false },
            onSave = { km, batteryLevel ->
                viewModel.saveEvData(km, batteryLevel)
                showUpdateDialog = false
            }
        )
    }

    if (showApiKeyPrompt) {
        StartupApiKeyDialog(
            onConfigure = {
                viewModel.dismissApiKeyPrompt()
                navigator?.navigateToLlmConfig()
            },
            onDismiss = { viewModel.dismissApiKeyPrompt() }
        )
    }
}

@Composable
private fun VehicleHeaderPod(
    vehicleName: String,
    onManualInputClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = vehicleName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.dashboard_subtitle_manual),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onManualInputClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.dashboard_manual_input).uppercase(),
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.dashboard_gps_phone),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00F0FF)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clickable { onHistoryClick() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.dashboard_history),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartupApiKeyDialog(
    onConfigure: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val videoUrl = "https://www.youtube.com/watch?v=vkX6XTxZBbk"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.startup_api_key_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.startup_api_key_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.startup_api_key_video_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = videoUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, videoUrl.toUri())
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfigure) {
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
    onDismiss: () -> Unit,
    onSave: (Long, Short) -> Unit
) {
    var kmInput by remember { mutableStateOf(initialKm.toString()) }
    var batteryInput by remember { mutableStateOf(initialBatteryLevel.toString()) }

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
                    onValueChange = {
                        val filtered = it.filter { char -> char.isDigit() }
                        val num = filtered.toIntOrNull()
                        if (filtered.isEmpty() || (num != null && num in 0..100)) {
                            batteryInput = filtered
                        }
                    },
                    label = { Text(stringResource(R.string.enter_battery)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    suffix = { Text(stringResource(R.string.battery_postfix)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = kmInput.toLongOrNull() ?: 0L
                    val battery = batteryInput.toShortOrNull() ?: 0
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

@Composable
private fun ButtonNavigateTrack(
    isRecordingTrip: Boolean,
    recordingTrip: (Boolean) -> Unit,
    navigator: AppNavigator? = null
){
    val buttonBgColor by animateColorAsState(
        targetValue = if (isRecordingTrip) Color(0xFFFF4081) else Color(0xFF00F0FF),
        label = "RecordButtonColor"
    )
    Button(
        onClick = {
            recordingTrip.invoke(!isRecordingTrip)
            navigator?.navigateToTrip()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonBgColor,
            contentColor = Color(0xFF002022)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (isRecordingTrip) {
                    stringResource(R.string.dashboard_recording)
                } else {
                    stringResource(R.string.dashboard_record_trip_gps)
                }.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun Telemetry(
    latestEvData: EvData?,
    showUpdateDialog: (Boolean) -> Unit,
    modifier: Modifier = Modifier
){
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Battery Card
            val batteryLevel = latestEvData?.batteryLevel?.toInt() ?: 43
            TelemetryBentoCard(
                title = stringResource(R.string.dashboard_battery_tag),
                titleIcon = Icons.Default.Info,
                accentColor = Color(0xFF34FF8C),
                value = batteryLevel.toString(),
                unit = stringResource(R.string.battery_postfix),
                progress = batteryLevel / 100f,
                onEditClick = { showUpdateDialog.invoke( true ) },
                statusRows = listOf(
                    stringResource(R.string.dashboard_battery_record) to stringResource(R.string.dashboard_battery_manual),
                    stringResource(R.string.dashboard_battery_updated) to "Hoy, 09:30"
                ),
                modifier = Modifier.weight(1f)
            )

            // 2. Last Charge Card
            TelemetryBentoCard(
                title = stringResource(R.string.dashboard_last_charge),
                titleIcon = Icons.Default.Info,
                accentColor = Color(0xFF00F0FF),
                value = "52",
                unit = "V",
                subtitle = stringResource(R.string.dashboard_est_full_charge),
                statusRows = listOf(
                    stringResource(R.string.dashboard_cycles) to stringResource(R.string.dashboard_cycles_est, 48),
                    stringResource(R.string.dashboard_pack_health) to stringResource(R.string.dashboard_pack_health_optimal)
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 3. Estimated Consumption
            TelemetryBentoCard(
                title = stringResource(R.string.dashboard_consumption),
                titleIcon = Icons.Default.Build,
                accentColor = Color(0xFFDDB7FF),
                value = "18.2",
                unit = stringResource(R.string.dashboard_consumption_unit),
                subtitle = stringResource(R.string.dashboard_calculated),
                showSparkline = true,
                footerBadge = null,
                modifier = Modifier.weight(1f)
            )

            // 4. Odometer Card
            val kmValue = latestEvData?.km ?: 143L
            TelemetryBentoCard(
                title = stringResource(R.string.odometer_title),
                titleIcon = Icons.Default.LocationOn,
                accentColor = Color(0xFF00F0FF),
                value = kmValue.toString(),
                unit = stringResource(R.string.km_unit),
                subtitle = stringResource(R.string.dashboard_odometer_manual),
                onEditClick = { showUpdateDialog.invoke( true ) },
                footerBadge = stringResource(R.string.dashboard_last_record) to "Ayer (+12.4 km)",
                modifier = Modifier.weight(1f)
            )
        }
    }
}