package co.japl.android.ev_ride_connect.core.ports

interface TripDatabasePort {
    suspend fun saveTripData(distance: Int, batteryConsumed: Int)
}
