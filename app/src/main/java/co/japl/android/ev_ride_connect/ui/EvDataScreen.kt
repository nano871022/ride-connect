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
    val records by viewModel.records.collectAsState()
    val maintenanceIndicators by viewModel.maintenanceIndicators.collectAsState()
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }

    val filterItems = listOf(
        FilterPillItem(HistoryFilter.ALL, stringResource(R.string.history_filter_all), Icons.Default.Tune),
        FilterPillItem(HistoryFilter.WEEK, stringResource(R.string.history_filter_week), Icons.Default.DateRange),
        FilterPillItem(HistoryFilter.MONTH, stringResource(R.string.history_filter_month), Icons.Default.CalendarToday),
        FilterPillItem(HistoryFilter.CHARGE, stringResource(R.string.history_filter_charge), Icons.Default.EvStation)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. Analytics Grid
        item {
           AnalyticsGrid(
               evDataList = evDataList
           )
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
           TelemetryChronology()
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
                    viewTelemetryText = stringResource(R.string.history_view_telemetry)
                )
            }
        } else {
            items(records) { record ->
                HistoryRecordCard(
                    record = record,
                    viewTelemetryText = stringResource(R.string.history_view_telemetry)
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
            FloatButtons()
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun AnalyticsGrid( evDataList: List<EvData>){
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

@Composable
private fun TelemetryChronology(){
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

@Composable
private fun FloatButtons(){
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
