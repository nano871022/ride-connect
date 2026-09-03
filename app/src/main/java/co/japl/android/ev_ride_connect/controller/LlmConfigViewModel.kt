package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

val AVAILABLE_LLM_MODELS = listOf("Gemini", "DeepSeek", "ChatGPT", "Groq", "Claude", "Mistral")

@HiltViewModel
class LlmConfigViewModel @Inject constructor(
    private val llmConfigPort: LlmConfigPort,
    private val llmClientPort: LlmClientPort
) : ViewModel() {

    private val _configs = MutableStateFlow<List<LlmConfig>>(emptyList())
    val configs: StateFlow<List<LlmConfig>> = _configs.asStateFlow()

    private val _activeConfigs = MutableStateFlow<List<LlmConfig>>(emptyList())
    val activeConfigs: StateFlow<List<LlmConfig>> = _activeConfigs.asStateFlow()

    private val _selectedModel = MutableStateFlow(AVAILABLE_LLM_MODELS.first())
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _apiKeyInput = MutableStateFlow("")
    val apiKeyInput: StateFlow<String> = _apiKeyInput.asStateFlow()

    private val _availableVersions = MutableStateFlow<List<String>>(emptyList())
    val availableVersions: StateFlow<List<String>> = _availableVersions.asStateFlow()

    private val _selectedVersion = MutableStateFlow("")
    val selectedVersion: StateFlow<String> = _selectedVersion.asStateFlow()

    private val _isFetchingVersions = MutableStateFlow(false)
    val isFetchingVersions: StateFlow<Boolean> = _isFetchingVersions.asStateFlow()

    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _editingConfigId = MutableStateFlow<Long>(0L)
    val editingConfigId: StateFlow<Long> = _editingConfigId.asStateFlow()

    private val _validationSuccessMessage = MutableStateFlow<String?>(null)
    val validationSuccessMessage: StateFlow<String?> = _validationSuccessMessage.asStateFlow()

    init {
        loadConfigs()
    }

    fun loadConfigs() {
        viewModelScope.launch {
            val all = llmConfigPort.getAllConfigs()
            _configs.value = all
            _activeConfigs.value = all.filter { it.isActive }
        }
    }

    fun onModelSelected(modelName: String) {
        _selectedModel.value = modelName
        _errorMessage.value = null
        _availableVersions.value = emptyList()
        _selectedVersion.value = ""
        if (_apiKeyInput.value.isNotBlank()) {
            fetchAvailableVersions()
        }
    }

    fun onApiKeyChanged(apiKey: String) {
        _apiKeyInput.value = apiKey
        _errorMessage.value = null
        _validationSuccessMessage.value = null
    }

    fun onVersionSelected(version: String) {
        _selectedVersion.value = version
    }

    fun onEditConfig(config: LlmConfig) {
        _editingConfigId.value = config.id
        _selectedModel.value = config.modelName.ifBlank { AVAILABLE_LLM_MODELS.first() }
        _apiKeyInput.value = config.apiKey
        _selectedVersion.value = config.selectedVersion
        _errorMessage.value = null
        _validationSuccessMessage.value = null
    }

    fun onDuplicateConfig(config: LlmConfig) {
        _editingConfigId.value = 0L
        _selectedModel.value = config.modelName.ifBlank { AVAILABLE_LLM_MODELS.first() }
        _apiKeyInput.value = config.apiKey
        _selectedVersion.value = config.selectedVersion
        _errorMessage.value = null
        _validationSuccessMessage.value = null
    }

    fun onCancelEdit() {
        _editingConfigId.value = 0L
        _apiKeyInput.value = ""
        _selectedVersion.value = ""
        _availableVersions.value = emptyList()
        _errorMessage.value = null
        _validationSuccessMessage.value = null
    }

    fun deleteConfig(configId: Long) {
        viewModelScope.launch {
            llmConfigPort.deleteConfig(configId)
            if (_editingConfigId.value == configId) {
                onCancelEdit()
            }
            loadConfigs()
        }
    }

    fun validateApiKeyAndModel() {
        val modelName = _selectedModel.value
        val apiKey = _apiKeyInput.value.trim()
        if (apiKey.isEmpty()) return

        viewModelScope.launch {
            _isValidating.value = true
            _errorMessage.value = null
            _validationSuccessMessage.value = null
            val isValid = llmClientPort.validateApiKey(modelName, apiKey)
            _isValidating.value = false
            if (isValid) {
                _validationSuccessMessage.value = "VALIDATION_SUCCESS"
            } else {
                _errorMessage.value = "INVALID_API_KEY"
            }
        }
    }

    fun fetchAvailableVersions() {
        val modelName = _selectedModel.value
        val apiKey = _apiKeyInput.value.trim()
        if (apiKey.isEmpty()) return

        viewModelScope.launch {
            _isFetchingVersions.value = true
            try {
                val versions = llmClientPort.fetchAvailableModels(modelName, apiKey)
                _availableVersions.value = versions
                if (versions.isNotEmpty() && (_selectedVersion.value.isBlank() || !versions.contains(_selectedVersion.value))) {
                    _selectedVersion.value = versions.first()
                }
            } catch (_: Exception) {
                _availableVersions.value = emptyList()
            } finally {
                _isFetchingVersions.value = false
            }
        }
    }

    fun saveConfig() {
        val modelName = _selectedModel.value
        val apiKey = _apiKeyInput.value.trim()

        if (apiKey.isEmpty()) return

        viewModelScope.launch {
            _isValidating.value = true
            _errorMessage.value = null
            val isValid = llmClientPort.validateApiKey(modelName, apiKey)
            _isValidating.value = false

            if (!isValid) {
                _errorMessage.value = "INVALID_API_KEY"
                return@launch
            }

            val configToSave = LlmConfig(
                id = _editingConfigId.value,
                modelName = modelName,
                selectedVersion = _selectedVersion.value,
                apiKey = apiKey,
                isActive = true
            )
            llmConfigPort.saveConfig(configToSave)
            _editingConfigId.value = 0L
            _apiKeyInput.value = ""
            _availableVersions.value = emptyList()
            _selectedVersion.value = ""
            _validationSuccessMessage.value = null
            loadConfigs()
        }
    }

    fun toggleActiveStatus(configId: Long, isActive: Boolean) {
        viewModelScope.launch {
            llmConfigPort.toggleActiveStatus(configId, isActive)
            loadConfigs()
        }
    }
}
