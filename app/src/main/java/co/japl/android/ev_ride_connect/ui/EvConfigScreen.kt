package co.japl.android.ev_ride_connect.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.EvConfigViewModel
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.navigation.AppNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvConfigScreen(
    viewModel: EvConfigViewModel,
    navigator: AppNavigator? = null,
    modifier: Modifier = Modifier
) {
    val evConfig by viewModel.evConfig.collectAsState()
    val isLoadingLlm by viewModel.isLoadingLlm.collectAsState()
    val llmErrorMessage by viewModel.llmErrorMessage.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isSearchDialogVisible by viewModel.isSearchDialogVisible.collectAsState()
    var leftMenuExpanded by remember { mutableStateOf(false) }
    var settingsMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ev_config_title)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AiRequestSection(
                request = evConfig.request,
                isLoadingLlm = isLoadingLlm,
                onRequestChanged = { viewModel.onRequestChanged(it) },
                onRequestAi = { viewModel.requestEvInfoFromLlm() }
            )

            GeneralSpecsSection(
                evConfig = evConfig,
                onBrandChanged = { viewModel.onBrandChanged(it) },
                onVersionChanged = { viewModel.onVersionChanged(it) },
                onManufactoryYearChanged = { viewModel.onManufactoryYearChanged(it) },
                onManufactoryCompanyChanged = { viewModel.onManufactoryCompanyChanged(it) },
                onBoughtDateChanged = { viewModel.onBoughtDateChanged(it) }
            )

            MotorsSection(
                motors = evConfig.motors,
                onAddMotor = { viewModel.onAddMotor("Motor ${evConfig.motors.size + 1}", 1000) },
                onUpdateMotor = { index, name, watts -> viewModel.onUpdateMotor(index, name, watts) },
                onRemoveMotor = { index -> viewModel.onRemoveMotor(index) }
            )

            BatterySection(
                evConfig = evConfig,
                onBatteryTechChanged = { viewModel.onBatteryTechnologyChanged(it) },
                onVoltsChanged = { viewModel.onBatteryVoltsChanged(it) },
                onAmpersChanged = { viewModel.onBatteryAmpersChanged(it) }
            )

            BrakesAndSuspensionSection(
                evConfig = evConfig,
                onBrakeQuantityChanged = { viewModel.onBrakeQuantityChanged(it) },
                onBrakeTechChanged = { viewModel.onBrakeTechnologyChanged(it) },
                onSuspensionTechChanged = { viewModel.onSuspensionTechnologyChanged(it) }
            )

            ChargingAndOtherSection(
                evConfig = evConfig,
                onChargePowerChanged = { viewModel.onChargePowerChanged(it) },
                onOtherCharacteristicsChanged = { viewModel.onOtherCharacteristicsChanged(it) }
            )

            val currentStatusMsg = statusMessage
            if (currentStatusMsg != null) {
                Text(
                    text = currentStatusMsg,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            ActionButtonsSection(
                isLoaded = evConfig.isLoaded,
                onSave = { viewModel.saveEvConfig() },
                onLoad = { viewModel.loadEv() }
            )
        }
    }

    if (isSearchDialogVisible) {
        EvSearchProgressDialog(
            isLoading = isLoadingLlm,
            errorMessage = llmErrorMessage,
            onRetry = { viewModel.requestEvInfoFromLlm() },
            onDismiss = { viewModel.dismissSearchDialog() }
        )
    }
}

@Composable
private fun AiRequestSection(
    request: String,
    isLoadingLlm: Boolean,
    onRequestChanged: (String) -> Unit,
    onRequestAi: () -> Unit
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
                text = stringResource(R.string.ev_request_label),
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = request,
                onValueChange = onRequestChanged,
                label = { Text(stringResource(R.string.ev_request_label)) },
                placeholder = { Text(stringResource(R.string.ev_request_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRequestAi,
                    enabled = !isLoadingLlm && request.isNotBlank()
                ) {
                    Text(stringResource(R.string.ev_request_ai_button))
                }
            }
        }
    }
}

@Composable
private fun EvSearchProgressDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = { Text(stringResource(R.string.ev_search_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    Text(stringResource(R.string.ev_search_dialog_processing))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else if (errorMessage != null) {
                    val errorText = when (errorMessage) {
                        "NO_ACTIVE_LLM_CONFIG" -> stringResource(R.string.ev_no_active_model)
                        "INVALID_API_KEY" -> stringResource(R.string.llm_validation_error)
                        else -> errorMessage
                    }
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            if (!isLoading && errorMessage != null) {
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.ev_search_retry_button))
                }
            }
        },
        dismissButton = {
            if (!isLoading) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        }
    )
}

