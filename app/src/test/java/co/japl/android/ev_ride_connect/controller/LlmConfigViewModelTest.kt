package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.ports.LlmClientPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.usecase.DeleteLlmConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.FetchAvailableLlmModelsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetActiveLlmConfigsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetAllLlmConfigsUseCase
import co.japl.android.ev_ride_connect.core.usecase.LlmConfigUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.SaveLlmConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.ToggleLlmConfigStatusUseCase
import co.japl.android.ev_ride_connect.core.usecase.ValidateLlmApiKeyUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LlmConfigViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldInitializeAndLoadConfigs() = runTest {
        val llmConfigPort = FakeLlmConfigPort()
        val llmClientPort = FakeLlmClientPort()

        val llmConfigUseCase = LlmConfigUseCaseImpl(
            getAllLlmConfigsUseCase = GetAllLlmConfigsUseCase(llmConfigPort),
            saveLlmConfigUseCase = SaveLlmConfigUseCase(llmConfigPort),
            deleteLlmConfigUseCase = DeleteLlmConfigUseCase(llmConfigPort),
            toggleLlmConfigStatusUseCase = ToggleLlmConfigStatusUseCase(llmConfigPort),
            validateLlmApiKeyUseCase = ValidateLlmApiKeyUseCase(llmClientPort),
            fetchAvailableLlmModelsUseCase = FetchAvailableLlmModelsUseCase(llmClientPort),
            llmConfigPort = llmConfigPort,
            llmClientPort = llmClientPort
        )

        val viewModel = LlmConfigViewModel(llmConfigUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.configs.value).hasSize(1)
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
