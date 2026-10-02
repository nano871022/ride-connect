package co.japl.android.ev_ride_connect.interfaces.usecase

import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps

interface TripDetailUseCase {
    suspend fun getTripDetails(tripId: Long): Pair<Trip, List<TripGps>>?
    suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps>
    suspend fun getTripById(tripId: Long): Trip?
}
