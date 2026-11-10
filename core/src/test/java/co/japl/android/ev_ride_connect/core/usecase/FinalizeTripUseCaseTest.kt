package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.usecase.api.CalculateMaxSpeedUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateMaxSpeedUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.FinalizeTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.SaveTripUseCase
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.interfaces.ports.TripDatabasePort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class FinalizeTripUseCaseTest {

    private lateinit var useCase: FinalizeTripUseCase
    private lateinit var fakeSaveTripUseCase: FakeSaveTripUseCase
    private lateinit var fakeTripDatabasePort: FakeTripDatabasePort

    @Before
    fun setUp() {
        fakeTripDatabasePort = FakeTripDatabasePort()
        fakeSaveTripUseCase = FakeSaveTripUseCase(fakeTripDatabasePort)
        val calculateMaxSpeedUseCase: CalculateMaxSpeedUseCase = CalculateMaxSpeedUseCaseImpl()
        useCase = FinalizeTripUseCase(calculateMaxSpeedUseCase, fakeSaveTripUseCase)
    }

    @Test
    fun shouldCalculateAndPersistMaxSpeedFromGpsPoints() = runTest {
        val gpsPoints = listOf(
            TripGps(orderIndex = 1, speed = 0.0),
            TripGps(orderIndex = 2, speed = 5.0),
            TripGps(orderIndex = 3, speed = 12.0),
            TripGps(orderIndex = 4, speed = 20.0),
            TripGps(orderIndex = 5, speed = 15.0),
            TripGps(orderIndex = 6, speed = 8.0)
        )
        val trip = Trip(id = 0, maxSpeed = 0.0, distance = 2.5, timeTrip = 600, createTmst = 1000L)

        val tripId = useCase.execute(trip, gpsPoints)

        // Debe calcular maxSpeed = 20.0 y persistirlo en el trip guardado
        assertThat(fakeSaveTripUseCase.savedTrips).hasSize(1)
        assertThat(fakeSaveTripUseCase.savedTrips[0].maxSpeed).isEqualTo(20.0)
        assertThat(tripId).isGreaterThan(0L)
    }

    @Test
    fun shouldReturnZeroMaxSpeedWhenNoGpsPoints() = runTest {
        val trip = Trip(id = 0, maxSpeed = 0.0, createTmst = 1000L)
        val tripId = useCase.execute(trip, emptyList())

        assertThat(fakeSaveTripUseCase.savedTrips).hasSize(1)
        assertThat(fakeSaveTripUseCase.savedTrips[0].maxSpeed).isEqualTo(0.0)
        assertThat(tripId).isGreaterThan(0L)
    }

    @Test
    fun shouldSaveExactlyOneTripNoDuplicates() = runTest {
        val gpsPoints = listOf(TripGps(orderIndex = 1, speed = 10.0))
        val trip = Trip(id = 0, maxSpeed = 0.0, createTmst = 1000L)

        useCase.execute(trip, gpsPoints)

        assertThat(fakeSaveTripUseCase.saveTripCallCount).isEqualTo(1)
    }

    private class FakeSaveTripUseCase(
        private val tripDatabasePort: TripDatabasePort
    ) : co.japl.android.ev_ride_connect.core.usecase.impl.TripWriterUseCase {
        var saveTripCallCount = 0
        val savedTrips = mutableListOf<Trip>()

        override suspend fun execute(trip: Trip, gpsPoints: List<TripGps>): Long {
            saveTripCallCount++
            savedTrips.add(trip)
            return tripDatabasePort.saveTrip(trip, gpsPoints)
        }
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long = trip.id.let { if (it == 0L) 1L else it }
        override suspend fun getAllTrips(): List<Trip> = emptyList()
        override suspend fun getTripById(tripId: Long): Trip? = null
        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = emptyList()
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> = emptyList()
        override suspend fun getTotalTripsCount(): Int = 0
        override suspend fun getTotalDistanceKm(): Double = 0.0
        override suspend fun getChargeDetectionsCount(threshold: Int): Int = 0
    }
}
