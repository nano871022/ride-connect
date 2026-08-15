package co.japl.android.ev_ride_connect.app.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
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
    private val llmConfigPort: LlmConfigPort
) : ViewModel() {

    private val _configs = MutableStateFlow<List<LlmConfig>>(emptyList())
    val configs: StateFlow<List<LlmConfig>> = _configs.asStateFlow()

    private val _activeConfigs = MutableStateFlow<List<LlmConfig>>(emptyList())
    val activeConfigs: StateFlow<List<LlmConfig>> = _activeConfigs.asStateFlow()

    private val _selectedModel = MutableStateFlow(AVAILABLE_LLM_MODELS.first())
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _apiKeyInput = MutableStateFlow("")
    val apiKeyInput: StateFlow<String> = _apiKeyInput.asStateFlow()

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
    }

    fun onApiKeyChanged(apiKey: String) {
        _apiKeyInput.value = apiKey
    }

    fun saveConfig() {
        val modelName = _selectedModel.value
        val apiKey = _apiKeyInput.value.trim()

        if (apiKey.isEmpty()) return

        viewModelScope.launch {
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
