package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class UpdateOdometerUseCaseTest {

    private lateinit var useCase: UpdateOdometerUseCase

    @Before
    fun setUp() {
        val evDataPort = FakeEvDataPort()
        useCase = UpdateOdometerUseCase(
            evDataPort = evDataPort,
            getLatestEvDataUseCase = GetLatestEvDataUseCase(evDataPort)
        )
    }

    @Test
    fun shouldUpdateOdometer() = runTest {
        val result = useCase.execute("EV01", 1500L, 95)

        assertThat(result).isEqualTo(1L)
    }

    private class FakeEvDataPort : EvDataPort {
        override suspend fun getLatestEvData(): EvData? = null
        override suspend fun saveEvData(evData: EvData): Long = 1L
        override suspend fun getAllEvData(): List<EvData> = emptyList()
        override suspend fun updateOdometer(evCode: String, newKm: Long, currentBatteryPercentage: Short): Long = 1L
    }

    private class FakeEvConfigPort : EvConfigPort {
        override suspend fun getEvConfig(): EvConfig = EvConfig(id = 1, brand = "VSETT")
        override suspend fun saveEvConfig(config: EvConfig): Long = 1L
    }
}
