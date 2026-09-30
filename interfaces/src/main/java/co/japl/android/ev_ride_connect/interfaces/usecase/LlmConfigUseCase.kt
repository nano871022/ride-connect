package co.japl.android.ev_ride_connect.interfaces.usecase

import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig

interface LlmConfigUseCase {
    suspend fun getAllLlmConfigs(): List<LlmConfig>
    suspend fun saveLlmConfig(config: LlmConfig): Long
    suspend fun deleteLlmConfig(configId: Long): Boolean
    suspend fun toggleActiveStatus(configId: Long, isActive: Boolean): Boolean
    suspend fun validateLlmApiKey(modelName: String, apiKey: String): Boolean
    suspend fun fetchAvailableLlmModels(modelName: String, apiKey: String): List<String>
}
