package co.japl.android.ev_ride_connect.controller

import android.content.Context
import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.MotionState
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import co.japl.android.ev_ride_connect.interfaces.ports.MotionDetectorPort
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import co.japl.android.ev_ride_connect.interfaces.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.core.usecase.CalculateCo2SavedUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateConsumptionUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateDynamicBatteryPercentageUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateTripSummaryUseCase
import co.japl.android.ev_ride_connect.core.usecase.EndTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetLatestEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripDetailsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripsByDateUseCase
import co.japl.android.ev_ride_connect.core.usecase.ObserveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.PauseTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.ResumeTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.TripUseCaseImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
class TripViewModelTest {

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
    fun shouldInitializeAndLoadEvConfig() = runTest {
        val tripDatabasePort = FakeTripDatabasePort()
        val sessionStatePort = FakeSessionStatePort()
        val evConfigPort = FakeEvConfigPort()
        val evDataPort = FakeEvDataPort()
        val motionDetectorPort = FakeMotionDetectorPort()

        val tripUseCase = TripUseCaseImpl(
            tripDatabasePort = tripDatabasePort,
            sessionStatePort = sessionStatePort,
            evConfigPort = evConfigPort,
            evDataPort = evDataPort,
            motionDetectorPort = motionDetectorPort,
            saveTripUseCase = SaveTripUseCase(tripDatabasePort),
            endTripUseCase = EndTripUseCase(sessionStatePort),
            pauseTripUseCase = PauseTripUseCase(sessionStatePort),
            resumeTripUseCase = ResumeTripUseCase(sessionStatePort),
            calculateTripSummaryUseCase = CalculateTripSummaryUseCase(tripDatabasePort),
            calculateCo2SavedUseCase = CalculateCo2SavedUseCase(),
            calculateConsumptionUseCase = CalculateConsumptionUseCase(),
            calculateDynamicBatteryPercentageUseCase = CalculateDynamicBatteryPercentageUseCase(),
            getTripDetailsUseCase = GetTripDetailsUseCase(tripDatabasePort),
            getTripsByDateUseCase = GetTripsByDateUseCase(tripDatabasePort),
            getEvConfigUseCase = GetEvConfigUseCase(evConfigPort),
            getLatestEvDataUseCase = GetLatestEvDataUseCase(evDataPort),
            saveEvDataUseCase = SaveEvDataUseCase(evDataPort),
            getActiveSessionUseCase = GetActiveSessionUseCase(sessionStatePort),
            observeActiveSessionUseCase = ObserveActiveSessionUseCase(sessionStatePort),
            saveActiveSessionUseCase = SaveActiveSessionUseCase(sessionStatePort)
        )

        val viewModel = TripViewModel(FakeContext(), tripUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isTripActive.value).isFalse()
    }

    private class FakeContext : android.content.ContextWrapper(null) {
        override fun getSystemService(name: String): Any? = null
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long = 1L
        override suspend fun getAllTrips(): List<Trip> = emptyList()
        override suspend fun getTripById(tripId: Long): Trip? = null
        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = emptyList()
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> = emptyList()
        override suspend fun getTotalTripsCount(): Int = 0
        override suspend fun getTotalDistanceKm(): Double = 0.0
        override suspend fun getChargeDetectionsCount(threshold: Int): Int = 0
    }

    private class FakeSessionStatePort : SessionStatePort {
        override suspend fun saveActiveSession(session: ActiveSession) {}
        override suspend fun getActiveSession(): ActiveSession? = null
        override fun observeActiveSession(): Flow<ActiveSession?> = flowOf(null)
        override suspend fun clearActiveSession() {}
    }

    private class FakeEvConfigPort : EvConfigPort {
        override suspend fun getEvConfig(): EvConfig? = EvConfig(brand = "VSETT")
        override suspend fun saveEvConfig(config: EvConfig): Long = 1L
    }

    private class FakeEvDataPort : EvDataPort {
        override suspend fun getLatestEvData(): EvData? = null
        override suspend fun saveEvData(evData: EvData): Long = 1L
        override suspend fun getAllEvData(): List<EvData> = emptyList()
        override suspend fun updateOdometer(evCode: String, newKm: Long, currentBatteryPercentage: Short): Long = 1L
    }

    private class FakeMotionDetectorPort : MotionDetectorPort {
        private val _state = MutableStateFlow(MotionState.STOPPED)
        override val motionState: StateFlow<MotionState> = _state.asStateFlow()
        override fun start() {}
        override fun stop() {}
        override fun processSensorData(accX: Float, accY: Float, accZ: Float, gyroX: Float, gyroY: Float, gyroZ: Float, currentTimestamp: Long) {}
        override fun updateState(newState: MotionState, currentTimestamp: Long) {}
    }
}
