package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.ScooterState
import kotlinx.coroutines.flow.Flow

interface BleScooterPort {
    fun observeScooterState(): Flow<ScooterState>
    fun sendCommand(dpId: Int, value: Any)
}
