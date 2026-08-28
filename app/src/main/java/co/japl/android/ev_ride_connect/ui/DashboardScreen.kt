package co.japl.android.ev_ride_connect.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.unit.dp
import co.com.japl.ui.components.DualMetricCard
import co.com.japl.ui.components.MetricCard
import co.com.japl.ui.components.SegmentOption
import co.com.japl.ui.components.SegmentedButtonGroup
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import co.japl.android.ev_ride_connect.core.domain.ScooterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    navigator: AppNavigator? = null,
    modifier: Modifier = Modifier
) {
    val scooterState by viewModel.scooterState.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navigator?.navigateToTrip() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.nav_trip),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Button(
                onClick = { navigator?.navigateToEvConfig() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.nav_ev_config),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            MetricCard(
                value = "${scooterState.currentSpeed}",
                unit = stringResource(R.string.speed_unit)
            )

            DualMetricCard(
                primaryTitle = stringResource(R.string.battery_title),
                primaryValue = "${scooterState.batteryPercentage}%",
                secondaryTitle = stringResource(R.string.voltage_label),
                secondaryValue = "${scooterState.realtimeVoltage / 10.0} V"
            )

            DualMetricCard(
                primaryTitle = stringResource(R.string.odometer_title),
                primaryValue = "${scooterState.totalOdometer} ${stringResource(R.string.odometer_unit)}",
                secondaryTitle = "",
                secondaryValue = ""
            )

            ControlsCard(
                scooterState = scooterState,
                onToggleLock = { viewModel.toggleLock() },
                onToggleLight = { viewModel.toggleLight() },
                onSelectSpeedMode = { mode -> viewModel.setSpeedMode(mode) }
            )
        }
    }
}

@Composable
private fun ControlsCard(
    scooterState: ScooterState,
    onToggleLock: () -> Unit,
    onToggleLight: () -> Unit,
    onSelectSpeedMode: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.speed_mode_title),
                style = MaterialTheme.typography.titleMedium
            )

            val modeOptions = listOf(
                SegmentOption(1, stringResource(R.string.speed_mode_eco)),
                SegmentOption(2, stringResource(R.string.speed_mode_drive)),
                SegmentOption(3, stringResource(R.string.speed_mode_sport))
            )

            SegmentedButtonGroup(
                options = modeOptions,
                selectedOption = scooterState.speedMode,
                onOptionSelected = onSelectSpeedMode
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onToggleLock,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (scooterState.isLocked) {
                            stringResource(R.string.unlock_button)
                        } else {
                            stringResource(R.string.lock_button)
                        }
                    )
                }

                Button(
                    onClick = onToggleLight,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "${stringResource(R.string.light_title)}: ${
                            if (scooterState.isLightOn) {
                                stringResource(R.string.light_on)
                            } else {
                                stringResource(R.string.light_off)
                            }
                        }"
                    )
                }
            }
        }
    }
}
