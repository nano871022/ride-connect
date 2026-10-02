package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.interfaces.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.interfaces.usecase.TripDetailUseCase
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TripDetailUseCaseTest {

    private lateinit var useCase: TripDetailUseCase

    @Before
    fun setUp() {
        val tripDatabasePort = FakeTripDatabasePort()
        useCase = TripDetailUseCaseImpl(
            tripDatabasePort = tripDatabasePort,
            getTripDetailsUseCase = GetTripDetailsUseCase(tripDatabasePort),
            getGpsPointsByTripIdUseCase = GetGpsPointsByTripIdUseCase(tripDatabasePort),
            getTripByIdUseCase = GetTripByIdUseCase(tripDatabasePort)
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
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long = 1L
        override suspend fun getAllTrips(): List<Trip> = emptyList()
        override suspend fun getTripById(tripId: Long): Trip = Trip(id = tripId)
        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = listOf(TripGps(tripId = tripId))
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> = emptyList()
        override suspend fun getTotalTripsCount(): Int = 0
        override suspend fun getTotalDistanceKm(): Double = 0.0
        override suspend fun getChargeDetectionsCount(threshold: Int): Int = 0
    }
}
