package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps

interface TripDatabasePort {
    suspend fun saveTripData(distance: Int, batteryConsumed: Int)
    suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long
    suspend fun getAllTrips(): List<Trip>
    suspend fun getTripById(tripId: Long): Trip?
    suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps>
}
