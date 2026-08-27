package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.ScooterState
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bleScooterPort: BleScooterPort,
    private val tripDatabasePort: TripDatabasePort
) : ViewModel() {

    val scooterState: StateFlow<ScooterState> = bleScooterPort.observeScooterState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ScooterState(
                isLocked = false,
                speedMode = 0,
                currentSpeed = 0,
                realtimeVoltage = 0,
                batteryPercentage = 0,
                totalOdometer = 0,
                isLightOn = false
            )
        )

    fun toggleLock() {
        val currentState = scooterState.value
        bleScooterPort.sendCommand(1, !currentState.isLocked)
    }

    fun toggleLight() {
        val currentState = scooterState.value
        bleScooterPort.sendCommand(4, !currentState.isLightOn)
    }

    fun setSpeedMode(speedMode: Int) {
        bleScooterPort.sendCommand(2, speedMode)
    }
}
