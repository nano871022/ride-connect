package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmClientPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import co.japl.android.ev_ride_connect.interfaces.usecase.EvConfigUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class EvConfigUseCaseTest {

    private lateinit var fakeEvConfigPort: FakeEvConfigPort
    private lateinit var fakeLlmConfigPort: FakeLlmConfigPort
    private lateinit var fakeLlmClientPort: FakeLlmClientPort
    private lateinit var fakeSessionStatePort: FakeSessionStatePort
    private lateinit var useCase: EvConfigUseCase

    @Before
    fun setUp() {
        fakeEvConfigPort = FakeEvConfigPort()
        fakeLlmConfigPort = FakeLlmConfigPort()
        fakeLlmClientPort = FakeLlmClientPort()
        fakeSessionStatePort = FakeSessionStatePort()

        useCase = EvConfigUseCaseImpl(
            evConfigPort = fakeEvConfigPort,
            llmConfigPort = fakeLlmConfigPort,
            llmClientPort = fakeLlmClientPort,
            sessionStatePort = fakeSessionStatePort,
            getEvConfigUseCase = GetEvConfigUseCase(fakeEvConfigPort),
            saveEvConfigUseCase = SaveEvConfigUseCase(fakeEvConfigPort),
            getActiveLlmConfigsUseCase = GetActiveLlmConfigsUseCase(fakeLlmConfigPort),
            fetchEvInfoUseCase = FetchEvInfoUseCase(fakeLlmClientPort),
            getActiveSessionUseCase = GetActiveSessionUseCase(fakeSessionStatePort),
            saveActiveSessionUseCase = SaveActiveSessionUseCase(fakeSessionStatePort),
            clearActiveSessionUseCase = ClearActiveSessionUseCase(fakeSessionStatePort)
        )
    }

    @Test
    fun shouldGetAndSaveEvConfig() = runTest {
        val config = useCase.getEvConfig()
        assertThat(config?.brand).isEqualTo("VSETT")

        val savedId = useCase.saveEvConfig(EvConfig(brand = "Dualtron"))
        assertThat(savedId).isEqualTo(1L)
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
