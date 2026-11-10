package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.EvConfig

interface EvConfigPort {
    suspend fun getEvConfig(): EvConfig?
    suspend fun saveEvConfig(config: EvConfig): Long
}
