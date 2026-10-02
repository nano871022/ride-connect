package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig

interface LlmClientPort {
    suspend fun queryLlm(prompt: String, config: LlmConfig, promptTemplate: String? = null): String
    suspend fun fetchAvailableModels(apiKey: String): List<String>
    suspend fun fetchAvailableModels(modelName: String, apiKey: String): List<String> = fetchAvailableModels(apiKey)
    suspend fun validateApiKey(modelName: String, apiKey: String): Boolean = true
    suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String = ""
}
