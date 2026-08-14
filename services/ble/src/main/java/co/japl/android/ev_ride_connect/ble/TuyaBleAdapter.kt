package co.japl.android.ev_ride_connect.ble

import co.japl.android.ev_ride_connect.core.domain.ScooterState
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.utils.BatteryCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TuyaBleAdapter : BleScooterPort {

    private val _scooterState = MutableStateFlow(
        ScooterState(
            isLocked = false,
            speedMode = 0,
            currentSpeed = 0,
            realtimeVoltage = 0,
            batteryPercentage = 0,
            totalOdometer = 0,
            isLightOn = false
        )
    )

    private var onSendCommandListener: ((Int, Any) -> Unit)? = null

    override fun observeScooterState(): Flow<ScooterState> {
        return _scooterState.asStateFlow()
    }

    override fun sendCommand(dpId: Int, value: Any) {
        onSendCommandListener?.invoke(dpId, value)
    }

    fun setOnSendCommandListener(listener: (Int, Any) -> Unit) {
        this.onSendCommandListener = listener
    }

    fun onDataPointReceived(dpId: Int, value: Any) {
        _scooterState.update { currentState ->
            updateStateWithDp(currentState, dpId, value)
        }
    }

    fun onDataPointsReceived(dps: Map<Int, Any>) {
        _scooterState.update { currentState ->
            var updatedState = currentState
            dps.forEach { (dpId, value) ->
                updatedState = updateStateWithDp(updatedState, dpId, value)
            }
            updatedState
        }
    }

    private fun updateStateWithDp(currentState: ScooterState, dpId: Int, value: Any): ScooterState {
        return when (dpId) {
            1 -> currentState.copy(isLocked = value as? Boolean ?: currentState.isLocked)
            2 -> currentState.copy(speedMode = (value as? Number)?.toInt() ?: currentState.speedMode)
            4 -> currentState.copy(isLightOn = value as? Boolean ?: currentState.isLightOn)
            5 -> currentState.copy(currentSpeed = (value as? Number)?.toInt() ?: currentState.currentSpeed)
            6 -> currentState.copy(totalOdometer = (value as? Number)?.toInt() ?: currentState.totalOdometer)
            7 -> {
                val voltage = (value as? Number)?.toInt() ?: currentState.realtimeVoltage
                val batteryPercentage = BatteryCalculator.calculate13SPercentage(voltage)
                currentState.copy(
                    realtimeVoltage = voltage,
                    batteryPercentage = batteryPercentage
                )
            }
            else -> currentState
        }
    }
}
