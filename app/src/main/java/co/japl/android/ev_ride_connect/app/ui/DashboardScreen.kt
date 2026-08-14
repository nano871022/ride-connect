package co.japl.android.ev_ride_connect.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.japl.android.ev_ride_connect.app.R
import co.japl.android.ev_ride_connect.app.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.core.domain.ScooterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val scooterState by viewModel.scooterState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            SpeedCard(currentSpeed = scooterState.currentSpeed)

            BatteryAndVoltageCard(
                batteryPercentage = scooterState.batteryPercentage,
                voltage = scooterState.realtimeVoltage
            )

            OdometerCard(totalOdometer = scooterState.totalOdometer)

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
private fun SpeedCard(currentSpeed: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$currentSpeed",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.speed_unit),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun BatteryAndVoltageCard(batteryPercentage: Int, voltage: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.battery_title),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "$batteryPercentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.voltage_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${voltage / 10.0} V",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OdometerCard(totalOdometer: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.odometer_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$totalOdometer ${stringResource(R.string.odometer_unit)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    1 to stringResource(R.string.speed_mode_eco),
                    2 to stringResource(R.string.speed_mode_drive),
                    3 to stringResource(R.string.speed_mode_sport)
                )
                modes.forEach { (modeInt, modeLabel) ->
                    val isSelected = scooterState.speedMode == modeInt
                    if (isSelected) {
                        Button(
                            onClick = { onSelectSpeedMode(modeInt) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(modeLabel)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onSelectSpeedMode(modeInt) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(modeLabel)
                        }
                    }
                }
            }

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
