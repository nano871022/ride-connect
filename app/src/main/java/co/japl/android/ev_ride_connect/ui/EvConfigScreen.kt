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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import co.com.japl.ui.components.ConfigSectionCard
import co.com.japl.ui.components.SpecTile
import co.com.japl.ui.components.VehicleImageCard
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.EvConfigViewModel
import co.japl.android.ev_ride_connect.core.domain.BatteryMode
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.MotorSpec
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

    val snackbarHostState = remember { SnackbarHostState() }

    val savedSuccess = stringResource(R.string.ev_save_button)
    val loadedSuccess = stringResource(R.string.ev_loaded_status)
    val llmSuccess = stringResource(R.string.ev_sync_banner_text)

    LaunchedEffect(statusMessage) {
        when (statusMessage) {
            "CONFIG_SAVED" -> snackbarHostState.showSnackbar(savedSuccess)
            "EV_LOADED" -> snackbarHostState.showSnackbar(loadedSuccess)
            "LLM_FETCH_SUCCESS" -> snackbarHostState.showSnackbar(llmSuccess)
        }
        if (statusMessage != null) {
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { viewModel.onPrepareNewVehicle() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.ev_add_vehicle_button)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        EvConfigContent(
            evConfig = evConfig,
            viewModel = viewModel,
            modifier = Modifier.padding(paddingValues)
        )
    }

    if (isSearchDialogVisible) {
        val promptTemplate = stringResource(R.string.ev_llm_prompt_template, evConfig.request)
        EvSearchProgressDialog(
            isLoading = isLoadingLlm,
            errorMessage = llmErrorMessage,
            onRetry = {
                viewModel.requestEvInfoFromLlm(promptTemplate)
            },
            onDismiss = { viewModel.dismissSearchDialog() }
        )
    }
}

@Composable
private fun EvConfigContent(
    evConfig: EvConfig,
    viewModel: EvConfigViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VehicleImageCard(
            imageUrl = evConfig.imageUrl,
            title = evConfig.brand,
            description = evConfig.version,
            addButtonText = stringResource(R.string.ev_save_button),
            onAddClick = { viewModel.saveEvConfig() }
        )

        AiAssistantHeaderSection(
            requestText = evConfig.request,
            onRequestChanged = { viewModel.onRequestChanged(it) },
            viewModel = viewModel
        )

        GeneralInfoSection(
            evConfig = evConfig,
            onBrandChanged = { viewModel.onBrandChanged(it) },
            onVersionChanged = { viewModel.onVersionChanged(it) },
            onYearChanged = { viewModel.onManufactoryYearChanged(it) },
            onCompanyChanged = { viewModel.onManufactoryCompanyChanged(it) },
            onBoughtDateChanged = { viewModel.onBoughtDateChanged(it) }
        )

        MotorsSection(
            motors = evConfig.motors,
            onAddMotor = { name, watts -> viewModel.onAddMotor(name, watts) },
            onUpdateMotor = { index, name, watts -> viewModel.onUpdateMotor(index, name, watts) },
            onRemoveMotor = { index -> viewModel.onRemoveMotor(index) }
        )

        BatterySpecsSection(
            evConfig = evConfig,
            onBatteryTechChanged = { viewModel.onBatteryTechnologyChanged(it) },
            onVoltsChanged = { viewModel.onBatteryVoltsChanged(it) },
            onAmpersChanged = { viewModel.onBatteryAmpersChanged(it) },
            onBatteryModeChanged = { viewModel.onBatteryModeChanged(it) },
            onMaxVoltageChanged = { viewModel.onMaxVoltageChanged(it) },
            onMinVoltageChanged = { viewModel.onMinVoltageChanged(it) }
        )

        BrakesAndSuspensionSection(
            evConfig = evConfig,
            onBrakeQuantityChanged = { viewModel.onBrakeQuantityChanged(it) },
            onBrakeTechChanged = { viewModel.onBrakeTechnologyChanged(it) },
            onSuspensionTechChanged = { viewModel.onSuspensionTechnologyChanged(it) }
        )

        ChargingAndErgonomicsSection(
            evConfig = evConfig,
            onChargePowerChanged = { viewModel.onChargePowerChanged(it) },
            onOtherCharacteristicsChanged = { viewModel.onOtherCharacteristicsChanged(it) }
        )

        TelemetryBannerSection()

        BottomActionTray(
            isLoaded = evConfig.isLoaded,
            onDiscard = { viewModel.loadSavedConfig() },
            onSave = { viewModel.saveEvConfig() }
        )
    }
}

