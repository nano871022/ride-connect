package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.usecase.api.CalculateMaxSpeedUseCase
import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.MotionState
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScooterTrackingServiceTest {

    private lateinit var fakeSessionPort: FakeSessionStatePort
    private lateinit var trackingTracker: ScooterTrackingTracker

    @Before
    fun setUp() {
        fakeSessionPort = FakeSessionStatePort()
    }

    @Test
    fun shouldNotBeTrackingInitially() {
        trackingTracker = ScooterTrackingTracker(
            sessionStatePort = fakeSessionPort
        )
        assertThat(trackingTracker.isTracking.value).isFalse()
    }

    @Test
    fun shouldStartTrackingWhenStartCalled() = runTest {
        trackingTracker = ScooterTrackingTracker(
            sessionStatePort = fakeSessionPort,
            coroutineScope = this
        )
        trackingTracker.startTracking()

        assertThat(trackingTracker.isTracking.value).isTrue()

        trackingTracker.stopTracking()
    }

    @Test
    fun shouldStopTrackingClearSessionButNotSaveTrip() = runTest {
        trackingTracker = ScooterTrackingTracker(
            sessionStatePort = fakeSessionPort,
            coroutineScope = this
        )

        trackingTracker.startTracking()
        trackingTracker.recordTelemetry(x = 10.0, y = 20.0, speed = 20.0, distanceDelta = 1.0)
        testScheduler.runCurrent()

        trackingTracker.stopTracking()

        assertThat(trackingTracker.isTracking.value).isFalse()
        // El tracker ya no persiste viajes (solo el ViewModel/FinalizeTripUseCase lo hace)
        assertThat(fakeSessionPort.activeSession).isNull()
    }

    @Test
    fun shouldRecordMultipleContinuousTelemetryPoints() = runTest {
        trackingTracker = ScooterTrackingTracker(
            sessionStatePort = fakeSessionPort,
            coroutineScope = this
        )

        trackingTracker.startTracking()
        trackingTracker.recordTelemetry(x = 10.0001, y = 20.0001, speed = 15.0, distanceDelta = 0.05, motionState = MotionState.MOVING)
        trackingTracker.recordTelemetry(x = 10.0002, y = 20.0002, speed = 18.0, distanceDelta = 0.05, motionState = MotionState.ACCELERATING)
        trackingTracker.recordTelemetry(x = 10.0003, y = 20.0003, speed = 12.0, distanceDelta = 0.05, motionState = MotionState.BRAKING)
        testScheduler.runCurrent()

        assertThat(trackingTracker.getCachedTelemetryCount()).isEqualTo(3)

        trackingTracker.stopTracking()
        // El tracker ya no guarda puntos GPS en BD
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
