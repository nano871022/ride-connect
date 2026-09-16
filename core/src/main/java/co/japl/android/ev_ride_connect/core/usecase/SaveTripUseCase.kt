package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import javax.inject.Inject

class SaveTripUseCase @Inject constructor(
    private val tripDatabasePort: TripDatabasePort
) {
    suspend fun execute(trip: Trip, gpsPoints: List<TripGps>): Long =
        tripDatabasePort.saveTrip(trip, gpsPoints)
}
