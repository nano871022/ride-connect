package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.domain.MotionState
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class MotionDetectorTest {

    private lateinit var motionDetector: MotionDetector

    @Before
    fun setUp() {
        motionDetector = MotionDetector()
    }

    @Test
    fun shouldDetectStoppedWhenNotMoving() {
        motionDetector.processSensorData(0f, 0f, 9.81f, 0f, 0f, 0f)

        assertThat(motionDetector.motionState.value).isEqualTo(MotionState.STOPPED)
    }

    @Test
    fun shouldDetectAcceleratingWhenDeltaAccPositive() {
        motionDetector.processSensorData(0f, 0f, 10.8f, 0.5f, 0f, 0f)

        assertThat(motionDetector.motionState.value).isEqualTo(MotionState.ACCELERATING)
    }

    @Test
    fun shouldDetectBrakingWhenDeltaAccNegative() {
        motionDetector.processSensorData(0f, 0f, 8.8f, 0.5f, 0f, 0f)

        assertThat(motionDetector.motionState.value).isEqualTo(MotionState.BRAKING)
    }

    @Test
    fun shouldDetectMovingWhenGyroHighButDeltaAccModerate() {
        motionDetector.processSensorData(0f, 0f, 10.0f, 0.5f, 0f, 0f)

        assertThat(motionDetector.motionState.value).isEqualTo(MotionState.MOVING)
    }

    @Test
    fun shouldUpdateStateDirectly() {
        motionDetector.updateState(MotionState.MOVING)

        assertThat(motionDetector.motionState.value).isEqualTo(MotionState.MOVING)
    }
}
