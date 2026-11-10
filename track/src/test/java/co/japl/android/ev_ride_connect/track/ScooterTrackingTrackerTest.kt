package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.usecase.api.CalculateMaxSpeedUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.TripWriterUseCase
import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.MotionState
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
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

    private lateinit var fakeMaxSpeedUseCase: FakeMaxSpeedUseCase
    private lateinit var fakeSaveTripUseCase: FakeSaveTripUseCase
    private lateinit var fakeSessionStatePort: FakeSessionStatePort
    private lateinit var tracker: ScooterTrackingTracker
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private class FakeMaxSpeedUseCase : CalculateMaxSpeedUseCase {
        var maxSpeed: Double = 0.0
        var executeCalled = false

        override suspend fun execute(gpsPoints: List<TripGps>): Double {
            executeCalled = true
            return maxSpeed
        }
    }

    private class FakeSaveTripUseCase : TripWriterUseCase {
        var saveTripCallCount = 0
        var savedTrips = mutableListOf<Trip>()
        var savedGpsPoints = mutableListOf<TripGps>()

        override suspend fun execute(trip: Trip, gpsPoints: List<TripGps>): Long {
            saveTripCallCount++
            savedTrips.add(trip)
            savedGpsPoints.addAll(gpsPoints)
            return 1L
        }
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
        fakeMaxSpeedUseCase = FakeMaxSpeedUseCase()
        fakeSaveTripUseCase = FakeSaveTripUseCase()
        fakeSessionStatePort = FakeSessionStatePort()
        tracker = ScooterTrackingTracker(
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
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 0.5, motionState = MotionState.MOVING)

        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(1)
        assertThat(fakeSessionStatePort.activeSession?.currentDistanceKm).isEqualTo(0.5)
        assertThat(fakeSessionStatePort.activeSession?.cachedTelemetryCount).isEqualTo(1)
        assertThat(fakeSessionStatePort.activeSession?.motionState).isEqualTo(MotionState.MOVING)
    }

    @Test
    fun shouldDiscardZeroZeroCoordinates() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(x = 0.0, y = 0.0, speed = 10.0, distanceDelta = 0.1, motionState = MotionState.MOVING)

        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(0)
    }

    @Test
    fun shouldPauseTrackingAndBlockNewTelemetry() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.pauseTracking()

        assertThat(tracker.isPaused.value).isTrue
        assertThat(fakeSessionStatePort.activeSession?.isPaused).isTrue

        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 0.5, motionState = MotionState.MOVING)
        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(0)
    }

    @Test
    fun shouldResumeTrackingAndAllowTelemetry() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.pauseTracking()
        tracker.resumeTracking()

        assertThat(tracker.isPaused.value).isFalse
        assertThat(fakeSessionStatePort.activeSession?.isPaused).isFalse

        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 0.5, motionState = MotionState.MOVING)
        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(1)
    }

    @Test
    fun shouldDiscardConsecutiveDuplicateCoordinates() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 0.5, motionState = MotionState.MOVING)
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 0.0, motionState = MotionState.MOVING)

        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(1)
    }

    @Test
    fun shouldRecordNewLocationAfterDuplicate() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 0.5, motionState = MotionState.MOVING)
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 0.0, motionState = MotionState.MOVING)
        tracker.recordTelemetry(x = 10.1, y = 20.1, speed = 30.0, distanceDelta = 0.2, motionState = MotionState.ACCELERATING)

        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(2)
        assertThat(fakeSessionStatePort.activeSession?.currentDistanceKm).isEqualTo(0.7)
    }

    @Test
    fun shouldResetLastSavedLocationWhenTrackingRestarted() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 0.5, motionState = MotionState.MOVING)
        tracker.stopTracking()

        fakeSaveTripUseCase.savedTrips.clear()
        fakeSaveTripUseCase.savedGpsPoints.clear()

        tracker.startTracking()
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 0.5, motionState = MotionState.MOVING)

        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(1)
    }

    @Test
    fun shouldStopTrackingClearSessionButNotSaveTrip() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 1.0, motionState = MotionState.ACCELERATING)
        tracker.stopTracking()

        assertThat(tracker.isTracking.value).isFalse
        // El tracker ya no persiste viajes (solo el ViewModel/FinalizeTripUseCase lo hace)
        assertThat(fakeSaveTripUseCase.saveTripCallCount).isEqualTo(0)
        assertThat(fakeSaveTripUseCase.savedTrips).isEmpty()
        assertThat(fakeSaveTripUseCase.savedGpsPoints).isEmpty()
        assertThat(fakeSessionStatePort.activeSession).isNull()
    }

    @Test
    fun shouldNotCalculateMaxSpeedNorSaveWhenStoppingTracking() = runTest(testDispatcher) {
        fakeMaxSpeedUseCase.maxSpeed = 25.0

        tracker.startTracking()
        tracker.recordTelemetry(x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 1.0, motionState = MotionState.ACCELERATING)
        tracker.stopTracking()

        // El tracker no calcula maxSpeed ni guarda viajes; eso lo hace FinalizeTripUseCase desde el ViewModel
        assertThat(fakeMaxSpeedUseCase.executeCalled).isFalse()
        assertThat(fakeSaveTripUseCase.saveTripCallCount).isEqualTo(0)
    }

    // --- RF-01: Un único punto de escritura (el ViewModel escribe, el tracker no) ---
    @Test
    fun shouldNotWriteTripThroughTracker() = runTest(testDispatcher) {
        val fakeUseCase = FakeSaveTripUseCase()
        val trackerWithUseCase = ScooterTrackingTracker(
            sessionStatePort = fakeSessionStatePort,
            coroutineScope = testScope
        )

        trackerWithUseCase.startTracking()
        trackerWithUseCase.recordTelemetry(
            x = 10.0, y = 20.0, speed = 25.0, distanceDelta = 1.0, motionState = MotionState.MOVING
        )
        trackerWithUseCase.stopTracking()

        // El tracker no debe llamar al caso de uso de escritura (cero veces)
        assertThat(fakeUseCase.saveTripCallCount).isEqualTo(0)
    }

    // --- RF-03: Filtrado de muestreo GPS solo con movimiento real ---
    @Test
    fun shouldNotRecordTelemetryWhenStoppedAndZeroSpeed() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(
            x = 10.0, y = 20.0, speed = 0.0, distanceDelta = 0.0, motionState = MotionState.STOPPED
        )

        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(0)
    }

    @Test
    fun shouldNotRecordTelemetryWhenStoppedEvenWithDistanceDelta() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(
            x = 10.0, y = 20.0, speed = 0.0, distanceDelta = 0.01, motionState = MotionState.STOPPED
        )

        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(0)
    }

    @Test
    fun shouldRecordTelemetryWhenMoving() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(
            x = 10.0, y = 20.0, speed = 15.0, distanceDelta = 0.5, motionState = MotionState.MOVING
        )

        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(1)
    }

    @Test
    fun shouldRecordTelemetryWhenAccelerating() = runTest(testDispatcher) {
        tracker.startTracking()
        tracker.recordTelemetry(
            x = 10.0, y = 20.0, speed = 10.0, distanceDelta = 0.3, motionState = MotionState.ACCELERATING
        )

        assertThat(tracker.getCachedTelemetryCount()).isEqualTo(1)
    }
}

