package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.EvData

interface EvDataPort {
    suspend fun getLatestEvData(): EvData?
    suspend fun getAllEvData(): List<EvData>
    suspend fun saveEvData(evData: EvData): Long
}
