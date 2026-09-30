package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.ports.LlmClientPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.interfaces.usecase.LlmConfigUseCase
import javax.inject.Inject

class LlmConfigUseCaseImpl @Inject constructor(
    private val llmConfigPort: LlmConfigPort,
    private val llmClientPort: LlmClientPort,
    private val getAllLlmConfigsUseCase: GetAllLlmConfigsUseCase,
    private val saveLlmConfigUseCase: SaveLlmConfigUseCase,
    private val deleteLlmConfigUseCase: DeleteLlmConfigUseCase,
    private val toggleLlmConfigStatusUseCase: ToggleLlmConfigStatusUseCase,
    private val validateLlmApiKeyUseCase: ValidateLlmApiKeyUseCase,
    private val fetchAvailableLlmModelsUseCase: FetchAvailableLlmModelsUseCase
) : LlmConfigUseCase {
    override suspend fun getAllLlmConfigs(): List<LlmConfig> {
        return getAllLlmConfigsUseCase.execute()
    }

    override suspend fun saveLlmConfig(config: LlmConfig): Long {
        return saveLlmConfigUseCase.execute(config)
    }

    override suspend fun deleteLlmConfig(configId: Long): Boolean {
        return deleteLlmConfigUseCase.execute(configId)
    }

    override suspend fun toggleActiveStatus(configId: Long, isActive: Boolean): Boolean {
        return toggleLlmConfigStatusUseCase.execute(configId, isActive)
    }

    override suspend fun validateLlmApiKey(modelName: String, apiKey: String): Boolean {
        return validateLlmApiKeyUseCase.execute(modelName, apiKey)
    }

    override suspend fun fetchAvailableLlmModels(modelName: String, apiKey: String): List<String> {
        return fetchAvailableLlmModelsUseCase.execute(modelName, apiKey)
    }
}
