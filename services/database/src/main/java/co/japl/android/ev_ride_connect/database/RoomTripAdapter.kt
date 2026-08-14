package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.database.dao.TripDao
import co.japl.android.ev_ride_connect.database.entities.TripEntity

class RoomTripAdapter(
    private val tripDao: TripDao
) : TripDatabasePort {

    override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {
        val tripEntity = TripEntity(
            distance = distance,
            batteryConsumed = batteryConsumed
        )
        tripDao.insertTrip(tripEntity)
    }
}
