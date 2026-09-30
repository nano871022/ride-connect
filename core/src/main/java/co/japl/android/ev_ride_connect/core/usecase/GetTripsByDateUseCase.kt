package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.ports.TripDatabasePort
import javax.inject.Inject

class GetTripsByDateUseCase @Inject constructor(
    private val tripDatabasePort: TripDatabasePort
) {
    suspend fun execute(startTimestamp: Long, endTimestamp: Long): List<Trip> =
        tripDatabasePort.getTripsByDate(startTimestamp, endTimestamp)
}
