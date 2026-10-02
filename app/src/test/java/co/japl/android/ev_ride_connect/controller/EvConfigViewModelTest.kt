package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmClientPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.usecase.ClearActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.EvConfigUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.FetchEvInfoUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetActiveLlmConfigsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvConfigUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EvConfigViewModelTest {

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
    fun shouldInitializeAndLoadConfig() = runTest {
        val evConfigPort = FakeEvConfigPort()
        val llmConfigPort = FakeLlmConfigPort()
        val llmClientPort = FakeLlmClientPort()
        val sessionStatePort = FakeSessionStatePort()

        val evConfigUseCase = EvConfigUseCaseImpl(
            evConfigPort = evConfigPort,
            llmConfigPort = llmConfigPort,
            llmClientPort = llmClientPort,
            sessionStatePort = sessionStatePort,
            getEvConfigUseCase = GetEvConfigUseCase(evConfigPort),
            saveEvConfigUseCase = SaveEvConfigUseCase(evConfigPort),
            getActiveLlmConfigsUseCase = GetActiveLlmConfigsUseCase(llmConfigPort),
            fetchEvInfoUseCase = FetchEvInfoUseCase(llmClientPort),
            getActiveSessionUseCase = GetActiveSessionUseCase(sessionStatePort),
            saveActiveSessionUseCase = SaveActiveSessionUseCase(sessionStatePort),
            clearActiveSessionUseCase = ClearActiveSessionUseCase(sessionStatePort)
        )

        val viewModel = EvConfigViewModel(evConfigUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.evConfig.value?.brand).isEqualTo("VSETT")
    }

    private class FakeEvConfigPort : EvConfigPort {
        override suspend fun getEvConfig(): EvConfig = EvConfig(brand = "VSETT")
        override suspend fun saveEvConfig(config: EvConfig): Long = 1L
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        override suspend fun getActiveConfigs(): List<LlmConfig> = emptyList()
        override suspend fun getAllConfigs(): List<LlmConfig> = emptyList()
        override suspend fun saveConfig(config: LlmConfig): Long = 1L
        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean = true
        override suspend fun deleteConfig(id: Long): Boolean = true
    }

    private class FakeLlmClientPort : LlmClientPort {
        override suspend fun queryLlm(prompt: String, config: LlmConfig, promptTemplate: String?): String = "{}"
        override suspend fun fetchAvailableModels(apiKey: String): List<String> = emptyList()
        override suspend fun fetchAvailableModels(modelName: String, apiKey: String): List<String> = emptyList()
        override suspend fun validateApiKey(modelName: String, apiKey: String): Boolean = true
        override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String = ""
    }

    private class FakeSessionStatePort : SessionStatePort {
        override suspend fun saveActiveSession(session: ActiveSession) {}
        override suspend fun getActiveSession(): ActiveSession? = null
        override fun observeActiveSession(): Flow<ActiveSession?> = flowOf(null)
        override suspend fun clearActiveSession() {}
    }
}
