package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScooterTrackingServiceTest {

    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var fakeSessionPort: FakeSessionStatePort
    private lateinit var trackingTracker: ScooterTrackingTracker

    @Before
    fun setUp() {
        fakeTripPort = FakeTripDatabasePort()
        fakeSessionPort = FakeSessionStatePort()
    }

    @Test
    fun shouldNotBeTrackingInitially() {
        trackingTracker = ScooterTrackingTracker(fakeTripPort, fakeSessionPort)
        assertThat(trackingTracker.isTracking.value).isFalse()
    }

    @Test
    fun shouldStartTrackingWhenStartCalled() = runTest {
        trackingTracker = ScooterTrackingTracker(fakeTripPort, fakeSessionPort, this)
        trackingTracker.startTracking()

        assertThat(trackingTracker.isTracking.value).isTrue()

        trackingTracker.stopTracking()
    }

    @Test
    fun shouldStopTrackingAndSaveTripWhenStopCalled() = runTest {
        trackingTracker = ScooterTrackingTracker(fakeTripPort, fakeSessionPort, this)

        trackingTracker.startTracking()
        testScheduler.runCurrent()

        trackingTracker.stopTracking()

        assertThat(trackingTracker.isTracking.value).isFalse()
        assertThat(fakeTripPort.savedTrips).hasSize(1)
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        val savedTrips = mutableListOf<Trip>()

        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}

        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
            savedTrips.add(trip)
            return 1L
        }

        override suspend fun getAllTrips(): List<Trip> = savedTrips

        override suspend fun getTripById(tripId: Long): Trip? = savedTrips.firstOrNull()

        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = emptyList()
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
