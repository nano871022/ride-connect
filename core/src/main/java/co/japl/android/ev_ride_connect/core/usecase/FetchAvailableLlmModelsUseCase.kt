package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import javax.inject.Inject

class FetchAvailableLlmModelsUseCase @Inject constructor(
    private val llmClientPort: LlmClientPort
) {
    suspend fun execute(modelName: String, apiKey: String): List<String> =
        llmClientPort.fetchAvailableModels(modelName, apiKey)
}
