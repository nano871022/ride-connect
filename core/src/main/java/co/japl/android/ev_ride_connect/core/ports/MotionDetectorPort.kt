package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.MotionState
import kotlinx.coroutines.flow.StateFlow

interface MotionDetectorPort {
    val motionState: StateFlow<MotionState>
    fun start()
    fun stop()
    fun processSensorData(
        accX: Float, accY: Float, accZ: Float,
        gyroX: Float = 0f, gyroY: Float = 0f, gyroZ: Float = 0f,
        currentTimestamp: Long = System.currentTimeMillis()
    )
    fun updateState(newState: MotionState, currentTimestamp: Long = System.currentTimeMillis())
}
