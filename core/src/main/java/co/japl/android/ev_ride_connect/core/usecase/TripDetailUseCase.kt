package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import javax.inject.Inject

class TripDetailUseCase @Inject constructor(
    private val tripDatabasePort: TripDatabasePort,
    private val getTripDetailsUseCase: GetTripDetailsUseCase,
    private val getGpsPointsByTripIdUseCase: GetGpsPointsByTripIdUseCase,
    private val getTripByIdUseCase: GetTripByIdUseCase
) {
    suspend fun getTripDetails(tripId: Long): Pair<Trip, List<TripGps>>? {
        return getTripDetailsUseCase.execute(tripId)
    }

    suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> {
        return getGpsPointsByTripIdUseCase.execute(tripId)
    }

    suspend fun getTripById(tripId: Long): Trip? {
        return getTripByIdUseCase.execute(tripId)
    }
}
