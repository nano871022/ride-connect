package co.japl.android.ev_ride_connect.track

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import co.japl.android.ev_ride_connect.core.domain.MotionState
import co.japl.android.ev_ride_connect.core.ports.MotionDetectorPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

class MotionDetector(private val context: Context? = null) : MotionDetectorPort, SensorEventListener {

    private val sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _motionState = MutableStateFlow(MotionState.STOPPED)
    override val motionState: StateFlow<MotionState> = _motionState.asStateFlow()

    private var isListening: Boolean = false

    override fun start() {
        if (isListening) return
        isListening = true

        gyroscope?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun stop() {
        if (!isListening) return
        isListening = false
        sensorManager?.unregisterListener(this)
        _motionState.value = MotionState.STOPPED
    }

    override fun processSensorData(
        accX: Float, accY: Float, accZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float,
        currentTimestamp: Long
    ) {
        val magnitude = sqrt((accX * accX + accY * accY + accZ * accZ).toDouble())
        val gyroMagnitude = sqrt((gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ).toDouble())

        val gravity = SensorManager.GRAVITY_EARTH.toDouble()
        val deltaAcc = magnitude - gravity
        val isMoving = abs(deltaAcc) > 0.6 || gyroMagnitude > 0.4

        val newState = when {
            !isMoving -> MotionState.STOPPED
            deltaAcc > 0.5 -> MotionState.ACCELERATING
            deltaAcc < -0.5 -> MotionState.BRAKING
            else -> MotionState.MOVING
        }

        updateState(newState, currentTimestamp)
    }

    override fun updateState(newState: MotionState, currentTimestamp: Long) {
        _motionState.value = newState
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            processSensorData(event.values[0], event.values[1], event.values[2])
        } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            processSensorData(0f, 0f, SensorManager.GRAVITY_EARTH, event.values[0], event.values[1], event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
