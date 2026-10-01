package co.japl.android.ev_ride_connect.llm

import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.ports.LlmClientPort

class LlmClientAdapter(
    private val geminiClient: GeminiClient = GeminiClient()
) : LlmClientPort {

    override suspend fun queryLlm(prompt: String, config: LlmConfig, promptTemplate: String?): String {
        return geminiClient.queryLlm(prompt, config.apiKey, config.modelName)
    }

    override suspend fun fetchAvailableModels(apiKey: String): List<String> {
        return geminiClient.fetchAvailableModels(apiKey)
    }
}
