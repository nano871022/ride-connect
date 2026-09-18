package co.japl.android.ev_ride_connect.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

enum class MotionState {
    STATIONARY,
    ACCELERATING,
    BRAKING
}

data class MotionData(
    val state: MotionState = MotionState.STATIONARY,
    val currentStationarySeconds: Long = 0L,
    val lastStationarySeconds: Long = 0L
)

class MotionDetector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _motionData = MutableStateFlow(MotionData())
    val motionData: StateFlow<MotionData> = _motionData.asStateFlow()

    private var stationaryStartTmst: Long = 0L
    private var isListening: Boolean = false

    fun start() {
        if (isListening) return
        isListening = true
        stationaryStartTmst = System.currentTimeMillis()

        gyroscope?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        if (!isListening) return
        isListening = false
        sensorManager?.unregisterListener(this)
        _motionData.value = MotionData()
    }

    fun processSensorData(
        accX: Float, accY: Float, accZ: Float,
        gyroX: Float = 0f, gyroY: Float = 0f, gyroZ: Float = 0f,
        currentTimestamp: Long = System.currentTimeMillis()
    ) {
        val magnitude = sqrt((accX * accX + accY * accY + accZ * accZ).toDouble())
        val gyroMagnitude = sqrt((gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ).toDouble())

        val gravity = SensorManager.GRAVITY_EARTH.toDouble()
        val deltaAcc = magnitude - gravity
        val isMoving = abs(deltaAcc) > 0.6 || gyroMagnitude > 0.4

        val newState = when {
            !isMoving -> MotionState.STATIONARY
            deltaAcc > 0.5 -> MotionState.ACCELERATING
            deltaAcc < -0.5 -> MotionState.BRAKING
            else -> MotionState.STATIONARY
        }

        updateState(newState, currentTimestamp)
    }

    fun updateState(newState: MotionState, currentTimestamp: Long = System.currentTimeMillis()) {
        val currentData = _motionData.value
        val previousState = currentData.state

        var currentStationary = 0L
        var lastStationary = currentData.lastStationarySeconds

        if (newState == MotionState.STATIONARY) {
            if (previousState != MotionState.STATIONARY || stationaryStartTmst == 0L) {
                stationaryStartTmst = currentTimestamp
            }
            currentStationary = (currentTimestamp - stationaryStartTmst) / 1000L
        } else {
            if (previousState == MotionState.STATIONARY && stationaryStartTmst > 0L) {
                lastStationary = (currentTimestamp - stationaryStartTmst) / 1000L
                stationaryStartTmst = 0L
            }
            currentStationary = 0L
        }

        _motionData.value = MotionData(
            state = newState,
            currentStationarySeconds = currentStationary,
            lastStationarySeconds = lastStationary
        )
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
