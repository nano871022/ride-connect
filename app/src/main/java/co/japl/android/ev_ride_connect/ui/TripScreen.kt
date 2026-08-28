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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

    var menuExpanded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.startTrip()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trip_title)) },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Settings Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dashboard_title)) },
                                onClick = {
                                    menuExpanded = false
                                    navigator?.navigateToDashboard()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.nav_trip)) },
                                onClick = {
                                    menuExpanded = false
                                    navigator?.navigateToTrip()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ev_config_title)) },
                                onClick = {
                                    menuExpanded = false
                                    navigator?.navigateToEvConfig()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.llm_config_title)) },
                                onClick = {
                                    menuExpanded = false
                                    navigator?.navigateToLlmConfig()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.backup_title)) },
                                onClick = {
                                    menuExpanded = false
                                    navigator?.navigateToBackup()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ble_test_title)) },
                                onClick = {
                                    menuExpanded = false
                                    navigator?.navigateToBleTest()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.trip_timer_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = DateUtils.formatDurationSeconds(elapsedTimeSeconds),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    DualMetricCard(
                        primaryTitle = stringResource(R.string.trip_distance_label),
                        primaryValue = String.format(Locale.getDefault(), "%.2f km", currentDistance),
                        secondaryTitle = stringResource(R.string.trip_avg_speed_label),
                        secondaryValue = String.format(Locale.getDefault(), "%.1f km/h", currentAverageSpeed)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.trip_start_button),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.stopTrip() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.trip_stop_button),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.trip_gps_interval_label),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val intervalOptions = listOf(15L, 30L, 60L, 120L, 300L).map { sec ->
                        SegmentOption(sec, stringResource(R.string.trip_interval_seconds, sec.toInt()))
                    }

                    SegmentedChipGroup(
                        options = intervalOptions,
                        selectedOption = gpsIntervalSeconds,
                        onOptionSelected = { sec -> viewModel.setGpsInterval(sec) }
                    )

                    if (showBatteryWarning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Battery Warning",
                                tint = MaterialTheme.colorScheme.error
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

            Text(
                text = stringResource(R.string.trip_history_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (tripHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.trip_empty_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tripHistory) { trip ->
                        TripHistoryItem(
                            trip = trip,
                            onClick = {
                                viewModel.loadTripDetail(trip.id)
                                navigator?.navigateToTripDetail(trip.id)
                            }
                        )
                    }
                }
            }
        }
    }
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
