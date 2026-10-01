package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.BleLogEntry
import co.japl.android.ev_ride_connect.interfaces.model.ScooterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BleScooterPort {
    val scooterState: StateFlow<ScooterState>
    fun observeScooterState(): Flow<ScooterState>
    fun observeConnectionState(): Flow<Boolean>
    fun observeRawLogs(): Flow<List<BleLogEntry>>
    fun clearLogs()
    fun sendCommand(dpId: Int, value: Any)
    fun connect(macAddress: String?)
    suspend fun connect(deviceAddress: String): Boolean
    suspend fun disconnect()
    suspend fun toggleLock(lock: Boolean): Boolean
    suspend fun setSpeedMode(mode: Int): Boolean
    suspend fun toggleLight(lightOn: Boolean): Boolean
}
