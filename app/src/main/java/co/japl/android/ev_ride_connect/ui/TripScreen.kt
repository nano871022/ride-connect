package co.japl.android.ev_ride_connect.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import co.com.japl.ui.components.DualMetricCard
import co.com.japl.ui.components.SegmentOption
import co.com.japl.ui.components.SegmentedChipGroup
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.TripViewModel
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import co.japl.android.ev_ride_connect.utils.DateUtils
import java.util.Locale


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import co.com.japl.ui.components.HistoryRecordCard
import co.com.japl.ui.components.HistoryRecordData
import co.com.japl.ui.components.HistoryRecordType
import co.com.japl.ui.components.MapHudCard
import co.com.japl.ui.components.TelemetryMetricsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    viewModel: TripViewModel,
    navigator: AppNavigator? = null,
    modifier: Modifier = Modifier
) {
    val isTripActive by viewModel.isTripActive.collectAsState()
    val elapsedTimeSeconds by viewModel.elapsedTimeSeconds.collectAsState()
    val gpsIntervalSeconds by viewModel.gpsIntervalSeconds.collectAsState()
    val showBatteryWarning by viewModel.showBatteryWarning.collectAsState()
    val tripHistory by viewModel.tripHistory.collectAsState()
    val currentDistance by viewModel.currentDistance.collectAsState()
    val currentAverageSpeed by viewModel.currentAverageSpeed.collectAsState()

    val showStartBatteryDialog by viewModel.showStartBatteryDialog.collectAsState()
    val showEndBatteryDialog by viewModel.showEndBatteryDialog.collectAsState()
    val latestBatteryLevel by viewModel.latestBatteryLevel.collectAsState()
    val calculatedNewKm by viewModel.calculatedNewKm.collectAsState()

    var leftMenuExpanded by remember { mutableStateOf(false) }
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var historyExpanded by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.onStartTripRequested()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trip_title)) },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { leftMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Navigation Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = leftMenuExpanded,
                            onDismissRequest = { leftMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dashboard_title)) },
                                onClick = {
                                    leftMenuExpanded = false
                                    navigator?.navigateToDashboard()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.nav_trip)) },
                                onClick = {
                                    leftMenuExpanded = false
                                    navigator?.navigateToTrip()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.nav_ev_data)) },
                                onClick = {
                                    leftMenuExpanded = false
                                    navigator?.navigateToEvData()
                                }
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { settingsMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = settingsMenuExpanded,
                            onDismissRequest = { settingsMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.llm_config_title)) },
                                onClick = {
                                    settingsMenuExpanded = false
                                    navigator?.navigateToLlmConfig()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ev_config_title)) },
                                onClick = {
                                    settingsMenuExpanded = false
                                    navigator?.navigateToEvConfig()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.backup_title)) },
                                onClick = {
                                    settingsMenuExpanded = false
                                    navigator?.navigateToBackup()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MapHudCard(
                    gpsIntervalSeconds = gpsIntervalSeconds,
                    onGpsIntervalSelected = { sec -> viewModel.setGpsInterval(sec) }
                )
            }

            item {
                TelemetryMetricsCard(
                    elapsedTimeFormatted = DateUtils.formatDurationSeconds(elapsedTimeSeconds),
                    currentDistanceValue = String.format(Locale.getDefault(), "%.2f", currentDistance),
                    currentSpeedValue = String.format(Locale.getDefault(), "%.1f", if (isTripActive) currentAverageSpeed * 1.25 else 0.0),
                    avgSpeedValue = String.format(Locale.getDefault(), "%.1f", currentAverageSpeed)
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isTripActive) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.trip_start_button),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.onStopTripRequested() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.trip_pause_button),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.trip_mark_poi_button),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.trip_sos_lock_button),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (showBatteryWarning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Battery Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.trip_battery_warning),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.trip_recent_history_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = { historyExpanded = !historyExpanded },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (historyExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle History",
                                    tint = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = historyExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            if (tripHistory.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.trip_empty_history),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    tripHistory.take(5).forEach { trip ->
                                        HistoryRecordCard(
                                            record = HistoryRecordData(
                                                id = trip.id.toString(),
                                                type = HistoryRecordType.RIDE,
                                                timestamp = DateUtils.formatTimestamp(trip.createTmst),
                                                subtitle = "Ruta Urban Connect",
                                                statusText = stringResource(R.string.history_status_completed),
                                                distanceValue = String.format(Locale.getDefault(), "%.2f", trip.distance),
                                                consumptionValue = "310 W",
                                                durationValue = DateUtils.formatDurationSeconds(trip.timeTrip),
                                                avgSpeedValue = String.format(Locale.getDefault(), "%.1f km/h", trip.averageSpeed)
                                            ),
                                            onViewTelemetryClick = {
                                                viewModel.loadTripDetail(trip.id)
                                                navigator?.navigateToTripDetail(trip.id)
                                            },
                                            viewTelemetryText = stringResource(R.string.trip_view_full_telemetry)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        if (showStartBatteryDialog) {
        TripBatteryDialog(
            title = stringResource(R.string.start_trip_battery_title),
            initialBattery = latestBatteryLevel,
            onDismiss = { viewModel.cancelStartTrip() },
            onConfirm = { batteryLevel -> viewModel.confirmStartTrip(batteryLevel) }
        )
    }

    if (showEndBatteryDialog) {
        TripBatteryDialog(
            title = stringResource(R.string.end_trip_battery_title),
            subtitle = "${stringResource(R.string.km_label)}: $calculatedNewKm ${stringResource(R.string.km_unit)}",
            initialBattery = latestBatteryLevel,
            onDismiss = { viewModel.cancelStopTrip() },
            onConfirm = { batteryLevel -> viewModel.confirmStopTrip(batteryLevel) }
        )
    }
}

@Composable
fun TripBatteryDialog(
    title: String,
    subtitle: String? = null,
    initialBattery: Short,
    onDismiss: () -> Unit,
    onConfirm: (Short) -> Unit
) {
    var batteryInput by remember { mutableStateOf(initialBattery.toString()) }

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
                    val battery = batteryInput.toShortOrNull() ?: 0
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
private fun TripHistoryItem(
    trip: Trip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = DateUtils.formatTimestamp(trip.createTmst),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = DateUtils.formatDurationSeconds(trip.timeTrip),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.trip_distance_label)}: ${String.format(Locale.getDefault(), "%.2f km", trip.distance)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${stringResource(R.string.trip_avg_speed_label)}: ${String.format(Locale.getDefault(), "%.1f km/h", trip.averageSpeed)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
