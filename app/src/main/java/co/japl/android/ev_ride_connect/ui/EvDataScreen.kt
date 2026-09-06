package co.japl.android.ev_ride_connect.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import co.com.japl.ui.components.AnalyticsStatCard
import co.com.japl.ui.components.FilterPillGroup
import co.com.japl.ui.components.FilterPillItem
import co.com.japl.ui.components.HistoryRecordCard
import co.com.japl.ui.components.HistoryRecordData
import co.com.japl.ui.components.HistoryRecordType
import co.com.japl.ui.components.MaintenanceHealthCard
import co.com.japl.ui.components.MaintenanceIndicatorItem
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.EvDataViewModel
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import co.japl.android.ev_ride_connect.utils.DateUtils

enum class HistoryFilter {
    ALL, WEEK, MONTH, CHARGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvDataScreen(
    viewModel: EvDataViewModel,
    navigator: AppNavigator? = null,
    modifier: Modifier = Modifier
) {
    val evDataList by viewModel.evDataList.collectAsState()
    var leftMenuExpanded by remember { mutableStateOf(false) }
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }

    val filterItems = listOf(
        FilterPillItem(HistoryFilter.ALL, stringResource(R.string.history_filter_all), Icons.Default.Tune),
        FilterPillItem(HistoryFilter.WEEK, stringResource(R.string.history_filter_week), Icons.Default.DateRange),
        FilterPillItem(HistoryFilter.MONTH, stringResource(R.string.history_filter_month), Icons.Default.CalendarToday),
        FilterPillItem(HistoryFilter.CHARGE, stringResource(R.string.history_filter_charge), Icons.Default.EvStation)
    )

    val sampleRecords = listOf(
        HistoryRecordData(
            id = "1",
            type = HistoryRecordType.RIDE,
            timestamp = "2026-09-05 17:56",
            subtitle = stringResource(R.string.history_active_session_ended),
            statusText = stringResource(R.string.history_status_completed),
            distanceValue = "143.0",
            distanceUnit = "km",
            consumptionValue = "1.2 kWh",
            batteryValue = "43%",
            durationValue = "32m",
            avgSpeedValue = "24.6 km/h"
        ),
        HistoryRecordData(
            id = "2",
            type = HistoryRecordType.DIAGNOSTIC,
            timestamp = "2026-09-03 19:20",
            subtitle = stringResource(R.string.history_internal_workshop),
            distanceValue = "0",
            batteryValue = "43%",
            operationValue = stringResource(R.string.history_static_calibration)
        ),
        HistoryRecordData(
            id = "3",
            type = HistoryRecordType.CHARGING,
            timestamp = "2026-09-03 19:09",
            subtitle = stringResource(R.string.history_domestic_charge),
            distanceValue = "0",
            batteryValue = "46%",
            chargeDeltaValue = stringResource(R.string.history_top_off)
        )
    )

