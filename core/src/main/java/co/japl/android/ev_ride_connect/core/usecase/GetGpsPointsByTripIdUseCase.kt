package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import javax.inject.Inject

class GetGpsPointsByTripIdUseCase @Inject constructor(
    private val tripDatabasePort: TripDatabasePort
) {
    suspend fun execute(tripId: Long): List<TripGps> = tripDatabasePort.getGpsPointsByTripId(tripId)
}
