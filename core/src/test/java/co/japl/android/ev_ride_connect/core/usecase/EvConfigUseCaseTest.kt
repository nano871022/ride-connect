package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import kotlinx.coroutines.flow.MutableStateFlow
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

        val fetchEvInfoUseCase = FetchEvInfoUseCase(fakeLlmClientPort)
        useCase = EvConfigUseCaseImpl(
            fakeEvConfigPort,
            fakeLlmConfigPort,
            fakeLlmClientPort,
            fakeSessionStatePort,
            GetEvConfigUseCase(fakeEvConfigPort),
            SaveEvConfigUseCase(fakeEvConfigPort),
            GetActiveLlmConfigsUseCase(fakeLlmConfigPort),
            fetchEvInfoUseCase,
            GetActiveSessionUseCase(fakeSessionStatePort),
            SaveActiveSessionUseCase(fakeSessionStatePort),
            ClearActiveSessionUseCase(fakeSessionStatePort)
        )
    }

    @Test
    fun shouldGetAndSaveEvConfig() = runTest {
        val config = useCase.getEvConfig()
        assertThat(config?.brand).isEqualTo("VSETT")

        val newConfig = EvConfig(id = 2L, brand = "Inokim")
        val savedId = useCase.saveEvConfig(newConfig)
        assertThat(savedId).isEqualTo(2L)
    }

    private class FakeEvConfigPort : EvConfigPort {
        override suspend fun getEvConfig() = EvConfig(id = 1L, brand = "VSETT")
        override suspend fun saveEvConfig(config: EvConfig) = config.id
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        override suspend fun getAllConfigs() = emptyList<LlmConfig>()
        override suspend fun getActiveConfigs() = emptyList<LlmConfig>()
        override suspend fun saveConfig(config: LlmConfig) = 1L
        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean) = true
        override suspend fun deleteConfig(id: Long) = true
    }

    private class FakeLlmClientPort : LlmClientPort {
        override suspend fun validateApiKey(modelName: String, apiKey: String) = true
        override suspend fun fetchAvailableModels(modelName: String, apiKey: String) = emptyList<String>()
        override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String) = "{}"
    }

    private class FakeSessionStatePort : SessionStatePort {
        override suspend fun saveActiveSession(session: co.japl.android.ev_ride_connect.core.domain.ActiveSession) {}
        override suspend fun getActiveSession() = null
        override fun observeActiveSession() = MutableStateFlow(null)
        override suspend fun clearActiveSession() {}
    }
}
