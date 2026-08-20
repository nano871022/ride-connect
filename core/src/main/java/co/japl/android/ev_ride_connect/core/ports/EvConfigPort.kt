package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.EvConfig

interface EvConfigPort {
    suspend fun getEvConfig(): EvConfig?
    suspend fun saveEvConfig(config: EvConfig): Long
}
