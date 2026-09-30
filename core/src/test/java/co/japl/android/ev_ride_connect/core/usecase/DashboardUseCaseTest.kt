package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class DashboardUseCaseTest {

    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeEvConfigPort: FakeEvConfigPort
    private lateinit var fakeLlmConfigPort: FakeLlmConfigPort
    private lateinit var fakeSessionStatePort: FakeSessionStatePort
    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var useCase: DashboardUseCase

    @Before
    fun setUp() {
        fakeEvDataPort = FakeEvDataPort()
        fakeEvConfigPort = FakeEvConfigPort()
        fakeLlmConfigPort = FakeLlmConfigPort()
        fakeSessionStatePort = FakeSessionStatePort()
        fakeTripPort = FakeTripDatabasePort()

        val getLatestEvDataUseCase = GetLatestEvDataUseCase(fakeEvDataPort)

        useCase = DashboardUseCase(
            fakeEvDataPort,
            fakeEvConfigPort,
            fakeLlmConfigPort,
            fakeSessionStatePort,
            fakeTripPort,
            getLatestEvDataUseCase,
            SaveEvDataUseCase(fakeEvDataPort),
            GetEvConfigUseCase(fakeEvConfigPort),
            GetActiveLlmConfigsUseCase(fakeLlmConfigPort),
            ObserveActiveSessionUseCase(fakeSessionStatePort),
            CalculateDynamicBatteryPercentageUseCase(),
            CalculateOptimalBatteryPercentageUseCase(),
            CalculateConsumptionUseCase(),
            UpdateOdometerUseCase(fakeEvDataPort, getLatestEvDataUseCase),
            GetAllTripsUseCase(fakeTripPort)
        )
    }

    @Test
    fun shouldGetLatestEvDataAndEvConfig() = runTest {
        val evData = useCase.getLatestEvData()
        val evConfig = useCase.getEvConfig()

        assertThat(evData?.evCode).isEqualTo("EV01")
        assertThat(evConfig?.brand).isEqualTo("VSETT")
    }

    @Test
    fun shouldCalculateMetricsCorrectly() {
        val optimalPct = useCase.calculateOptimalBatteryPercentage(10)
        assertThat(optimalPct).isGreaterThan(0.0)

        val consumption = useCase.calculateConsumption(10, 52.0, 20.0, 10.0)
        assertThat(consumption).isGreaterThan(0.0)
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

    private class FakeSessionStatePort : SessionStatePort {
        override suspend fun saveActiveSession(session: co.japl.android.ev_ride_connect.core.domain.ActiveSession) {}
        override suspend fun getActiveSession() = null
        override fun observeActiveSession() = MutableStateFlow(null)
        override suspend fun clearActiveSession() {}
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        override suspend fun saveTrip(trip: Trip, gpsPoints: List<co.japl.android.ev_ride_connect.core.domain.TripGps>) = 1L
        override suspend fun getAllTrips() = emptyList<Trip>()
        override suspend fun getTripById(tripId: Long) = null
        override suspend fun getGpsPointsByTripId(tripId: Long) = emptyList<co.japl.android.ev_ride_connect.core.domain.TripGps>()
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long) = emptyList<Trip>()
        override suspend fun getTotalTripsCount() = 0
        override suspend fun getTotalDistanceKm() = 0.0
        override suspend fun getChargeDetectionsCount(threshold: Int) = 0
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
    }
}
