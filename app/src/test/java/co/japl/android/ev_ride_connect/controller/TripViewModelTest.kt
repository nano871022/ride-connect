package co.japl.android.ev_ride_connect.controller

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.BatteryMode
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.core.usecase.CalculateCo2SavedUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateConsumptionUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateDynamicBatteryPercentageUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateTripSummaryUseCase
import co.japl.android.ev_ride_connect.core.usecase.EndTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetAllTripsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetGpsPointsByTripIdUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetLatestEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripByIdUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripDetailsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripsByDateUseCase
import co.japl.android.ev_ride_connect.core.usecase.ObserveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.PauseTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.ResumeTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveTripUseCase
import co.japl.android.ev_ride_connect.ui.HistoryFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeEvConfigPort: FakeEvConfigPort
    private lateinit var fakeSessionStatePort: FakeSessionStatePort

    private lateinit var viewModel: TripViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()

        fakeTripPort = FakeTripDatabasePort()
        fakeEvDataPort = FakeEvDataPort()
        fakeEvConfigPort = FakeEvConfigPort()
        fakeSessionStatePort = FakeSessionStatePort()

        viewModel = TripViewModel(
            context = context,
            saveTripUseCase = SaveTripUseCase(fakeTripPort),
            getAllTripsUseCase = GetAllTripsUseCase(fakeTripPort),
            getTripByIdUseCase = GetTripByIdUseCase(fakeTripPort),
            getGpsPointsByTripIdUseCase = GetGpsPointsByTripIdUseCase(fakeTripPort),
            getLatestEvDataUseCase = GetLatestEvDataUseCase(fakeEvDataPort),
            saveEvDataUseCase = SaveEvDataUseCase(fakeEvDataPort),
            getEvConfigUseCase = GetEvConfigUseCase(fakeEvConfigPort),
            observeActiveSessionUseCase = ObserveActiveSessionUseCase(fakeSessionStatePort),
            pauseTripUseCase = PauseTripUseCase(fakeSessionStatePort),
            resumeTripUseCase = ResumeTripUseCase(fakeSessionStatePort),
            endTripUseCase = EndTripUseCase(fakeSessionStatePort),
            calculateTripSummaryUseCase = CalculateTripSummaryUseCase(fakeTripPort),
            getTripsByDateUseCase = GetTripsByDateUseCase(fakeTripPort),
            getTripDetailsUseCase = GetTripDetailsUseCase(fakeTripPort),
            calculateDynamicBatteryPercentageUseCase = CalculateDynamicBatteryPercentageUseCase(),
            calculateCo2SavedUseCase = CalculateCo2SavedUseCase(),
            calculateConsumptionUseCase = CalculateConsumptionUseCase()
        )
    }

    @After
    fun tearDown() {
        if (viewModel.isTripActive.value) {
            viewModel.stopTrip()
        }
        Dispatchers.resetMain()
    }

    @Test
    fun shouldSetGpsIntervalAndBatteryWarning() {
        viewModel.setGpsInterval(15L)
        assertThat(viewModel.gpsIntervalSeconds.value).isEqualTo(15L)

        viewModel.setGpsInterval(10L)
        assertThat(viewModel.showBatteryWarning.value).isTrue()

        viewModel.setGpsInterval(60L)
        assertThat(viewModel.gpsIntervalSeconds.value).isEqualTo(60L)
        assertThat(viewModel.showBatteryWarning.value).isFalse()
    }

    @Test
    fun shouldRequestAndConfirmStartTrip() = runTest {
        fakeEvDataPort.savedList.add(EvData(evCode = "1", km = 100L, batteryLevel = 85))

        viewModel.onStartTripRequested()
        testScheduler.runCurrent()

        assertThat(viewModel.showStartBatteryDialog.value).isTrue()
        assertThat(viewModel.latestBatteryLevel.value).isEqualTo(85.toShort())

        viewModel.confirmStartTrip(80.0)
        testScheduler.runCurrent()

        assertThat(viewModel.showStartBatteryDialog.value).isFalse()
        assertThat(viewModel.isTripActive.value).isTrue()

        viewModel.stopTrip()
        testScheduler.runCurrent()
    }

    @Test
    fun shouldPauseAndResumeTrip() = runTest {
        viewModel.confirmStartTrip(80.0)
        testScheduler.runCurrent()
        assertThat(viewModel.isTripActive.value).isTrue()
        assertThat(viewModel.isPaused.value).isFalse()

        viewModel.pauseTrip()
        testScheduler.runCurrent()

        assertThat(viewModel.isPaused.value).isTrue()

        viewModel.addLocationPoint(4.6097, -74.0817)
        assertThat(viewModel.recordedGpsPoints).isEmpty()

        viewModel.resumeTrip()
        testScheduler.runCurrent()

        assertThat(viewModel.isPaused.value).isFalse()

        viewModel.addLocationPoint(4.6097, -74.0817)
        assertThat(viewModel.recordedGpsPoints).hasSize(1)

        viewModel.stopTrip()
    }

    @Test
    fun shouldDiscardConsecutiveDuplicateLocationPoints() = runTest {
        viewModel.confirmStartTrip(80.0)
        testScheduler.runCurrent()

        viewModel.addLocationPoint(4.6097, -74.0817)
        assertThat(viewModel.recordedGpsPoints).hasSize(1)

        // Adding exact same coordinates consecutively
        viewModel.addLocationPoint(4.6097, -74.0817)
        assertThat(viewModel.recordedGpsPoints).hasSize(1)

        // Adding different coordinates
        viewModel.addLocationPoint(4.6098, -74.0818)
        assertThat(viewModel.recordedGpsPoints).hasSize(2)

        viewModel.stopTrip()
    }

    @Test
    fun shouldStartAndStopTripAndDisplayTripSummary() = runTest {
        fakeEvDataPort.savedList.add(EvData(evCode = "1", km = 100L, batteryLevel = 80))

        viewModel.confirmStartTrip(80.0)
        testScheduler.runCurrent()

        assertThat(viewModel.isTripActive.value).isTrue()

        val time1 = System.currentTimeMillis()
        viewModel.addLocationPoint(4.6097, -74.0817, time1)

        val time2 = time1 + 900_000L
        viewModel.addLocationPoint(4.7097, -74.0817, time2)

        assertThat(viewModel.currentDistance.value).isGreaterThan(0.0)

        viewModel.onStopTripRequested()
        testScheduler.runCurrent()

        assertThat(viewModel.showEndBatteryDialog.value).isTrue()

        viewModel.confirmStopTrip(70.0)
        testScheduler.runCurrent()

        assertThat(viewModel.isTripActive.value).isFalse()
        assertThat(viewModel.showSummaryDialog.value).isTrue()
        assertThat(viewModel.tripSummary.value).isNotNull()
        assertThat(viewModel.tripSummary.value?.totalGpsLocationsCount).isEqualTo(2)
        assertThat(viewModel.tripSummary.value?.batteryConsumedPercentage).isEqualTo(10)

        viewModel.dismissSummaryDialog()
        assertThat(viewModel.showSummaryDialog.value).isFalse()
        assertThat(viewModel.tripSummary.value).isNull()
    }

    @Test
    fun shouldStartAndStopTripInVoltageMode() = runTest {
        fakeEvConfigPort.config = EvConfig(
            id = 1L,
            batteryMode = BatteryMode.VOLTAGE,
            minVoltage = 39.0,
            maxVoltage = 54.6
        )

        viewModel.loadEvConfig()
        testScheduler.runCurrent()

        // 54.6V = 100%
        viewModel.confirmStartTrip(54.6)
        testScheduler.runCurrent()

        val time1 = System.currentTimeMillis()
        viewModel.addLocationPoint(4.6097, -74.0817, time1)
        val time2 = time1 + 900_000L
        viewModel.addLocationPoint(4.7097, -74.0817, time2)

        viewModel.onStopTripRequested()
        testScheduler.runCurrent()

        // 46.8V = 50%
        viewModel.confirmStopTrip(46.8)
        testScheduler.runCurrent()

        assertThat(viewModel.showSummaryDialog.value).isTrue()
        assertThat(viewModel.tripSummary.value?.batteryConsumedPercentage).isEqualTo(50)
    }

    @Test
    fun shouldLoadTripDetail() = runTest {
        val trip = podamFactory.manufacturePojo(Trip::class.java).copy(id = 1L)
        val gpsPoints = listOf(
            podamFactory.manufacturePojo(TripGps::class.java).copy(id = 1L, tripId = 1L),
            podamFactory.manufacturePojo(TripGps::class.java).copy(id = 2L, tripId = 1L)
        )
        fakeTripPort.saveTrip(trip, gpsPoints)

        viewModel.loadTripDetail(1L)
        testScheduler.runCurrent()

        val detail = viewModel.selectedTripDetail.value
        assertThat(detail).isNotNull
        assertThat(detail?.first?.id).isEqualTo(1L)
        assertThat(detail?.second).hasSize(2)
    }

    @Test
    fun shouldFilterTripsByDate() = runTest {
        val now = System.currentTimeMillis()
        val recentTrip = Trip(id = 1L, createTmst = now - 3600_000L, batteryConsumed = 10)
        val oldTrip = Trip(id = 2L, createTmst = now - (40L * 24 * 3600 * 1000), batteryConsumed = 0)

        fakeTripPort.saveTrip(recentTrip, emptyList())
        fakeTripPort.saveTrip(oldTrip, emptyList())

        viewModel.filterTripsByDate(HistoryFilter.WEEK)
        testScheduler.runCurrent()

        assertThat(viewModel.selectedFilter.value).isEqualTo(HistoryFilter.WEEK)
        assertThat(viewModel.tripHistory.value).hasSize(1)
        assertThat(viewModel.tripHistory.value[0].id).isEqualTo(1L)

        viewModel.filterTripsByDate(HistoryFilter.ALL)
        testScheduler.runCurrent()

        assertThat(viewModel.tripHistory.value).hasSize(2)
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        val savedTrips = mutableListOf<Trip>()
        val savedGpsMap = mutableMapOf<Long, List<TripGps>>()
        private var nextId = 1L

        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}

        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
            val assignedId = if (trip.id == 0L) nextId++ else trip.id
            val savedTrip = trip.copy(id = assignedId)
            savedTrips.add(savedTrip)
            savedGpsMap[assignedId] = gpsPoints.map { it.copy(tripId = assignedId) }
            return assignedId
        }

        override suspend fun getAllTrips(): List<Trip> {
            return savedTrips.sortedByDescending { it.createTmst }
        }

        override suspend fun getTripById(tripId: Long): Trip? {
            return savedTrips.find { it.id == tripId }
        }

        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> {
            return savedGpsMap[tripId] ?: emptyList()
        }

        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> {
            return savedTrips.filter { it.createTmst in startTimestamp..endTimestamp }.sortedByDescending { it.createTmst }
        }

        override suspend fun getTotalTripsCount(): Int = savedTrips.size
        override suspend fun getTotalDistanceKm(): Double = savedTrips.sumOf { it.distance }
        override suspend fun getChargeDetectionsCount(threshold: Int): Int = savedTrips.count { it.batteryConsumed >= threshold }
    }

    private class FakeEvDataPort : EvDataPort {
        val savedList = mutableListOf<EvData>()

        override suspend fun getLatestEvData(): EvData? {
            return savedList.lastOrNull()
        }

        override suspend fun getAllEvData(): List<EvData> {
            return savedList.toList()
        }

        override suspend fun saveEvData(evData: EvData): Long {
            savedList.add(evData)
            return savedList.size.toLong()
        }
    }

    private class FakeEvConfigPort : EvConfigPort {
        var config: EvConfig? = null

        override suspend fun getEvConfig(): EvConfig? = config

        override suspend fun saveEvConfig(config: EvConfig): Long {
            this.config = config
            return 1L
        }
    }

    private class FakeSessionStatePort : SessionStatePort {
        var activeSession: ActiveSession? = null

        override suspend fun saveActiveSession(session: ActiveSession) {
            activeSession = session
        }

        override suspend fun getActiveSession(): ActiveSession? = activeSession

        override fun observeActiveSession(): Flow<ActiveSession?> = flowOf(activeSession)

        override suspend fun clearActiveSession() {
            activeSession = null
        }
    }
}
