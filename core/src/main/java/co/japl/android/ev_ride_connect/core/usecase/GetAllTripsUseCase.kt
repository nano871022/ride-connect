package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import javax.inject.Inject

class GetAllTripsUseCase @Inject constructor(
    private val tripDatabasePort: TripDatabasePort
) {
    suspend fun execute(): List<Trip> = tripDatabasePort.getAllTrips()
}
