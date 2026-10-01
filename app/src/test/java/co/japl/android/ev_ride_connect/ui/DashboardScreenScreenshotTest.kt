package co.japl.android.ev_ride_connect.ui

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
import co.japl.android.ev_ride_connect.core.usecase.CalculateConsumptionUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateDynamicBatteryPercentageUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateOptimalBatteryPercentageUseCase
import co.japl.android.ev_ride_connect.core.usecase.DashboardUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.GetActiveLlmConfigsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetAllTripsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetLatestEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.ObserveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.UpdateOdometerUseCase
import co.japl.android.ev_ride_connect.controller.DashboardViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class DashboardScreenScreenshotTest {

    @Test
    fun testDashboardScreenSetup() {
        val evDataPort = FakeEvDataPort()
        val evConfigPort = FakeEvConfigPort()
        val llmConfigPort = FakeLlmConfigPort()
        val sessionStatePort = FakeSessionStatePort()
        val tripDatabasePort = FakeTripDatabasePort()

        val dashboardUseCase = DashboardUseCaseImpl(
            evDataPort,
            evConfigPort,
            llmConfigPort,
            sessionStatePort,
            tripDatabasePort,
            GetLatestEvDataUseCase(evDataPort),
            SaveEvDataUseCase(evDataPort),
            GetEvConfigUseCase(evConfigPort),
            GetActiveLlmConfigsUseCase(llmConfigPort),
            ObserveActiveSessionUseCase(sessionStatePort),
            CalculateDynamicBatteryPercentageUseCase(),
            CalculateOptimalBatteryPercentageUseCase(),
            CalculateConsumptionUseCase(),
            UpdateOdometerUseCase(evDataPort, evConfigPort),
            GetAllTripsUseCase(tripDatabasePort)
        )

        val viewModel = DashboardViewModel(dashboardUseCase)
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
