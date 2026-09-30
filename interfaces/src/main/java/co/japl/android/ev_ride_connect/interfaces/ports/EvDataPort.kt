package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.EvData

interface EvDataPort {
    suspend fun getLatestEvData(): EvData?
    suspend fun getAllEvData(): List<EvData>
    suspend fun saveEvData(evData: EvData): Long
}
