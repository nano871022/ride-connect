package co.japl.android.ev_ride_connect.app.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.domain.MotorSpec
import co.japl.android.ev_ride_connect.core.mappers.EvConfigMapper
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvConfigViewModel @Inject constructor(
    private val evConfigPort: EvConfigPort,
    private val llmConfigPort: LlmConfigPort,
    private val llmClientPort: LlmClientPort,
    private val bleScooterPort: BleScooterPort
) : ViewModel() {

    private val _evConfig = MutableStateFlow(EvConfig())
    val evConfig: StateFlow<EvConfig> = _evConfig.asStateFlow()

    private val _isLoadingLlm = MutableStateFlow(false)
    val isLoadingLlm: StateFlow<Boolean> = _isLoadingLlm.asStateFlow()

    private val _llmErrorMessage = MutableStateFlow<String?>(null)
    val llmErrorMessage: StateFlow<String?> = _llmErrorMessage.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _activeLlmConfigs = MutableStateFlow<List<LlmConfig>>(emptyList())
    val activeLlmConfigs: StateFlow<List<LlmConfig>> = _activeLlmConfigs.asStateFlow()

    private val _selectedLlmConfig = MutableStateFlow<LlmConfig?>(null)
    val selectedLlmConfig: StateFlow<LlmConfig?> = _selectedLlmConfig.asStateFlow()

    init {
        loadSavedConfig()
        loadActiveLlmConfigs()
    }

    fun loadSavedConfig() {
        viewModelScope.launch {
            val saved = evConfigPort.getEvConfig()
            if (saved != null) {
                _evConfig.value = saved
            }
        }
    }

    fun loadActiveLlmConfigs() {
        viewModelScope.launch {
            val configs = llmConfigPort.getActiveConfigs()
            _activeLlmConfigs.value = configs
            if (_selectedLlmConfig.value == null && configs.isNotEmpty()) {
                _selectedLlmConfig.value = configs.first()
            }
        }
    }

    fun onSelectLlmConfig(config: LlmConfig) {
        _selectedLlmConfig.value = config
    }

    fun onRequestChanged(value: String) {
        _evConfig.update { it.copy(request = value) }
    }

    fun onBrandChanged(value: String) {
        _evConfig.update { it.copy(brand = value) }
    }

    fun onVersionChanged(value: String) {
        _evConfig.update { it.copy(version = value) }
    }

    fun onManufactoryYearChanged(value: String) {
        _evConfig.update { it.copy(manufactoryYear = value) }
    }

    fun onManufactoryCompanyChanged(value: String) {
        _evConfig.update { it.copy(manufactoryCompany = value) }
    }

    fun onBoughtDateChanged(value: String) {
        _evConfig.update { it.copy(boughtDate = value) }
    }

    fun onBatteryTechnologyChanged(value: String) {
        _evConfig.update { it.copy(batteryTechnology = value) }
    }

    fun onBatteryVoltsChanged(value: String) {
        _evConfig.update { it.copy(batteryVolts = value) }
    }

    fun onBatteryAmpersChanged(value: String) {
        _evConfig.update { it.copy(batteryAmpers = value) }
    }

    fun onBrakeQuantityChanged(value: Int) {
        _evConfig.update { it.copy(brakeQuantity = value) }
    }

    fun onBrakeTechnologyChanged(value: String) {
        _evConfig.update { it.copy(brakeTechnology = value) }
    }

    fun onSuspensionTechnologyChanged(value: String) {
        _evConfig.update { it.copy(suspensionTechnology = value) }
    }

    fun onChargePowerChanged(value: String) {
        _evConfig.update { it.copy(chargePower = value) }
    }

    fun onOtherCharacteristicsChanged(value: String) {
        _evConfig.update { it.copy(otherCharacteristics = value) }
    }

    fun onAddMotor(name: String, watts: Int) {
        val updatedMotors = _evConfig.value.motors.toMutableList().apply {
            add(MotorSpec(name = name, watts = watts))
        }
        _evConfig.update { it.copy(motors = updatedMotors) }
    }

    fun onUpdateMotor(index: Int, name: String, watts: Int) {
        val currentMotors = _evConfig.value.motors.toMutableList()
        if (index in currentMotors.indices) {
            currentMotors[index] = MotorSpec(name = name, watts = watts)
            _evConfig.update { it.copy(motors = currentMotors) }
        }
    }

    fun onRemoveMotor(index: Int) {
        val currentMotors = _evConfig.value.motors.toMutableList()
        if (index in currentMotors.indices) {
            currentMotors.removeAt(index)
            _evConfig.update { it.copy(motors = currentMotors) }
        }
    }

    fun requestEvInfoFromLlm() {
        val requestText = _evConfig.value.request.trim()
        if (requestText.isBlank()) {
            _llmErrorMessage.value = "EMPTY_REQUEST_PROMPT"
            return
        }

        val config = _selectedLlmConfig.value ?: _activeLlmConfigs.value.firstOrNull()
        if (config == null || config.apiKey.isBlank()) {
            _llmErrorMessage.value = "NO_ACTIVE_LLM_CONFIG"
            return
        }

        viewModelScope.launch {
            _isLoadingLlm.value = true
            _llmErrorMessage.value = null

            try {
                val prompt = buildEvPrompt(requestText)
                val responseText = llmClientPort.generateResponse(config.modelName, config.apiKey, prompt)
                parseAndApplyEvInfo(requestText, responseText)
                _statusMessage.value = "LLM_FETCH_SUCCESS"
            } catch (e: Exception) {
                _llmErrorMessage.value = e.localizedMessage ?: "LLM_FETCH_FAILED"
            } finally {
                _isLoadingLlm.value = false
            }
        }
    }

    fun saveEvConfig() {
        viewModelScope.launch {
            val id = evConfigPort.saveEvConfig(_evConfig.value)
            _evConfig.update { it.copy(id = id) }
            _statusMessage.value = "CONFIG_SAVED"
        }
    }

    fun loadEvAndConnectBle() {
        viewModelScope.launch {
            val updated = _evConfig.value.copy(isLoaded = true)
            val id = evConfigPort.saveEvConfig(updated)
            _evConfig.value = updated.copy(id = id)
            bleScooterPort.sendCommand(1, false)
            _statusMessage.value = "EV_LOADED_BLE_CONNECTED"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
        _llmErrorMessage.value = null
    }

    private fun buildEvPrompt(userRequest: String): String {
        return String.format(
            co.japl.android.ev_ride_connect.core.domain.EvConstants.EV_LLM_PROMPT_TEMPLATE.trimIndent(),
            userRequest
        )
    }

    private fun parseAndApplyEvInfo(userRequest: String, responseText: String) {
        val parsedConfig = EvConfigMapper.fromLlmResponse(userRequest, responseText)
        _evConfig.update { current ->
            current.copy(
                brand = if (parsedConfig.brand.isNotBlank()) parsedConfig.brand else current.brand,
                version = if (parsedConfig.version.isNotBlank()) parsedConfig.version else current.version,
                motors = if (parsedConfig.motors.isNotEmpty()) parsedConfig.motors else current.motors,
                manufactoryYear = if (parsedConfig.manufactoryYear.isNotBlank()) parsedConfig.manufactoryYear else current.manufactoryYear,
                manufactoryCompany = if (parsedConfig.manufactoryCompany.isNotBlank()) parsedConfig.manufactoryCompany else current.manufactoryCompany,
                batteryTechnology = if (parsedConfig.batteryTechnology.isNotBlank()) parsedConfig.batteryTechnology else current.batteryTechnology,
                batteryVolts = if (parsedConfig.batteryVolts.isNotBlank()) parsedConfig.batteryVolts else current.batteryVolts,
                batteryAmpers = if (parsedConfig.batteryAmpers.isNotBlank()) parsedConfig.batteryAmpers else current.batteryAmpers,
                brakeQuantity = if (parsedConfig.brakeQuantity > 0) parsedConfig.brakeQuantity else current.brakeQuantity,
                brakeTechnology = if (parsedConfig.brakeTechnology.isNotBlank()) parsedConfig.brakeTechnology else current.brakeTechnology,
                suspensionTechnology = if (parsedConfig.suspensionTechnology.isNotBlank()) parsedConfig.suspensionTechnology else current.suspensionTechnology,
                chargePower = if (parsedConfig.chargePower.isNotBlank()) parsedConfig.chargePower else current.chargePower,
                otherCharacteristics = if (parsedConfig.otherCharacteristics.isNotBlank()) parsedConfig.otherCharacteristics else current.otherCharacteristics
            )
        }
    }
}
