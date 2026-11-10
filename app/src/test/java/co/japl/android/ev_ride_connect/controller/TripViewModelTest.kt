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
import co.japl.android.ev_ride_connect.core.usecase.api.CalculateMaxSpeedUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateCo2SavedUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateConsumptionUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateDynamicBatteryPercentageUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateMaxSpeedUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateTripSummaryUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.EndTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.FinalizeTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.GetActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.GetLatestEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.GetTripDetailsUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.GetTripsByDateUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.ObserveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.PauseTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.ResumeTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.SaveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.SaveEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.SaveTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.TripUseCaseImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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

        val calculateMaxSpeedUseCase: CalculateMaxSpeedUseCase = CalculateMaxSpeedUseCaseImpl()
        val finalizeTripUseCase = FinalizeTripUseCase(
            calculateMaxSpeedUseCase,
            SaveTripUseCase(tripDatabasePort)
        )

        val viewModel = TripViewModel(FakeContext(), tripUseCase, finalizeTripUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isTripActive.value).isFalse()
    }

    private class FakeContext : android.content.ContextWrapper(null) {
        override fun getSystemService(name: String): Any? = null
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        internal val savedTrips = mutableListOf<Trip>()

        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
            savedTrips.add(trip)
            return savedTrips.size.toLong()
        }
        override suspend fun getAllTrips(): List<Trip> = savedTrips
        override suspend fun getTripById(tripId: Long): Trip? = savedTrips.firstOrNull { it.id == tripId }
        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = emptyList()
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> = emptyList()
        override suspend fun getTotalTripsCount(): Int = savedTrips.size
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

    // --- RF-05: Historial se recarga automáticamente tras guardar viaje ---
    @Test
    fun shouldRefreshTripHistoryAfterStopTrip() = runTest {
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

        val calculateMaxSpeedUseCase2: CalculateMaxSpeedUseCase = CalculateMaxSpeedUseCaseImpl()
        val finalizeTripUseCase2 = FinalizeTripUseCase(
            calculateMaxSpeedUseCase2,
            SaveTripUseCase(tripDatabasePort)
        )

        val viewModel = TripViewModel(FakeContext(), tripUseCase, finalizeTripUseCase2)

        // RF-05: Guardar un viaje directamente a través del caso de uso
        val trip = Trip(
            id = 0,
            timeTrip = 300,
            distance = 2.5,
            batteryConsumed = 10,
            createTmst = System.currentTimeMillis()
        )
        tripUseCase.saveTrip(trip, emptyList())

        // RF-05: Recargar historial tras guardar el viaje
        viewModel.loadTripHistory()

        // El historial debe incluir el viaje recién guardado
        assertThat(viewModel._tripHistory.value).isNotEmpty()
        assertThat(viewModel._tripHistory.value?.firstOrNull()?.distance).isEqualTo(2.5)
    }
}

