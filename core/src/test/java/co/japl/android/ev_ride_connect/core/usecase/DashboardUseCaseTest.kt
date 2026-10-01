package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import co.japl.android.ev_ride_connect.interfaces.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.interfaces.usecase.DashboardUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class DashboardUseCaseTest {

    private lateinit var useCase: DashboardUseCase

    @Before
    fun setUp() {
        val evDataPort = FakeEvDataPort()
        val evConfigPort = FakeEvConfigPort()
        val llmConfigPort = FakeLlmConfigPort()
        val sessionStatePort = FakeSessionStatePort()
        val tripDatabasePort = FakeTripDatabasePort()

        useCase = DashboardUseCaseImpl(
            evDataPort = evDataPort,
            evConfigPort = evConfigPort,
            llmConfigPort = llmConfigPort,
            sessionStatePort = sessionStatePort,
            tripDatabasePort = tripDatabasePort,
            getLatestEvDataUseCase = GetLatestEvDataUseCase(evDataPort),
            getEvConfigUseCase = GetEvConfigUseCase(evConfigPort),
            getActiveLlmConfigsUseCase = GetActiveLlmConfigsUseCase(llmConfigPort),
            observeActiveSessionUseCase = ObserveActiveSessionUseCase(sessionStatePort),
            getAllTripsUseCase = GetAllTripsUseCase(tripDatabasePort),
            calculateConsumptionUseCase = CalculateConsumptionUseCase(),
            calculateOptimalBatteryPercentageUseCase = CalculateOptimalBatteryPercentageUseCase(),
            calculateDynamicBatteryPercentageUseCase = CalculateDynamicBatteryPercentageUseCase(),
            saveEvDataUseCase = SaveEvDataUseCase(evDataPort),
            updateOdometerUseCase = UpdateOdometerUseCase(evDataPort, GetLatestEvDataUseCase(evDataPort))
        )
    }

    @Test
    fun shouldReturnLatestDataAndConfig() = runTest {
        val evData = useCase.getLatestEvData()
        val evConfig = useCase.getEvConfig()

        assertThat(evData?.evCode).isEqualTo("EV01")
        assertThat(evConfig?.brand).isEqualTo("VSETT")
    }

    @Test
    fun shouldCalculateMetricsCorrectly() {
        val optimalBattery = useCase.calculateOptimalBatteryPercentage(10)
        assertThat(optimalBattery).isGreaterThan(0.0)

        val consumption = useCase.calculateConsumption(10, 52.0, 20.0, 15.0)
        assertThat(consumption).isGreaterThan(0.0)
    }

    private class FakeEvDataPort : EvDataPort {
        override suspend fun getLatestEvData(): EvData = EvData(evCode = "EV01", km = 100, batteryLevel = 90)
        override suspend fun saveEvData(evData: EvData): Long = 1L
        override suspend fun getAllEvData(): List<EvData> = emptyList()
        override suspend fun updateOdometer(evCode: String, newKm: Long, currentBatteryPercentage: Short): Long = 1L
    }

    private class FakeEvConfigPort : EvConfigPort {
        override suspend fun getEvConfig(): EvConfig = EvConfig(brand = "VSETT", version = "C7 Plus")
        override suspend fun saveEvConfig(config: EvConfig): Long = 1L
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        override suspend fun getActiveConfigs(): List<LlmConfig> = emptyList()
        override suspend fun getAllConfigs(): List<LlmConfig> = emptyList()
        override suspend fun saveConfig(config: LlmConfig): Long = 1L
        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean = true
        override suspend fun deleteConfig(id: Long): Boolean = true
    }

    private class FakeSessionStatePort : SessionStatePort {
        override suspend fun saveActiveSession(session: ActiveSession) {}
        override suspend fun getActiveSession(): ActiveSession? = null
        override fun observeActiveSession(): Flow<ActiveSession?> = flowOf(null)
        override suspend fun clearActiveSession() {}
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
        override suspend fun saveTrip(trip: Trip, gpsPoints: List<co.japl.android.ev_ride_connect.interfaces.model.TripGps>): Long = 1L
        override suspend fun getAllTrips(): List<Trip> = emptyList()
        override suspend fun getTripById(tripId: Long): Trip? = null
        override suspend fun getGpsPointsByTripId(tripId: Long): List<co.japl.android.ev_ride_connect.interfaces.model.TripGps> = emptyList()
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> = emptyList()
        override suspend fun getTotalTripsCount(): Int = 0
        override suspend fun getTotalDistanceKm(): Double = 0.0
        override suspend fun getChargeDetectionsCount(threshold: Int): Int = 0
    }
}
