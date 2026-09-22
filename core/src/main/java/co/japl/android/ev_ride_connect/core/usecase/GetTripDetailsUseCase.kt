package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import javax.inject.Inject

class GetTripDetailsUseCase @Inject constructor(
    private val tripDatabasePort: TripDatabasePort
) {
    suspend fun execute(tripId: Long): Pair<Trip, List<TripGps>>? {
        val trip = tripDatabasePort.getTripById(tripId) ?: return null
        val gpsPoints = tripDatabasePort.getGpsPointsByTripId(tripId)
        return Pair(trip, gpsPoints)
    }
}
