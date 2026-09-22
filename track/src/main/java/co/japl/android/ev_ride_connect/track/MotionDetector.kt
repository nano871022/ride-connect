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
    private val linearAccelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val activeSensor = linearAccelerometer ?: accelerometer

    private val _motionState = MutableStateFlow(MotionState.STOPPED)
    override val motionState: StateFlow<MotionState> = _motionState.asStateFlow()

    private var isListening: Boolean = false

    private val alpha = 0.2f
    private var filteredAccX = 0f
    private var filteredAccY = 0f
    private var filteredAccZ = 0f
    private var isFilterInitialized = false

    override fun start() {
        if (isListening) return
        isListening = true
        resetFilter()

        activeSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun stop() {
        if (!isListening) return
        isListening = false
        sensorManager?.unregisterListener(this)
        resetFilter()
        _motionState.value = MotionState.STOPPED
    }

    private fun resetFilter() {
        filteredAccX = 0f
        filteredAccY = 0f
        filteredAccZ = 0f
        isFilterInitialized = false
    }

    private fun applyLowPassFilter(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
        if (!isFilterInitialized) {
            filteredAccX = x
            filteredAccY = y
            filteredAccZ = z
            isFilterInitialized = true
        } else {
            filteredAccX += alpha * (x - filteredAccX)
            filteredAccY += alpha * (y - filteredAccY)
            filteredAccZ += alpha * (z - filteredAccZ)
        }
        return Triple(filteredAccX, filteredAccY, filteredAccZ)
    }

    override fun processSensorData(
        accX: Float, accY: Float, accZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float,
        currentTimestamp: Long
    ) {
        val (smX, smY, smZ) = applyLowPassFilter(accX, accY, accZ)
        val rawMagnitude = sqrt((smX * smX + smY * smY + smZ * smZ).toDouble())

        val gravity = SensorManager.GRAVITY_EARTH.toDouble()
        val deltaAcc = if (rawMagnitude > 5.0) rawMagnitude - gravity else rawMagnitude

        val isMoving = abs(deltaAcc) > 0.8

        val newState = when {
            !isMoving -> MotionState.STOPPED
            deltaAcc > 1.2 -> MotionState.ACCELERATING
            deltaAcc < -1.2 -> MotionState.BRAKING
            else -> MotionState.MOVING
        }

        updateState(newState, currentTimestamp)
    }

    override fun updateState(newState: MotionState, currentTimestamp: Long) {
        _motionState.value = newState
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION || event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            processSensorData(event.values[0], event.values[1], event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
