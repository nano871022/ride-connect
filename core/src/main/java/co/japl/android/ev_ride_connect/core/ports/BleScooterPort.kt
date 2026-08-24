package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.BleLogEntry
import co.japl.android.ev_ride_connect.core.domain.ScooterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface BleScooterPort {
    fun observeScooterState(): Flow<ScooterState>
    fun sendCommand(dpId: Int, value: Any)

    fun connect(macAddress: String? = null) {}
    fun disconnect() {}
    fun observeConnectionState(): Flow<Boolean> = flowOf(false)

    fun observeRawLogs(): Flow<List<BleLogEntry>> = flowOf(emptyList())
    fun clearLogs() {}
}
