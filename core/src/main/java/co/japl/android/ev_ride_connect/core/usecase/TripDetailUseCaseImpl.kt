package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.interfaces.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.interfaces.usecase.TripDetailUseCase
import javax.inject.Inject

class TripDetailUseCaseImpl @Inject constructor(
    private val tripDatabasePort: TripDatabasePort,
    private val getTripDetailsUseCase: GetTripDetailsUseCase,
    private val getGpsPointsByTripIdUseCase: GetGpsPointsByTripIdUseCase,
    private val getTripByIdUseCase: GetTripByIdUseCase
) : TripDetailUseCase {
    override suspend fun getTripDetails(tripId: Long): Pair<Trip, List<TripGps>>? {
        return getTripDetailsUseCase.execute(tripId)
    }

    override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> {
        return getGpsPointsByTripIdUseCase.execute(tripId)
    }

    override suspend fun getTripById(tripId: Long): Trip? {
        return getTripByIdUseCase.execute(tripId)
    }
}
