package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class SplashUseCaseTest {

    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeEvConfigPort: FakeEvConfigPort
    private lateinit var fakeLlmConfigPort: FakeLlmConfigPort
    private lateinit var useCase: SplashUseCase

    @Before
    fun setUp() {
        fakeEvDataPort = FakeEvDataPort()
        fakeEvConfigPort = FakeEvConfigPort()
        fakeLlmConfigPort = FakeLlmConfigPort()

        useCase = SplashUseCaseImpl(
            fakeEvDataPort,
            fakeEvConfigPort,
            fakeLlmConfigPort,
            GetLatestEvDataUseCase(fakeEvDataPort),
            GetEvConfigUseCase(fakeEvConfigPort),
            GetActiveLlmConfigsUseCase(fakeLlmConfigPort),
            GetAllLlmConfigsUseCase(fakeLlmConfigPort)
        )
    }

    @Test
    fun shouldPreloadSplashData() = runTest {
        val result = useCase.preloadSplashData()

        assertThat(result.latestEvData?.evCode).isEqualTo("EV01")
        assertThat(result.evConfig?.brand).isEqualTo("VSETT")
    }

    private class FakeEvDataPort : EvDataPort {
        override suspend fun getLatestEvData() = EvData(evCode = "EV01", km = 100L, batteryLevel = 80)
        override suspend fun getAllEvData() = listOf(EvData(evCode = "EV01", km = 100L, batteryLevel = 80))
        override suspend fun saveEvData(evData: EvData) = 1L
    }

    private class FakeEvConfigPort : EvConfigPort {
        override suspend fun getEvConfig() = EvConfig(id = 1L, brand = "VSETT")
        override suspend fun saveEvConfig(config: EvConfig) = 1L
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        override suspend fun getAllConfigs() = emptyList<LlmConfig>()
        override suspend fun getActiveConfigs() = emptyList<LlmConfig>()
        override suspend fun saveConfig(config: LlmConfig) = 1L
        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean) = true
        override suspend fun deleteConfig(id: Long) = true
    }
}
