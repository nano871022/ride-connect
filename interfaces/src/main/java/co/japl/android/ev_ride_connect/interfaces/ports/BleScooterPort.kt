package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.ScooterState
import kotlinx.coroutines.flow.StateFlow

interface BleScooterPort {
    val scooterState: StateFlow<ScooterState>
    suspend fun connect(deviceAddress: String): Boolean
    suspend fun disconnect()
    suspend fun toggleLock(lock: Boolean): Boolean
    suspend fun setSpeedMode(mode: Int): Boolean
    suspend fun toggleLight(lightOn: Boolean): Boolean
}
