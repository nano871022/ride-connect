package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class EvDataUseCaseTest {

    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var useCase: EvDataUseCase

    @Before
    fun setUp() {
        fakeEvDataPort = FakeEvDataPort()
        fakeTripPort = FakeTripDatabasePort()
        useCase = EvDataUseCaseImpl(
            fakeEvDataPort,
            fakeTripPort,
            GetAllEvDataUseCase(fakeEvDataPort),
            GetAllTripsUseCase(fakeTripPort),
            GetTripsByDateUseCase(fakeTripPort)
        )
    }

    @Test
    fun shouldGetAllEvDataAndTrips() = runTest {
        val evDataList = useCase.getAllEvData()
        val trips = useCase.getAllTrips()

        assertThat(evDataList).hasSize(1)
        assertThat(trips).hasSize(1)
    }

    private class FakeEvDataPort : EvDataPort {
        override suspend fun getLatestEvData() = EvData(evCode = "EV01", km = 100L, batteryLevel = 80)
        override suspend fun getAllEvData() = listOf(EvData(evCode = "EV01", km = 100L, batteryLevel = 80))
        override suspend fun saveEvData(evData: EvData) = 1L
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        override suspend fun saveTrip(trip: Trip, gpsPoints: List<co.japl.android.ev_ride_connect.core.domain.TripGps>) = 1L
        override suspend fun getAllTrips() = listOf(Trip(id = 1L, distance = 10.0))
        override suspend fun getTripById(tripId: Long) = null
        override suspend fun getGpsPointsByTripId(tripId: Long) = emptyList<co.japl.android.ev_ride_connect.core.domain.TripGps>()
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long) = emptyList<Trip>()
        override suspend fun getTotalTripsCount() = 1
        override suspend fun getTotalDistanceKm() = 10.0
        override suspend fun getChargeDetectionsCount(threshold: Int) = 0
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
    }
}
