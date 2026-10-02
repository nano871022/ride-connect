package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.ports.LlmClientPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.interfaces.usecase.LlmConfigUseCase
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class LlmConfigUseCaseTest {

    private lateinit var useCase: LlmConfigUseCase

    @Before
    fun setUp() {
        val llmConfigPort = FakeLlmConfigPort()
        val llmClientPort = FakeLlmClientPort()

        useCase = LlmConfigUseCaseImpl(
            llmConfigPort = llmConfigPort,
            llmClientPort = llmClientPort,
            getAllLlmConfigsUseCase = GetAllLlmConfigsUseCase(llmConfigPort),
            saveLlmConfigUseCase = SaveLlmConfigUseCase(llmConfigPort),
            deleteLlmConfigUseCase = DeleteLlmConfigUseCase(llmConfigPort),
            toggleLlmConfigStatusUseCase = ToggleLlmConfigStatusUseCase(llmConfigPort),
            validateLlmApiKeyUseCase = ValidateLlmApiKeyUseCase(llmClientPort),
            fetchAvailableLlmModelsUseCase = FetchAvailableLlmModelsUseCase(llmClientPort)
        )
    }

    @Test
    fun shouldManageLlmConfigAndValidateKey() = runTest {
        val configs = useCase.getAllLlmConfigs()
        assertThat(configs).hasSize(1)

        val isValid = useCase.validateLlmApiKey("Gemini", "validKey")
        assertThat(isValid).isTrue()
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        override suspend fun getActiveConfigs(): List<LlmConfig> = listOf(LlmConfig(modelName = "Gemini"))
        override suspend fun getAllConfigs(): List<LlmConfig> = listOf(LlmConfig(modelName = "Gemini"))
        override suspend fun saveConfig(config: LlmConfig): Long = 1L
        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean = true
        override suspend fun deleteConfig(id: Long): Boolean = true
    }

    private class FakeLlmClientPort : LlmClientPort {
        override suspend fun queryLlm(prompt: String, config: LlmConfig, promptTemplate: String?): String = "{}"
        override suspend fun fetchAvailableModels(apiKey: String): List<String> = listOf("Gemini 1.5 Flash")
        override suspend fun fetchAvailableModels(modelName: String, apiKey: String): List<String> = listOf("Gemini 1.5 Flash")
        override suspend fun validateApiKey(modelName: String, apiKey: String): Boolean = true
        override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String = ""
    }
}
