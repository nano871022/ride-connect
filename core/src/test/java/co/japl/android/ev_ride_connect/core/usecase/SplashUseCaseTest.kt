package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.interfaces.usecase.SplashUseCase
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class SplashUseCaseTest {

    private lateinit var useCase: SplashUseCase

    @Before
    fun setUp() {
        val evDataPort = FakeEvDataPort()
        val evConfigPort = FakeEvConfigPort()
        val llmConfigPort = FakeLlmConfigPort()

        useCase = SplashUseCaseImpl(
            evDataPort = evDataPort,
            evConfigPort = evConfigPort,
            llmConfigPort = llmConfigPort,
            getLatestEvDataUseCase = GetLatestEvDataUseCase(evDataPort),
            getEvConfigUseCase = GetEvConfigUseCase(evConfigPort),
            getActiveLlmConfigsUseCase = GetActiveLlmConfigsUseCase(llmConfigPort),
            getAllLlmConfigsUseCase = GetAllLlmConfigsUseCase(llmConfigPort)
        )
    }

    @Test
    fun shouldPreloadSplashData() = runTest {
        val splashData = useCase.preloadSplashData()

        assertThat(splashData.evConfig?.brand).isEqualTo("VSETT")
        assertThat(splashData.latestEvData?.evCode).isEqualTo("EV01")
    }

    private class FakeEvConfigPort : EvConfigPort {
        override suspend fun getEvConfig(): EvConfig = EvConfig(brand = "VSETT")
        override suspend fun saveEvConfig(config: EvConfig): Long = 1L
    }

    private class FakeEvDataPort : EvDataPort {
        override suspend fun getLatestEvData(): EvData = EvData(evCode = "EV01")
        override suspend fun saveEvData(evData: EvData): Long = 1L
        override suspend fun getAllEvData(): List<EvData> = emptyList()
        override suspend fun updateOdometer(evCode: String, newKm: Long, currentBatteryPercentage: Short): Long = 1L
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        override suspend fun getActiveConfigs(): List<LlmConfig> = emptyList()
        override suspend fun getAllConfigs(): List<LlmConfig> = emptyList()
        override suspend fun saveConfig(config: LlmConfig): Long = 1L
        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean = true
        override suspend fun deleteConfig(id: Long): Boolean = true
    }
}
