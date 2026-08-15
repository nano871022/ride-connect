package co.japl.android.ev_ride_connect.core.ports

interface LlmClientPort {
    suspend fun validateApiKey(modelName: String, apiKey: String): Boolean
    suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String
}
