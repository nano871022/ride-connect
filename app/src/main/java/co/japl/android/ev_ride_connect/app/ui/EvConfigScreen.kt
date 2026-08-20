package co.japl.android.ev_ride_connect.app.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.japl.android.ev_ride_connect.app.R
import co.japl.android.ev_ride_connect.app.controller.EvConfigViewModel
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvConfigScreen(
    viewModel: EvConfigViewModel,
    modifier: Modifier = Modifier
) {
    val evConfig by viewModel.evConfig.collectAsState()
    val isLoadingLlm by viewModel.isLoadingLlm.collectAsState()
    val llmErrorMessage by viewModel.llmErrorMessage.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val activeLlmConfigs by viewModel.activeLlmConfigs.collectAsState()
    val selectedLlmConfig by viewModel.selectedLlmConfig.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ev_config_title)) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AiRequestSection(
                request = evConfig.request,
                activeLlmConfigs = activeLlmConfigs,
                selectedLlmConfig = selectedLlmConfig,
                isLoadingLlm = isLoadingLlm,
                llmErrorMessage = llmErrorMessage,
                onRequestChanged = { viewModel.onRequestChanged(it) },
                onSelectLlmConfig = { viewModel.onSelectLlmConfig(it) },
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
                onLoadAndConnect = { viewModel.loadEvAndConnectBle() }
            )
        }
    }
}

@Composable
private fun AiRequestSection(
    request: String,
    activeLlmConfigs: List<LlmConfig>,
    selectedLlmConfig: LlmConfig?,
    isLoadingLlm: Boolean,
    llmErrorMessage: String?,
    onRequestChanged: (String) -> Unit,
    onSelectLlmConfig: (LlmConfig) -> Unit,
    onRequestAi: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

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

            if (activeLlmConfigs.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedLlmConfig?.modelName ?: stringResource(R.string.llm_model_label),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.llm_model_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { dropdownExpanded = !dropdownExpanded }
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activeLlmConfigs.forEach { config ->
                            DropdownMenuItem(
                                text = { Text(config.modelName) },
                                onClick = {
                                    onSelectLlmConfig(config)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            val currentErrorMsg = llmErrorMessage
            if (currentErrorMsg != null) {
                Text(
                    text = currentErrorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoadingLlm) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }

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
    onLoadAndConnect: () -> Unit
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
                    onClick = onLoadAndConnect,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.ev_load_ble_button))
                }
            }
        }
    }
}
