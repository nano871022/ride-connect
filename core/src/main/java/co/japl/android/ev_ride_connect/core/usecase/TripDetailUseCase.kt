package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import javax.inject.Inject

interface TripDetailUseCase {
    suspend fun getTripDetails(tripId: Long): Pair<Trip, List<TripGps>>?
    suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps>
    suspend fun getTripById(tripId: Long): Trip?
}

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
