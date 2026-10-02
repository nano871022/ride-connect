package co.japl.android.ev_ride_connect.interfaces.usecase

import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.Trip

interface EvDataUseCase {
    suspend fun getAllEvData(): List<EvData>
    suspend fun getAllTrips(): List<Trip>
    suspend fun getTripsByDate(startDateMs: Long, endDateMs: Long): List<Trip>
}
