package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.MotionState
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
        trackingTracker.recordTelemetry(x = 10.0, y = 20.0, speed = 20.0, distanceDelta = 1.0)
        testScheduler.runCurrent()

        trackingTracker.stopTracking()

        assertThat(trackingTracker.isTracking.value).isFalse()
        assertThat(fakeTripPort.savedTrips).hasSize(1)
    }

    @Test
    fun shouldRecordMultipleContinuousTelemetryPoints() = runTest {
        trackingTracker = ScooterTrackingTracker(fakeTripPort, fakeSessionPort, this)

        trackingTracker.startTracking()
        trackingTracker.recordTelemetry(x = 10.0001, y = 20.0001, speed = 15.0, distanceDelta = 0.05, motionState = MotionState.MOVING)
        trackingTracker.recordTelemetry(x = 10.0002, y = 20.0002, speed = 18.0, distanceDelta = 0.05, motionState = MotionState.ACCELERATING)
        trackingTracker.recordTelemetry(x = 10.0003, y = 20.0003, speed = 12.0, distanceDelta = 0.05, motionState = MotionState.BRAKING)
        testScheduler.runCurrent()

        assertThat(trackingTracker.getCachedTelemetryCount()).isEqualTo(3)

        trackingTracker.stopTracking()
        assertThat(fakeTripPort.savedGpsPoints).hasSize(3)
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        val savedTrips = mutableListOf<Trip>()
        val savedGpsPoints = mutableListOf<TripGps>()

        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}

        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
            savedTrips.add(trip)
            savedGpsPoints.addAll(gpsPoints)
            return 1L
        }

        override suspend fun getAllTrips(): List<Trip> = savedTrips

        override suspend fun getTripById(tripId: Long): Trip? = savedTrips.firstOrNull()

        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = savedGpsPoints

        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> = savedTrips

        override suspend fun getTotalTripsCount(): Int = savedTrips.size
        override suspend fun getTotalDistanceKm(): Double = savedTrips.sumOf { it.distance }
        override suspend fun getChargeDetectionsCount(threshold: Int): Int = savedTrips.count { it.batteryConsumed >= threshold }
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
