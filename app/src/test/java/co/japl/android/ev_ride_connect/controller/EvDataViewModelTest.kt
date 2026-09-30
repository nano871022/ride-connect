package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.core.usecase.EvDataUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.GetAllEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetAllTripsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripsByDateUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class EvDataViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var viewModel: EvDataViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeEvDataPort = FakeEvDataPort()
        fakeTripPort = FakeTripDatabasePort()
        fakeEvDataPort.savedList.add(EvData(evCode = "EV01", km = 100L, batteryLevel = 80))
        fakeEvDataPort.savedList.add(EvData(evCode = "EV01", km = 120L, batteryLevel = 70))

        val evDataUseCase = EvDataUseCaseImpl(
            fakeEvDataPort,
            fakeTripPort,
            GetAllEvDataUseCase(fakeEvDataPort),
            GetAllTripsUseCase(fakeTripPort),
            GetTripsByDateUseCase(fakeTripPort)
        )
        viewModel = EvDataViewModel(evDataUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadEvDataHistoryOnInit() = runTest {
        viewModel.loadEvDataHistory()
        testScheduler.runCurrent()

        assertThat(viewModel.evDataList.value).hasSize(2)
        assertThat(viewModel.evDataList.value.first().km).isEqualTo(100L)
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

    private class FakeTripDatabasePort : TripDatabasePort {
        val trips = mutableListOf<Trip>()

        override suspend fun getAllTrips(): List<Trip> = trips
        override suspend fun saveTrip(trip: Trip, points: List<co.japl.android.ev_ride_connect.core.domain.TripGps>): Long = 1L
        override suspend fun getTripById(tripId: Long): Trip? = null
        override suspend fun getGpsPointsByTripId(tripId: Long) = emptyList<co.japl.android.ev_ride_connect.core.domain.TripGps>()
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long) = trips
        override suspend fun getTotalTripsCount() = 0
        override suspend fun getTotalDistanceKm() = 0.0
        override suspend fun getChargeDetectionsCount(threshold: Int) = 0
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
    }
}
