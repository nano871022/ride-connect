package co.japl.android.ev_ride_connect.controller

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
@Config(sdk = [34])
class TripViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val podamFactory = PodamFactoryImpl()
    private lateinit var context: Context
    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeEvConfigPort: FakeEvConfigPort
    private lateinit var viewModel: TripViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        fakeTripPort = FakeTripDatabasePort()
        fakeEvDataPort = FakeEvDataPort()
        fakeEvConfigPort = FakeEvConfigPort()
        viewModel = TripViewModel(context, fakeTripPort, fakeEvDataPort, fakeEvConfigPort)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldSetGpsIntervalAndBatteryWarning() {
        viewModel.setGpsInterval(15L)
        assertThat(viewModel.gpsIntervalSeconds.value).isEqualTo(15L)
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

        viewModel.confirmStartTrip(80)
        testScheduler.runCurrent()

        assertThat(viewModel.showStartBatteryDialog.value).isFalse()
        assertThat(viewModel.isTripActive.value).isTrue()
        assertThat(fakeEvDataPort.savedList).hasSize(2)
        assertThat(fakeEvDataPort.savedList.last().batteryLevel).isEqualTo(80.toShort())
    }

    @Test
    fun shouldStartAndStopTripAndSaveToDatabaseWithEvData() = runTest {
        fakeEvDataPort.savedList.add(EvData(evCode = "1", km = 100L, batteryLevel = 80))

        viewModel.startTrip()
        assertThat(viewModel.isTripActive.value).isTrue()

        val time1 = System.currentTimeMillis()
        viewModel.addLocationPoint(4.6097, -74.0817, time1)

        val time2 = time1 + 900_000L
        viewModel.addLocationPoint(4.7097, -74.0817, time2)

        assertThat(viewModel.currentDistance.value).isGreaterThan(0.0)

        viewModel.onStopTripRequested()
        testScheduler.runCurrent()

        assertThat(viewModel.showEndBatteryDialog.value).isTrue()
        assertThat(viewModel.calculatedNewKm.value).isGreaterThanOrEqualTo(100L)

        viewModel.confirmStopTrip(70)
        testScheduler.advanceUntilIdle()

        assertThat(viewModel.isTripActive.value).isFalse()
        assertThat(fakeTripPort.savedTrips).hasSize(1)
        assertThat(fakeEvDataPort.savedList).hasSize(2)
        assertThat(fakeEvDataPort.savedList.last().batteryLevel).isEqualTo(70.toShort())
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
        testScheduler.advanceUntilIdle()

        val detail = viewModel.selectedTripDetail.value
        assertThat(detail).isNotNull
        assertThat(detail?.first?.id).isEqualTo(1L)
        assertThat(detail?.second).hasSize(2)
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
}
