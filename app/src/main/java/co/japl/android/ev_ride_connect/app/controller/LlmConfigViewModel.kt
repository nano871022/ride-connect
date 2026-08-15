package co.japl.android.ev_ride_connect.app.controller

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

    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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
    }

    fun onApiKeyChanged(apiKey: String) {
        _apiKeyInput.value = apiKey
        _errorMessage.value = null
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

            val newConfig = LlmConfig(
                modelName = modelName,
                apiKey = apiKey,
                isActive = true
            )
            llmConfigPort.saveConfig(newConfig)
            _apiKeyInput.value = ""
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