@Composable
private fun AiAssistantHeaderSection(
    requestText: String,
    onRequestChanged: (String) -> Unit,
    viewModel: EvConfigViewModel
) {
    val activeConfigs by viewModel.activeLlmConfigs.collectAsState()
    val selectedConfig by viewModel.selectedLlmConfig.collectAsState()
    var isDropdownExpanded by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.ev_ai_assistant_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.ev_official_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = stringResource(R.string.ev_ai_assistant_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (activeConfigs.isNotEmpty()) {
                Box {
                    OutlinedButton(
                        onClick = { isDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedConfig?.let { "${it.modelName} (${it.selectedVersion})" }
                                    ?: stringResource(R.string.llm_ai_provider_label),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        activeConfigs.forEach { config ->
                            DropdownMenuItem(
                                text = { Text("${config.modelName} - ${config.selectedVersion}") },
                                onClick = {
                                    viewModel.onSelectLlmConfig(config)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = requestText,
                onValueChange = onRequestChanged,
                label = { Text(stringResource(R.string.ev_request_label)) },
                placeholder = { Text(stringResource(R.string.ev_request_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            val promptTemplate = stringResource(R.string.ev_llm_prompt_template, requestText)

            Button(
                onClick = { viewModel.requestEvInfoFromLlm(promptTemplate) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ev_request_ai_button),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GeneralInfoSection(
    evConfig: EvConfig,
    onBrandChanged: (String) -> Unit,
    onVersionChanged: (String) -> Unit,
    onYearChanged: (String) -> Unit,
    onCompanyChanged: (String) -> Unit,
    onBoughtDateChanged: (String) -> Unit
) {
    ConfigSectionCard(
        title = stringResource(R.string.ev_config_title),
        icon = Icons.Default.ElectricScooter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = evConfig.manufactoryYear,
                onValueChange = onYearChanged,
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
            onValueChange = onCompanyChanged,
            label = { Text(stringResource(R.string.ev_manufactory_company_label)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MotorsSection(
    motors: List<MotorSpec>,
    onAddMotor: (String, Int) -> Unit,
    onUpdateMotor: (Int, String, Int) -> Unit,
    onRemoveMotor: (Int) -> Unit
) {
    val totalWatts = motors.sumOf { it.watts }

    ConfigSectionCard(
        title = stringResource(R.string.ev_motors_power_title),
        icon = Icons.Default.Bolt,
        actionText = stringResource(R.string.ev_add_motor),
        onActionClick = { onAddMotor("Motor ${motors.size + 1}", 500) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpecTile(
                label = stringResource(R.string.ev_nominal_power),
                value = if (totalWatts > 0) "${totalWatts}W" else "1200W",
                valueColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            SpecTile(
                label = stringResource(R.string.ev_peak_power),
                value = if (totalWatts > 0) "${(totalWatts * 1.8).toInt()}W" else "2200W",
                valueColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        if (motors.isEmpty()) {
            Text(
                text = stringResource(R.string.ev_motors_empty),
                style = MaterialTheme.typography.bodyMedium,
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
                        modifier = Modifier.weight(2f)
                    )

                    OutlinedTextField(
                        value = if (motor.watts > 0) motor.watts.toString() else "",
                        onValueChange = { newWattsStr ->
                            val watts = newWattsStr.toIntOrNull() ?: 0
                            onUpdateMotor(index, motor.name, watts)
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

@Composable
private fun BatterySpecsSection(
    evConfig: EvConfig,
    onBatteryTechChanged: (String) -> Unit,
    onVoltsChanged: (String) -> Unit,
    onAmpersChanged: (String) -> Unit,
    onBatteryModeChanged: (BatteryMode) -> Unit,
    onMaxVoltageChanged: (Double) -> Unit,
    onMinVoltageChanged: (Double) -> Unit
) {
    val voltsNum = evConfig.batteryVolts.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
    val ampersNum = evConfig.batteryAmpers.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
    val calculatedEnergyWh = if (voltsNum > 0 && ampersNum > 0) {
        String.format("%.1f Wh", voltsNum * ampersNum)
    } else {
        "374.4 Wh"
    }

    ConfigSectionCard(
        title = stringResource(R.string.ev_battery_section),
        icon = Icons.Default.BatteryChargingFull,
        badgeText = stringResource(R.string.ev_removable_pack)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.battery_mode_label),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = evConfig.batteryMode == BatteryMode.PERCENTAGE,
                    onClick = { onBatteryModeChanged(BatteryMode.PERCENTAGE) },
                    label = { Text(stringResource(R.string.battery_mode_percentage)) }
                )
                FilterChip(
                    selected = evConfig.batteryMode == BatteryMode.VOLTAGE,
                    onClick = { onBatteryModeChanged(BatteryMode.VOLTAGE) },
                    label = { Text(stringResource(R.string.battery_mode_voltage)) }
                )
            }
        }

        OutlinedTextField(
            value = evConfig.batteryTechnology,
            onValueChange = onBatteryTechChanged,
            label = { Text(stringResource(R.string.ev_chemical_tech)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = evConfig.batteryVolts,
                onValueChange = onVoltsChanged,
                label = { Text(stringResource(R.string.ev_nominal_voltage)) },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = evConfig.batteryAmpers,
                onValueChange = onAmpersChanged,
                label = { Text(stringResource(R.string.ev_amp_capacity)) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = if (evConfig.maxVoltage > 0) evConfig.maxVoltage.toString() else "",
                onValueChange = { str -> onMaxVoltageChanged(str.toDoubleOrNull() ?: 0.0) },
                label = { Text(stringResource(R.string.max_voltage_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = if (evConfig.minVoltage > 0) evConfig.minVoltage.toString() else "",
                onValueChange = { str -> onMinVoltageChanged(str.toDoubleOrNull() ?: 0.0) },
                label = { Text(stringResource(R.string.min_voltage_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        SpecTile(
            label = stringResource(R.string.ev_energy_capacity),
            value = calculatedEnergyWh,
            valueColor = MaterialTheme.colorScheme.secondary
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.ev_soh_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = stringResource(R.string.ev_soh_value),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
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
    ConfigSectionCard(
        title = stringResource(R.string.ev_brakes_section),
        icon = Icons.Default.Tune
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                modifier = Modifier.weight(2f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.ev_fork_suspension).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = evConfig.suspensionTechnology,
                        onValueChange = onSuspensionTechChanged,
                        label = { Text(stringResource(R.string.ev_suspension_tech_label)) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChargingAndErgonomicsSection(
    evConfig: EvConfig,
    onChargePowerChanged: (String) -> Unit,
    onOtherCharacteristicsChanged: (String) -> Unit
) {
    ConfigSectionCard(
        title = stringResource(R.string.ev_charging_section),
        icon = Icons.Default.ElectricMeter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = evConfig.chargePower,
                onValueChange = onChargePowerChanged,
                label = { Text(stringResource(R.string.ev_charge_power_label)) },
                modifier = Modifier.weight(1f)
            )

            SpecTile(
                label = stringResource(R.string.ev_estimated_time),
                value = "4.5 h",
                valueColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = evConfig.otherCharacteristics,
            onValueChange = onOtherCharacteristicsChanged,
            label = { Text(stringResource(R.string.ev_chassis_ergonomics_details)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 4
        )
    }
}

@Composable
private fun TelemetryBannerSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ev_telemetry_bms_version).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.ev_telemetry_canbus),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "99.8%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(R.string.ev_telemetry_bt_signal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BottomActionTray(
    isLoaded: Boolean,
    onDiscard: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDiscard,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.ev_discard_button),
                fontWeight = FontWeight.SemiBold
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(2f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.ev_save_button),
                fontWeight = FontWeight.Bold
            )
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