    val maintenanceIndicators = listOf(
        MaintenanceIndicatorItem(
            title = stringResource(R.string.history_brake_pads),
            icon = Icons.Default.Build,
            iconTint = MaterialTheme.colorScheme.primary,
            progress = 0.78f,
            percentageText = "78%",
            progressColor = MaterialTheme.colorScheme.primaryContainer,
            subTextRight = stringResource(R.string.history_next_inspection)
        ),
        MaintenanceIndicatorItem(
            title = stringResource(R.string.history_tires),
            icon = Icons.Default.Place,
            iconTint = MaterialTheme.colorScheme.secondaryContainer,
            progress = 0.85f,
            percentageText = "85%",
            progressColor = MaterialTheme.colorScheme.secondaryContainer,
            subTextRight = stringResource(R.string.history_recommended_pressure)
        ),
        MaintenanceIndicatorItem(
            title = stringResource(R.string.history_battery_cycles),
            icon = Icons.Default.Refresh,
            iconTint = MaterialTheme.colorScheme.tertiary,
            progress = 0.028f,
            percentageText = "14 / 500",
            progressColor = MaterialTheme.colorScheme.tertiaryContainer,
            subTextLeft = stringResource(R.string.history_degradation_est),
            subTextRight = stringResource(R.string.history_remaining_life)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "VOLTRIDE EV",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.history_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        CircleShape
                                    )
                            )
                            Text(
                                text = "VSETT C7+ • ${stringResource(R.string.history_connected)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

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
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Analytics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsStatCard(
                            title = stringResource(R.string.history_total_odometer),
                            value = if (evDataList.isNotEmpty()) evDataList.first().km.toString() else "143.0",
                            unit = stringResource(R.string.km_unit),
                            icon = Icons.Default.Place,
                            modifier = Modifier.weight(1f),
                            subText = stringResource(R.string.history_today_km),
                            iconTint = MaterialTheme.colorScheme.primaryContainer,
                            valueColor = MaterialTheme.colorScheme.primary
                        )

                        AnalyticsStatCard(
                            title = stringResource(R.string.history_trips),
                            value = "18",
                            unit = stringResource(R.string.history_routes_unit),
                            icon = Icons.Default.SportsScore,
                            modifier = Modifier.weight(1f),
                            subIcon = Icons.Default.CalendarToday,
                            subText = stringResource(R.string.history_cloud_sync),
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsStatCard(
                            title = stringResource(R.string.history_efficiency),
                            value = "17.5",
                            unit = stringResource(R.string.history_efficiency_unit),
                            icon = Icons.Default.Eco,
                            modifier = Modifier.weight(1f),
                            subIcon = Icons.Default.Eco,
                            subText = stringResource(R.string.history_mode_sport_eco),
                            iconTint = MaterialTheme.colorScheme.secondaryContainer,
                            valueColor = MaterialTheme.colorScheme.secondaryContainer,
                            subTextColor = MaterialTheme.colorScheme.secondaryContainer
                        )

                        AnalyticsStatCard(
                            title = stringResource(R.string.history_current_battery),
                            value = if (evDataList.isNotEmpty()) evDataList.first().batteryLevel.toString() else "43",
                            unit = stringResource(R.string.history_battery_unit),
                            icon = Icons.Default.BatteryChargingFull,
                            modifier = Modifier.weight(1f),
                            subIcon = Icons.Default.BatteryChargingFull,
                            subText = stringResource(R.string.history_voltage_stable),
                            iconTint = MaterialTheme.colorScheme.tertiary,
                            valueColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // 2. Filter Pills
            item {
                FilterPillGroup(
                    items = filterItems,
                    selectedItemId = selectedFilter,
                    onItemSelected = { selectedFilter = it }
                )
            }

            // 3. Telemetry Chronology Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.history_recent_records),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.history_order_chronological),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. Chronology Records
            if (evDataList.isNotEmpty()) {
                items(evDataList) { evData ->
                    HistoryRecordCard(
                        record = HistoryRecordData(
                            id = "${evData.evCode}",
                            type = HistoryRecordType.RIDE,
                            timestamp = DateUtils.formatTimestamp(evData.createTmst),
                            subtitle = stringResource(R.string.history_active_session_ended),
                            statusText = stringResource(R.string.history_status_completed),
                            distanceValue = evData.km.toString(),
                            distanceUnit = stringResource(R.string.km_unit),
                            consumptionValue = "1.2 kWh",
                            batteryValue = "${evData.batteryLevel}%",
                            durationValue = "32m",
                            avgSpeedValue = "24.6 km/h"
                        ),
                        viewTelemetryText = stringResource(R.string.history_view_telemetry),
                        gpxText = stringResource(R.string.history_gpx)
                    )
                }
            } else {
                items(sampleRecords) { record ->
                    HistoryRecordCard(
                        record = record,
                        viewTelemetryText = stringResource(R.string.history_view_telemetry),
                        gpxText = stringResource(R.string.history_gpx)
                    )
                }
            }

            // 5. Lifespan & Maintenance Section
            item {
                MaintenanceHealthCard(
                    title = stringResource(R.string.history_maintenance_title),
                    statusBadgeText = stringResource(R.string.history_optimal_health),
                    indicators = maintenanceIndicators
                )
            }

            // 6. Action Floating Button Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Button(
                        onClick = { /* Action to log manual event */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.history_log_event),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
