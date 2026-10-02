package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps

interface TripDatabasePort {
    suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long
    suspend fun getAllTrips(): List<Trip>
    suspend fun getTripById(tripId: Long): Trip?
    suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps>
    suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip>
    suspend fun getTotalTripsCount(): Int
    suspend fun getTotalDistanceKm(): Double
    suspend fun getChargeDetectionsCount(threshold: Int): Int
    suspend fun saveTripData(distance: Int, batteryConsumed: Int)
}
