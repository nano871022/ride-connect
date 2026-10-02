package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.MotionState
import kotlinx.coroutines.flow.StateFlow

interface MotionDetectorPort {
    val motionState: StateFlow<MotionState>
    fun start()
    fun stop()
    fun processSensorData(
        accX: Float, accY: Float, accZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float,
        currentTimestamp: Long
    )
    fun updateState(newState: MotionState, currentTimestamp: Long)
}
