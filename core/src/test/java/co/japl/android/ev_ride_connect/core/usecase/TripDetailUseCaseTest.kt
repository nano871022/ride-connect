package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TripDetailUseCaseTest {

    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var useCase: TripDetailUseCase

    @Before
    fun setUp() {
        fakeTripPort = FakeTripDatabasePort()
        useCase = TripDetailUseCase(
            fakeTripPort,
            GetTripDetailsUseCase(fakeTripPort),
            GetGpsPointsByTripIdUseCase(fakeTripPort),
            GetTripByIdUseCase(fakeTripPort)
        )
    }

    @Test
    fun shouldGetTripDetails() = runTest {
        val details = useCase.getTripDetails(1L)

        assertThat(details).isNotNull
        assertThat(details?.first?.id).isEqualTo(1L)
        assertThat(details?.second).hasSize(1)
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>) = 1L
        override suspend fun getAllTrips() = listOf(Trip(id = 1L, distance = 10.0))
        override suspend fun getTripById(tripId: Long) = Trip(id = 1L, distance = 10.0)
        override suspend fun getGpsPointsByTripId(tripId: Long) = listOf(TripGps(orderIndex = 1, speed = 25.0, distance = 1.0, x = 4.0, y = -74.0))
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long) = emptyList<Trip>()
        override suspend fun getTotalTripsCount() = 1
        override suspend fun getTotalDistanceKm() = 10.0
        override suspend fun getChargeDetectionsCount(threshold: Int) = 0
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
    }
}
