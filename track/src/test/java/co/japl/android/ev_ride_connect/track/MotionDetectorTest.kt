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
    fun shouldDetectAcceleratingWhenDeltaAccExceedsThreshold() {
        motionDetector.processSensorData(0f, 0f, 11.81f, 0f, 0f, 0f)

        assertThat(motionDetector.motionState.value).isEqualTo(MotionState.ACCELERATING)
    }

    @Test
    fun shouldDetectBrakingWhenDeltaAccBelowThreshold() {
        motionDetector.processSensorData(0f, 0f, 7.81f, 0f, 0f, 0f)

        assertThat(motionDetector.motionState.value).isEqualTo(MotionState.BRAKING)
    }

    @Test
    fun shouldDetectMovingWhenDeltaAccModerate() {
        motionDetector.processSensorData(0f, 0f, 10.81f, 0f, 0f, 0f)

        assertThat(motionDetector.motionState.value).isEqualTo(MotionState.MOVING)
    }

    @Test
    fun shouldIgnoreGyroDataForStateTransitions() {
        // Even with high gyro rates, motion state is derived purely from accelerometer
        motionDetector.processSensorData(0f, 0f, 9.81f, 5.0f, 5.0f, 5.0f)

        assertThat(motionDetector.motionState.value).isEqualTo(MotionState.STOPPED)
    }

    @Test
    fun shouldUpdateStateDirectly() {
        motionDetector.updateState(MotionState.MOVING)

        assertThat(motionDetector.motionState.value).isEqualTo(MotionState.MOVING)
    }
}
