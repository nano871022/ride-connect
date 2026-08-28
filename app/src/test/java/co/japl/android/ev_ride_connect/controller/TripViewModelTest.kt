package co.japl.android.ev_ride_connect.controller

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
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
    private lateinit var viewModel: TripViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        fakeTripPort = FakeTripDatabasePort()
        viewModel = TripViewModel(context, fakeTripPort)
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
    fun shouldStartAndStopTripAndSaveToDatabase() = runTest {
        viewModel.startTrip()
        assertThat(viewModel.isTripActive.value).isTrue()

        val time1 = System.currentTimeMillis()
        viewModel.addLocationPoint(4.6097, -74.0817, time1)

        val time2 = time1 + 900_000L
        viewModel.addLocationPoint(4.7097, -74.0817, time2)

        assertThat(viewModel.currentDistance.value).isGreaterThan(0.0)

        viewModel.stopTrip()
        testScheduler.advanceUntilIdle()

        assertThat(viewModel.isTripActive.value).isFalse()
        assertThat(fakeTripPort.savedTrips).hasSize(1)
        assertThat(fakeTripPort.savedGpsMap[fakeTripPort.savedTrips[0].id]).hasSize(2)
        assertThat(viewModel.tripHistory.value).hasSize(1)
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
}
