package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import co.japl.android.ev_ride_connect.interfaces.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.interfaces.usecase.EvDataUseCase
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class EvDataUseCaseTest {

    private lateinit var useCase: EvDataUseCase

    @Before
    fun setUp() {
        val evDataPort = FakeEvDataPort()
        val tripDatabasePort = FakeTripDatabasePort()

        useCase = EvDataUseCaseImpl(
            evDataPort = evDataPort,
            tripDatabasePort = tripDatabasePort,
            getAllEvDataUseCase = GetAllEvDataUseCase(evDataPort),
            getAllTripsUseCase = GetAllTripsUseCase(tripDatabasePort),
            getTripsByDateUseCase = GetTripsByDateUseCase(tripDatabasePort)
        )
    }

    @Test
    fun shouldGetAllEvDataAndTrips() = runTest {
        val evDataList = useCase.getAllEvData()
        val tripsList = useCase.getAllTrips()

        assertThat(evDataList).hasSize(1)
        assertThat(tripsList).hasSize(1)
    }

    private class FakeEvDataPort : EvDataPort {
        override suspend fun getLatestEvData(): EvData? = null
        override suspend fun saveEvData(evData: EvData): Long = 1L
        override suspend fun getAllEvData(): List<EvData> = listOf(EvData(evCode = "EV01"))
        override suspend fun updateOdometer(evCode: String, newKm: Long, currentBatteryPercentage: Short): Long = 1L
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
        override suspend fun saveTrip(trip: Trip, gpsPoints: List<co.japl.android.ev_ride_connect.interfaces.model.TripGps>): Long = 1L
        override suspend fun getAllTrips(): List<Trip> = listOf(Trip(id = 1L))
        override suspend fun getTripById(tripId: Long): Trip? = null
        override suspend fun getGpsPointsByTripId(tripId: Long): List<co.japl.android.ev_ride_connect.interfaces.model.TripGps> = emptyList()
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> = emptyList()
        override suspend fun getTotalTripsCount(): Int = 1
        override suspend fun getTotalDistanceKm(): Double = 10.0
        override suspend fun getChargeDetectionsCount(threshold: Int): Int = 0
    }
}
