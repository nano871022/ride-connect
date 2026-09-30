package co.japl.android.ev_ride_connect.controller

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.BatteryMode
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
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
import co.japl.android.ev_ride_connect.core.usecase.GetGpsPointsByTripIdUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetLatestEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripByIdUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripDetailsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripsByDateUseCase
import co.japl.android.ev_ride_connect.core.usecase.ObserveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.PauseTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.ResumeTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveTripUseCase
import co.japl.android.ev_ride_connect.interfaces.usecase.TripUseCaseImpl
import co.japl.android.ev_ride_connect.ui.HistoryFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import uk.co.jemos.podam.api.PodamFactoryImpl

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TripViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val podamFactory = PodamFactoryImpl()

    private lateinit var context: Context
    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var fakeSessionPort: FakeSessionStatePort
    private lateinit var fakeEvConfigPort: FakeEvConfigPort
    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeMotionPort: FakeMotionDetectorPort
    private lateinit var viewModel: TripViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        fakeTripPort = FakeTripDatabasePort()
        fakeSessionPort = FakeSessionStatePort()
        fakeEvConfigPort = FakeEvConfigPort()
        fakeEvDataPort = FakeEvDataPort()
        fakeMotionPort = FakeMotionDetectorPort()

        val getLatestEvDataUseCase = GetLatestEvDataUseCase(fakeEvDataPort)
        val getEvConfigUseCase = GetEvConfigUseCase(fakeEvConfigPort)

        val tripUseCase = TripUseCaseImpl(
            fakeTripPort,
            fakeSessionPort,
            fakeEvConfigPort,
            fakeEvDataPort,
            fakeMotionPort,
            SaveTripUseCase(fakeTripPort),
            EndTripUseCase(fakeSessionPort),
            PauseTripUseCase(fakeSessionPort),
            ResumeTripUseCase(fakeSessionPort),
            CalculateTripSummaryUseCase(fakeTripPort),
            CalculateCo2SavedUseCase(),
            CalculateConsumptionUseCase(),
            CalculateDynamicBatteryPercentageUseCase(),
            GetTripDetailsUseCase(fakeTripPort),
            GetTripsByDateUseCase(fakeTripPort),
            getEvConfigUseCase,
            getLatestEvDataUseCase,
            SaveEvDataUseCase(fakeEvDataPort),
            GetActiveSessionUseCase(fakeSessionPort),
            ObserveActiveSessionUseCase(fakeSessionPort),
            SaveActiveSessionUseCase(fakeSessionPort)
        )

        viewModel = TripViewModel(context, tripUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldInitializeWithTripInactive() = runTest {
        assertThat(viewModel.isTripActive.value).isFalse()
        assertThat(viewModel.isPaused.value).isFalse()
        assertThat(viewModel.elapsedTimeSeconds.value).isEqualTo(0L)
        assertThat(viewModel.currentDistance.value).isEqualTo(0.0)
    }

    @Test
    fun shouldStartTripAndSetStateActive() = runTest {
        viewModel.startTrip()
        testScheduler.runCurrent()

        assertThat(viewModel.isTripActive.value).isTrue()
        assertThat(viewModel.isPaused.value).isFalse()
    }

    @Test
    fun shouldPauseAndResumeTrip() = runTest {
        viewModel.startTrip()
        testScheduler.runCurrent()

        viewModel.pauseTrip()
        testScheduler.runCurrent()

        assertThat(viewModel.isPaused.value).isTrue()

        viewModel.resumeTrip()
        testScheduler.runCurrent()

        assertThat(viewModel.isPaused.value).isFalse()
    }

    @Test
    fun shouldFilterTripsByDate() = runTest {
        val trip1 = Trip(id = 1L, distance = 10.0, timeTrip = 600L, createTmst = System.currentTimeMillis() - 86400000L)
        fakeTripPort.trips.add(trip1)

        viewModel.filterTripsByDate(HistoryFilter.WEEK)
        testScheduler.runCurrent()

        assertThat(viewModel.selectedFilter.value).isEqualTo(HistoryFilter.WEEK)
        assertThat(viewModel.tripHistory.value).hasSize(1)

        viewModel.filterTripsByDate(HistoryFilter.ALL)
        testScheduler.runCurrent()

        assertThat(viewModel.selectedFilter.value).isEqualTo(HistoryFilter.ALL)
    }

    @Test
    fun shouldAddLocationPointAndCalculateMetrics() = runTest {
        viewModel.startTrip()
        testScheduler.runCurrent()

        viewModel.addLocationPoint(4.60971, -74.08175, timestamp = 1000L)
        viewModel.addLocationPoint(4.61000, -74.08200, timestamp = 5000L)

        assertThat(viewModel.recordedGpsCount.value).isEqualTo(2)
        assertThat(viewModel.currentDistance.value).isGreaterThan(0.0)
    }

    @Test
    fun shouldStopTripAndSaveData() = runTest {
        viewModel.startTrip()
        testScheduler.runCurrent()

        viewModel.addLocationPoint(4.60971, -74.08175)
        viewModel.stopTrip(batteryConsumed = 15)

        testScheduler.runCurrent()

        assertThat(viewModel.isTripActive.value).isFalse()
        assertThat(fakeTripPort.savedTrip).isNotNull
        assertThat(fakeTripPort.savedTrip?.batteryConsumed).isEqualTo(15)
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        val trips = mutableListOf<Trip>()
        var savedTrip: Trip? = null
        var savedGpsPoints: List<TripGps> = emptyList()

        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
            savedTrip = trip
            savedGpsPoints = gpsPoints
            trips.add(trip)
            return trip.id.takeIf { it > 0 } ?: trips.size.toLong()
        }

        override suspend fun getAllTrips(): List<Trip> = trips.toList()

        override suspend fun getTripById(tripId: Long): Trip? = trips.find { it.id == tripId }

        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = savedGpsPoints

        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> {
            return trips.filter { it.createTmst in startTimestamp..endTimestamp }
        }

        override suspend fun getTotalTripsCount(): Int = trips.size
        override suspend fun getTotalDistanceKm(): Double = trips.sumOf { it.distance }
        override suspend fun getChargeDetectionsCount(threshold: Int): Int = 0
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
    }

    private class FakeSessionStatePort : SessionStatePort {
        var activeSession: ActiveSession? = null

        override suspend fun saveActiveSession(session: ActiveSession) {
            activeSession = session
        }

        override suspend fun getActiveSession(): ActiveSession? = activeSession

        override fun observeActiveSession(): Flow<ActiveSession?> = MutableStateFlow(activeSession)

        override suspend fun clearActiveSession() {
            activeSession = null
        }
    }

    private class FakeEvConfigPort : EvConfigPort {
        var config: EvConfig? = EvConfig(
            id = 1L,
            batteryVolts = "52V",
            batteryAmpers = "20Ah",
            batteryMode = BatteryMode.VOLTAGE,
            minVoltage = 39.0,
            maxVoltage = 54.6
        )

        override suspend fun getEvConfig(): EvConfig? = config
        override suspend fun saveEvConfig(config: EvConfig): Long = config.id
    }

    private class FakeEvDataPort : EvDataPort {
        var latestEvData: EvData? = EvData(evCode = "EV01", km = 100L, batteryLevel = 90)

        override suspend fun getLatestEvData(): EvData? = latestEvData
        override suspend fun getAllEvData(): List<EvData> = listOfNotNull(latestEvData)
        override suspend fun saveEvData(evData: EvData): Long {
            latestEvData = evData
            return 1L
        }
    }

    private class FakeMotionDetectorPort : MotionDetectorPort {
        private val _motionState = MutableStateFlow(co.japl.android.ev_ride_connect.interfaces.model.MotionState.STOPPED)
        override val motionState: kotlinx.coroutines.flow.StateFlow<co.japl.android.ev_ride_connect.interfaces.model.MotionState> = _motionState

        override fun start() {}
        override fun stop() {}
        override fun processSensorData(
            accX: Float, accY: Float, accZ: Float,
            gyroX: Float, gyroY: Float, gyroZ: Float,
            currentTimestamp: Long
        ) {}
        override fun updateState(newState: co.japl.android.ev_ride_connect.interfaces.model.MotionState, currentTimestamp: Long) {
            _motionState.value = newState
        }
    }
}
