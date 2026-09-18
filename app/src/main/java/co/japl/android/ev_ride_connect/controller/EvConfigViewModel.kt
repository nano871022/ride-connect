package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.domain.MotorSpec
import co.japl.android.ev_ride_connect.core.usecase.ClearActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.FetchEvInfoUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetActiveLlmConfigsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvConfigViewModel @Inject constructor(
    private val getEvConfigUseCase: GetEvConfigUseCase,
    private val saveEvConfigUseCase: SaveEvConfigUseCase,
    private val getActiveLlmConfigsUseCase: GetActiveLlmConfigsUseCase,
    private val fetchEvInfoUseCase: FetchEvInfoUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val saveActiveSessionUseCase: SaveActiveSessionUseCase,
    private val clearActiveSessionUseCase: ClearActiveSessionUseCase
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

    private val _isSearchDialogVisible = MutableStateFlow(false)
    val isSearchDialogVisible: StateFlow<Boolean> = _isSearchDialogVisible.asStateFlow()

    init {
        loadSavedConfig()
        loadActiveLlmConfigs()
        checkAndHydratePendingLlmState()
    }

    private fun checkAndHydratePendingLlmState() {
        viewModelScope.launch {
            val session = getActiveSessionUseCase.execute()
            if (session != null) {
                val response = session.pendingLlmResponse
                if (session.isLlmProcessing) {
                    _isSearchDialogVisible.value = true
                    _isLoadingLlm.value = true
                } else if (response != null) {
                    _isLoadingLlm.value = false
                    if (response.startsWith("SUCCESS")) {
                        loadSavedConfig()
                        _statusMessage.value = "LLM_FETCH_SUCCESS"
                        _isSearchDialogVisible.value = false
                    } else if (response.startsWith("ERROR")) {
                        _llmErrorMessage.value = response.removePrefix("ERROR:")
                        _isSearchDialogVisible.value = true
                    }
                    val cleared = session.copy(pendingLlmPrompt = null, pendingLlmResponse = null)
                    if (!cleared.isRideActive) {
                        clearActiveSessionUseCase.execute()
                    } else {
                        saveActiveSessionUseCase.execute(cleared)
                    }
                }
            }
        }
    }

    fun loadSavedConfig() {
        viewModelScope.launch {
            val saved = getEvConfigUseCase.execute()
            if (saved != null) {
                _evConfig.value = saved
            }
        }
    }

    fun loadActiveLlmConfigs() {
        viewModelScope.launch {
            val configs = getActiveLlmConfigsUseCase.execute()
            _activeLlmConfigs.value = configs
            if (_selectedLlmConfig.value == null && configs.isNotEmpty()) {
                _selectedLlmConfig.value = configs.first()
            }
        }
    }

    fun onSelectLlmConfig(config: LlmConfig) {
        _selectedLlmConfig.value = config
    }

    fun onImageUrlChanged(value: String) {
        _evConfig.update { it.copy(imageUrl = value) }
    }

    fun onPrepareNewVehicle() {
        _evConfig.update { it.copy(id = 0, isLoaded = false) }
        _statusMessage.value = "EV_CREATION_MODE"
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

    fun requestEvInfoFromLlm(promptTemplate: String? = null) {
        val requestText = _evConfig.value.request.trim()
        if (requestText.isBlank()) {
            _llmErrorMessage.value = "EMPTY_REQUEST_PROMPT"
            return
        }

        viewModelScope.launch {
            _isSearchDialogVisible.value = true
            _isLoadingLlm.value = true
            _llmErrorMessage.value = null

            val configs = getActiveLlmConfigsUseCase.execute()
            val config = configs.maxByOrNull { it.id } ?: configs.firstOrNull()

            if (config == null || config.apiKey.isBlank()) {
                _llmErrorMessage.value = "NO_ACTIVE_LLM_CONFIG"
                _isLoadingLlm.value = false
                return@launch
            }

            try {
                val updatedConfig = fetchEvInfoUseCase.execute(requestText, config, _evConfig.value, promptTemplate)
                _evConfig.value = updatedConfig
                _statusMessage.value = "LLM_FETCH_SUCCESS"
                _isSearchDialogVisible.value = false
            } catch (e: Exception) {
                _llmErrorMessage.value = e.localizedMessage ?: "LLM_FETCH_FAILED"
            } finally {
                _isLoadingLlm.value = false
            }
        }
    }

    fun dismissSearchDialog() {
        _isSearchDialogVisible.value = false
        _llmErrorMessage.value = null
    }

    fun saveEvConfig() {
        viewModelScope.launch {
            val id = saveEvConfigUseCase.execute(_evConfig.value)
            _evConfig.update { it.copy(id = id) }
            _statusMessage.value = "CONFIG_SAVED"
        }
    }

    fun loadEv() {
        viewModelScope.launch {
            val updated = _evConfig.value.copy(isLoaded = true)
            val id = saveEvConfigUseCase.execute(updated)
            _evConfig.value = updated.copy(id = id)
            _statusMessage.value = "EV_LOADED"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
        _llmErrorMessage.value = null
    }
}
