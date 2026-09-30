package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.ports.LlmClientPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class LlmConfigUseCaseTest {

    private lateinit var fakeLlmConfigPort: FakeLlmConfigPort
    private lateinit var fakeLlmClientPort: FakeLlmClientPort
    private lateinit var useCase: LlmConfigUseCase

    @Before
    fun setUp() {
        fakeLlmConfigPort = FakeLlmConfigPort()
        fakeLlmClientPort = FakeLlmClientPort()
        useCase = LlmConfigUseCaseImpl(
            fakeLlmConfigPort,
            fakeLlmClientPort,
            GetAllLlmConfigsUseCase(fakeLlmConfigPort),
            SaveLlmConfigUseCase(fakeLlmConfigPort),
            DeleteLlmConfigUseCase(fakeLlmConfigPort),
            ToggleLlmConfigStatusUseCase(fakeLlmConfigPort),
            ValidateLlmApiKeyUseCase(fakeLlmClientPort),
            FetchAvailableLlmModelsUseCase(fakeLlmClientPort)
        )
    }

    @Test
    fun shouldGetAllConfigsAndValidateApiKey() = runTest {
        val configs = useCase.getAllLlmConfigs()
        assertThat(configs).hasSize(1)

        val isValid = useCase.validateLlmApiKey("Gemini", "valid-key")
        assertThat(isValid).isTrue()
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        override suspend fun getAllConfigs() = listOf(LlmConfig(id = 1L, modelName = "Gemini", apiKey = "key"))
        override suspend fun getActiveConfigs() = emptyList<LlmConfig>()
        override suspend fun saveConfig(config: LlmConfig) = 1L
        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean) = true
        override suspend fun deleteConfig(id: Long) = true
    }

    private class FakeLlmClientPort : LlmClientPort {
        override suspend fun validateApiKey(modelName: String, apiKey: String) = true
        override suspend fun fetchAvailableModels(modelName: String, apiKey: String) = listOf("v1")
        override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String) = ""
    }
}
