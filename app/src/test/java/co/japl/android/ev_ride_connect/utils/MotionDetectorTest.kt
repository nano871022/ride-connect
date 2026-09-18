package co.japl.android.ev_ride_connect.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import android.hardware.SensorManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MotionDetectorTest {

    private lateinit var context: Context
    private lateinit var motionDetector: MotionDetector

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        motionDetector = MotionDetector(context)
    }

    @Test
    fun shouldDetectStationaryStateWhenNoMovement() {
        val gravity = SensorManager.GRAVITY_EARTH
        motionDetector.processSensorData(
            accX = 0f,
            accY = 0f,
            accZ = gravity,
            gyroX = 0f,
            gyroY = 0f,
            gyroZ = 0f,
            currentTimestamp = 10000L
        )

        val data = motionDetector.motionData.value
        assertThat(data.state).isEqualTo(MotionState.STATIONARY)
    }

    @Test
    fun shouldDetectAcceleratingStateWhenPositiveDeltaAcceleration() {
        val gravity = SensorManager.GRAVITY_EARTH
        motionDetector.processSensorData(
            accX = 0f,
            accY = 0f,
            accZ = gravity + 2.0f,
            gyroX = 0.5f,
            gyroY = 0f,
            gyroZ = 0f,
            currentTimestamp = 10000L
        )

        val data = motionDetector.motionData.value
        assertThat(data.state).isEqualTo(MotionState.ACCELERATING)
    }

    @Test
    fun shouldDetectBrakingStateWhenNegativeDeltaAcceleration() {
        val gravity = SensorManager.GRAVITY_EARTH
        motionDetector.processSensorData(
            accX = 0f,
            accY = 0f,
            accZ = gravity - 2.0f,
            gyroX = 0.5f,
            gyroY = 0f,
            gyroZ = 0f,
            currentTimestamp = 10000L
        )

        val data = motionDetector.motionData.value
        assertThat(data.state).isEqualTo(MotionState.BRAKING)
    }

    @Test
    fun shouldTrackStationaryDurationAndLastStationaryOnTransition() {
        val t0 = 10000L
        motionDetector.updateState(MotionState.STATIONARY, t0)

        val t1 = 25000L // 15 seconds later
        motionDetector.updateState(MotionState.STATIONARY, t1)

        assertThat(motionDetector.motionData.value.currentStationarySeconds).isEqualTo(15L)

        val t2 = 28000L
        motionDetector.updateState(MotionState.ACCELERATING, t2)

        assertThat(motionDetector.motionData.value.state).isEqualTo(MotionState.ACCELERATING)
        assertThat(motionDetector.motionData.value.currentStationarySeconds).isEqualTo(0L)
        assertThat(motionDetector.motionData.value.lastStationarySeconds).isEqualTo(18L)
    }
}