@Composable
private fun GeneralSpecsSection(
    evConfig: EvConfig,
    onBrandChanged: (String) -> Unit,
    onVersionChanged: (String) -> Unit,
    onManufactoryYearChanged: (String) -> Unit,
    onManufactoryCompanyChanged: (String) -> Unit,
    onBoughtDateChanged: (String) -> Unit
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
                text = stringResource(R.string.ev_brand_label),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = evConfig.brand,
                    onValueChange = onBrandChanged,
                    label = { Text(stringResource(R.string.ev_brand_label)) },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = evConfig.version,
                    onValueChange = onVersionChanged,
                    label = { Text(stringResource(R.string.ev_version_label)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = evConfig.manufactoryYear,
                    onValueChange = onManufactoryYearChanged,
                    label = { Text(stringResource(R.string.ev_manufactory_year_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = evConfig.boughtDate,
                    onValueChange = onBoughtDateChanged,
                    label = { Text(stringResource(R.string.ev_bought_date_label)) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = evConfig.manufactoryCompany,
                onValueChange = onManufactoryCompanyChanged,
                label = { Text(stringResource(R.string.ev_manufactory_company_label)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MotorsSection(
    motors: List<co.japl.android.ev_ride_connect.core.domain.MotorSpec>,
    onAddMotor: () -> Unit,
    onUpdateMotor: (Int, String, Int) -> Unit,
    onRemoveMotor: (Int) -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ev_motors_title),
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedButton(onClick = onAddMotor) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.ev_add_motor))
                }
            }

            if (motors.isEmpty()) {
                Text(
                    text = stringResource(R.string.ev_motors_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                motors.forEachIndexed { index, motor ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = motor.name,
                            onValueChange = { newName -> onUpdateMotor(index, newName, motor.watts) },
                            label = { Text(stringResource(R.string.ev_motor_name_label)) },
                            modifier = Modifier.weight(1.2f)
                        )

                        OutlinedTextField(
                            value = if (motor.watts > 0) motor.watts.toString() else "",
                            onValueChange = { newWattsStr ->
                                val wattsInt = newWattsStr.toIntOrNull() ?: 0
                                onUpdateMotor(index, motor.name, wattsInt)
                            },
                            label = { Text(stringResource(R.string.ev_motor_watts_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { onRemoveMotor(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatterySection(
    evConfig: EvConfig,
    onBatteryTechChanged: (String) -> Unit,
    onVoltsChanged: (String) -> Unit,
    onAmpersChanged: (String) -> Unit
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
                text = stringResource(R.string.ev_battery_section),
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = evConfig.batteryTechnology,
                onValueChange = onBatteryTechChanged,
                label = { Text(stringResource(R.string.ev_battery_tech_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = evConfig.batteryVolts,
                    onValueChange = onVoltsChanged,
                    label = { Text(stringResource(R.string.ev_battery_volts_label)) },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = evConfig.batteryAmpers,
                    onValueChange = onAmpersChanged,
                    label = { Text(stringResource(R.string.ev_battery_ampers_label)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BrakesAndSuspensionSection(
    evConfig: EvConfig,
    onBrakeQuantityChanged: (Int) -> Unit,
    onBrakeTechChanged: (String) -> Unit,
    onSuspensionTechChanged: (String) -> Unit
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
                text = stringResource(R.string.ev_brakes_section),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = if (evConfig.brakeQuantity > 0) evConfig.brakeQuantity.toString() else "",
                    onValueChange = { str -> onBrakeQuantityChanged(str.toIntOrNull() ?: 0) },
                    label = { Text(stringResource(R.string.ev_brake_quantity_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = evConfig.brakeTechnology,
                    onValueChange = onBrakeTechChanged,
                    label = { Text(stringResource(R.string.ev_brake_tech_label)) },
                    modifier = Modifier.weight(1.5f)
                )
            }

            OutlinedTextField(
                value = evConfig.suspensionTechnology,
                onValueChange = onSuspensionTechChanged,
                label = { Text(stringResource(R.string.ev_suspension_tech_label)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ChargingAndOtherSection(
    evConfig: EvConfig,
    onChargePowerChanged: (String) -> Unit,
    onOtherCharacteristicsChanged: (String) -> Unit
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
                text = stringResource(R.string.ev_charging_section),
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = evConfig.chargePower,
                onValueChange = onChargePowerChanged,
                label = { Text(stringResource(R.string.ev_charge_power_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = evConfig.otherCharacteristics,
                onValueChange = onOtherCharacteristicsChanged,
                label = { Text(stringResource(R.string.ev_other_characteristics_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    isLoaded: Boolean,
    onSave: () -> Unit,
    onLoad: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoaded) {
                Text(
                    text = stringResource(R.string.ev_loaded_status),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.ev_save_button))
                }

                Button(
                    onClick = onLoad,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.ev_save_button))
                }
            }
        }
    }
}
