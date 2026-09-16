package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import javax.inject.Inject

class GetTripByIdUseCase @Inject constructor(
    private val tripDatabasePort: TripDatabasePort
) {
    suspend fun execute(tripId: Long): Trip? = tripDatabasePort.getTripById(tripId)
}
