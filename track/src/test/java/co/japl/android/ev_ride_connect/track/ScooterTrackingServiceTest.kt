package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScooterTrackingServiceTest {

    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var trackingTracker: ScooterTrackingTracker

    @Before
    fun setUp() {
        fakeTripPort = FakeTripDatabasePort()
    }

    @Test
    fun shouldNotBeTrackingInitially() {
        trackingTracker = ScooterTrackingTracker(fakeTripPort)
        assertThat(trackingTracker.isTracking.value).isFalse()
    }

    @Test
    fun shouldStartTrackingWhenStartCalled() = runTest {
        trackingTracker = ScooterTrackingTracker(fakeTripPort, this)
        trackingTracker.startTracking()

        assertThat(trackingTracker.isTracking.value).isTrue()

        trackingTracker.stopTracking()
    }

    @Test
    fun shouldStopTrackingAndSaveTripWhenStopCalled() = runTest {
        trackingTracker = ScooterTrackingTracker(fakeTripPort, this)

        trackingTracker.startTracking()
        testScheduler.runCurrent()

        trackingTracker.stopTracking()

        assertThat(trackingTracker.isTracking.value).isFalse()
        assertThat(fakeTripPort.savedTrips).hasSize(1)
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        val savedTrips = mutableListOf<Pair<Int, Int>>()

        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {
            savedTrips.add(distance to batteryConsumed)
        }

        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
            return 1L
        }

        override suspend fun getAllTrips(): List<Trip> {
            return emptyList()
        }

        override suspend fun getTripById(tripId: Long): Trip? {
            return null
        }

        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> {
            return emptyList()
        }
    }
}
