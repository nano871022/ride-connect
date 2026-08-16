package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import javax.inject.Inject

class LlmClientAdapter @Inject constructor() : LlmClientPort {

    override suspend fun validateApiKey(modelName: String, apiKey: String): Boolean {
        if (apiKey.isBlank()) return false
        if (apiKey.trim().length < 8) return false
        if (apiKey.lowercase().contains("invalid") || apiKey.lowercase().contains("error")) return false
        return true
    }

    override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String {
        if (!validateApiKey(modelName, apiKey)) {
            throw IllegalArgumentException("Invalid API key for model: $modelName")
        }
        return "Response from $modelName: $prompt"
    }
}
