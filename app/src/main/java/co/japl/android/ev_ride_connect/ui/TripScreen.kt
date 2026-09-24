package co.japl.android.ev_ride_connect.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.com.japl.ui.components.HistoryRecordCard
import co.com.japl.ui.components.HistoryRecordData
import co.com.japl.ui.components.HistoryRecordType
import co.com.japl.ui.components.StatusCard
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.TripViewModel
import co.japl.android.ev_ride_connect.core.domain.BatteryMode
import co.japl.android.ev_ride_connect.core.domain.MotionState
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.domain.TripSummary
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import co.japl.android.ev_ride_connect.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    viewModel: TripViewModel,
    navigator: AppNavigator? = null,
    onTripClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isTripActive by viewModel.isTripActive.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val tripHistory by viewModel.tripHistory.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val tripSummary by viewModel.tripSummary.collectAsState()
    val showSummaryDialog by viewModel.showSummaryDialog.collectAsState()
    val elapsedTimeSeconds by viewModel.elapsedTimeSeconds.collectAsState()
    val gpsIntervalSeconds by viewModel.gpsIntervalSeconds.collectAsState()
    val showBatteryWarning by viewModel.showBatteryWarning.collectAsState()
    val currentDistance by viewModel.currentDistance.collectAsState()
    val currentAverageSpeed by viewModel.currentAverageSpeed.collectAsState()
    val sateliteMeterPrecision by viewModel.metersPrecisionSatelite.collectAsState()
    val sateliteCount by viewModel.sateliteCount.collectAsState()

    val showStartBatteryDialog by viewModel.showStartBatteryDialog.collectAsState()
    val showEndBatteryDialog by viewModel.showEndBatteryDialog.collectAsState()
    val latestBatteryLevel by viewModel.latestBatteryLevel.collectAsState()
    val calculatedNewKm by viewModel.calculatedNewKm.collectAsState()

    val activeSession by viewModel.activeSession.collectAsState()
    val evConfig = viewModel.evConfig.collectAsState().value
    val batteryMode = evConfig?.batteryMode ?: BatteryMode.PERCENTAGE

    val motionState = activeSession?.motionState ?: MotionState.STOPPED
    val motionStateText = when (motionState) {
        MotionState.MOVING -> stringResource(R.string.motion_moving)
        MotionState.ACCELERATING -> stringResource(R.string.motion_accelerating)
        MotionState.BRAKING -> stringResource(R.string.motion_braking)
        MotionState.STOPPED -> stringResource(R.string.motion_stopped)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.onStartTripRequested()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isTripActive) {
                StatusCard(
                    title = stringResource(R.string.trip_title),
                    isLoading = false,
                    statusMessage = motionStateText,
                    isSuccessStatus = !isPaused
                )

                ActiveTripPanel(
                    elapsedTimeSeconds = elapsedTimeSeconds,
                    currentDistance = currentDistance,
                    currentAverageSpeed = currentAverageSpeed,
                    isPaused = isPaused,
                    viewModel = viewModel
                )
            } else {
                ButtonTripStart(
                    isTripActive = false,
                    isPaused = false,
                    showBatteryWarning = showBatteryWarning,
                    permissionLauncher = permissionLauncher,
                    viewModel = viewModel
                )
            }

            GpsIntervalSelector(
                currentInterval = gpsIntervalSeconds,
                onIntervalSelected = { viewModel.setGpsInterval(it) }
            )

            if (showBatteryWarning) {
                ShowBatteryWarning()
            }
        }
    }

    if (showStartBatteryDialog) {
        TripBatteryDialog(
            title = stringResource(R.string.start_trip_battery_title),
            initialBattery = latestBatteryLevel,
            batteryMode = batteryMode,
            onDismiss = { viewModel.cancelStartTrip() },
            onConfirm = { batteryVal -> viewModel.confirmStartTrip(batteryVal) }
        )
    }

    if (showEndBatteryDialog) {
        TripBatteryDialog(
            title = stringResource(R.string.end_trip_battery_title),
            subtitle = "${stringResource(R.string.km_label)}: $calculatedNewKm ${stringResource(R.string.km_unit)}",
            initialBattery = latestBatteryLevel,
            batteryMode = batteryMode,
            onDismiss = { viewModel.cancelStopTrip() },
            onConfirm = { batteryVal -> viewModel.confirmStopTrip(batteryVal) }
        )
    }

    TripSummaryDialog(
        showSummaryDialog = showSummaryDialog,
        tripSummary = tripSummary,
        viewModel = viewModel
    )
}

