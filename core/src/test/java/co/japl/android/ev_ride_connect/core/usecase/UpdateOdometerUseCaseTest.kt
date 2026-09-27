package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class UpdateOdometerUseCaseTest {

    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeGetLatestEvDataUseCase: GetLatestEvDataUseCase
    private lateinit var useCase: UpdateOdometerUseCase

    private class FakeEvDataPort : EvDataPort {
        val savedData = mutableListOf<EvData>()

        override suspend fun getLatestEvData(): EvData? = savedData.lastOrNull()
        override suspend fun getAllEvData(): List<EvData> = savedData
        override suspend fun saveEvData(evData: EvData): Long {
            savedData.add(evData)
            return savedData.size.toLong()
        }
    }

    @Before
    fun setUp() {
        fakeEvDataPort = FakeEvDataPort()
        fakeGetLatestEvDataUseCase = GetLatestEvDataUseCase(fakeEvDataPort)
        useCase = UpdateOdometerUseCase(fakeEvDataPort, fakeGetLatestEvDataUseCase)
    }

    @Test
    fun execute_savesNewOdometerValue() = runTest {
        val result = useCase.execute("EV01", newKm = 150L, currentBatteryLevel = 80)

        assertThat(result).isEqualTo(1L)
        assertThat(fakeEvDataPort.savedData).hasSize(1)
        assertThat(fakeEvDataPort.savedData[0].km).isEqualTo(150L)
        assertThat(fakeEvDataPort.savedData[0].batteryLevel).isEqualTo(80.toShort())
    }
}
