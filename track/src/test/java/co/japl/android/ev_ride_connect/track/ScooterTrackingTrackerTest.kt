package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScooterTrackingTrackerTest {

    private lateinit var fakeTripDatabasePort: FakeTripDatabasePort
    private lateinit var fakeSessionStatePort: FakeSessionStatePort
    private lateinit var tracker: ScooterTrackingTracker
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private class FakeTripDatabasePort : TripDatabasePort {
        var savedTrips = mutableListOf<Trip>()
        var savedGpsPoints = mutableListOf<TripGps>()

        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}

        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
            savedTrips.add(trip)
            savedGpsPoints.addAll(gpsPoints)
            return 1L
        }

        override suspend fun getAllTrips(): List<Trip> = savedTrips
        override suspend fun getTripById(tripId: Long): Trip? = savedTrips.firstOrNull()
        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = savedGpsPoints
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

    @Before
    fun setUp() {
        fakeTripDatabasePort = FakeTripDatabasePort()
        fakeSessionStatePort = FakeSessionStatePort()
        tracker = ScooterTrackingTracker(
            tripDatabasePort = fakeTripDatabasePort,
            sessionStatePort = fakeSessionStatePort,
            coroutineScope = testScope
        )
    }

    @Test
    fun shouldStartTrackingAndPersistActiveSession() = runTest(testDispatcher) {
        tracker.startTracking()

        assertThat(tracker.isTracking.value).isTrue
        assertThat(fakeSessionStatePort.activeSession).isNotNull
        assertThat(fakeSessionStatePort.activeSession?.isRideActive).isTrue
    }

    @Test
    fun shouldRecordTelemetryAndCacheInSession() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 0.5)

        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(1)
        assertThat(fakeSessionStatePort.activeSession?.currentDistanceKm).isEqualTo(0.5)
        assertThat(fakeSessionStatePort.activeSession?.cachedTelemetryCount).isEqualTo(1)
    }

    @Test
    fun shouldStopTrackingSaveTripAndClearSession() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 1.0)
        tracker.stopTracking()

        assertThat(tracker.isTracking.value).isFalse
        assertThat(fakeTripDatabasePort.savedTrips).hasSize(1)
        assertThat(fakeTripDatabasePort.savedGpsPoints).hasSize(1)
        assertThat(fakeSessionStatePort.activeSession).isNull()
    }
}