@Composable
private fun ShowBatteryWarning(){
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = stringResource(R.string.trip_battery_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun TripSummaryDialog(
    showSummaryDialog: Boolean,
    tripSummary: TripSummary?,
    viewModel: TripViewModel
) {
    if (showSummaryDialog && tripSummary != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSummaryDialog() },
            title = {
                Text(
                    text = stringResource(R.string.trip_summary_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.trip_summary_distance),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%.2f", tripSummary.totalDistanceKm)} ${stringResource(R.string.km_unit)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.trip_summary_duration),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = DateUtils.formatDurationSeconds(tripSummary.totalDurationSeconds),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.trip_summary_avg_speed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%.1f", tripSummary.averageSpeedKmH)} ${stringResource(R.string.km_unit)}/h",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.trip_summary_battery_consumed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${tripSummary.batteryConsumedPercentage}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.trip_summary_gps_points),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${tripSummary.totalGpsLocationsCount}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissSummaryDialog() }) {
                    Text(stringResource(R.string.trip_summary_close))
                }
            }
        )
    }
}

@Composable
fun TripBatteryDialog(
    title: String,
    subtitle: String? = null,
    initialBattery: Short,
    batteryMode: BatteryMode = BatteryMode.PERCENTAGE,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var batteryInput by remember { mutableStateOf(initialBattery.toString()) }

    val labelRes = if (batteryMode == BatteryMode.VOLTAGE) R.string.enter_voltage else R.string.enter_battery
    val suffixRes = if (batteryMode == BatteryMode.VOLTAGE) R.string.voltage_postfix else R.string.battery_postfix

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedTextField(
                    value = batteryInput,
                    onValueChange = { input ->
                        if (batteryMode == BatteryMode.VOLTAGE) {
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
                    val battery = batteryInput.toDoubleOrNull() ?: 0.0
                    onConfirm(battery)
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
private fun ButtonTripStart(
    isTripActive: Boolean,
    isPaused: Boolean,
    showBatteryWarning: Boolean,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    viewModel: TripViewModel
){
    Button(
        onClick = {
            if (isTripActive) {
                viewModel.onStopTripRequested()
            } else {
                val permissions = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(permissions.toTypedArray())
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTripActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = if (isTripActive) stringResource(R.string.trip_end_button) else stringResource(R.string.trip_start_button),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActiveTripPanel(
    elapsedTimeSeconds: Long,
    currentDistance: Double,
    currentAverageSpeed: Double,
    isPaused: Boolean,
    viewModel: TripViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.trip_timer_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateUtils.formatDurationSeconds(elapsedTimeSeconds),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.trip_distance_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.2f", currentDistance)} ${stringResource(R.string.km_unit)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.trip_avg_speed_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", currentAverageSpeed)} ${stringResource(R.string.km_unit)}/h",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isPaused) viewModel.resumeTrip() else viewModel.pauseTrip()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isPaused) stringResource(R.string.trip_resume_button) else stringResource(R.string.trip_pause_button),
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.onStopTripRequested() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.trip_end_button),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GpsIntervalSelector(
    currentInterval: Long,
    onIntervalSelected: (Long) -> Unit
) {
    val intervals = listOf(5L, 10L, 30L, 60L, 120L)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.trip_gps_interval_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            intervals.forEachIndexed { index, interval ->
                SegmentedButton(
                    selected = interval == currentInterval,
                    onClick = { onIntervalSelected(interval) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = intervals.size)
                ) {
                    Text(stringResource(R.string.trip_interval_seconds, interval))
                }
            }
        }
    }
}