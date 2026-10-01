package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.EvData

interface EvDataPort {
    suspend fun getLatestEvData(): EvData?
    suspend fun saveEvData(evData: EvData): Long
    suspend fun getAllEvData(): List<EvData>
    suspend fun updateOdometer(evCode: String, newKm: Long, currentBatteryPercentage: Short): Long
    suspend fun updateOdometer(newKm: Long): Long = updateOdometer("EV01", newKm, 100)
}
