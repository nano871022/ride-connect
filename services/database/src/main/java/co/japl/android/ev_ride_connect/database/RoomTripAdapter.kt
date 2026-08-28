package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.database.dao.TripDao
import co.japl.android.ev_ride_connect.database.entities.TripEntity
import co.japl.android.ev_ride_connect.database.entities.TripGpsEntity

class RoomTripAdapter(
    private val tripDao: TripDao
) : TripDatabasePort {

    override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {
        val tripEntity = TripEntity(
            distance = distance,
            batteryConsumed = batteryConsumed,
            distanceKm = distance / 1000.0
        )
        tripDao.insertTrip(tripEntity)
    }

    override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
        val tripEntity = TripEntity(
            id = trip.id,
            duration = trip.timeTrip,
            timeTrip = trip.timeTrip,
            averageSpeed = trip.averageSpeed,
            distanceKm = trip.distance,
            distance = (trip.distance * 1000).toInt(),
            createTmst = trip.createTmst,
            timestamp = trip.createTmst
        )
        val insertedId = tripDao.insertTrip(tripEntity)

        if (gpsPoints.isNotEmpty()) {
            val gpsEntities = gpsPoints.map { gps ->
                TripGpsEntity(
                    id = gps.id,
                    tripId = insertedId,
                    orderIndex = gps.orderIndex,
                    speed = gps.speed,
                    distance = gps.distance,
                    x = gps.x,
                    y = gps.y,
                    createTmst = gps.createTmst
                )
            }
            tripDao.insertTripGpsList(gpsEntities)
        }

        return insertedId
    }

    override suspend fun getAllTrips(): List<Trip> {
        return tripDao.getAllTrips().map { entity ->
            Trip(
                id = entity.id,
                timeTrip = if (entity.timeTrip != 0L) entity.timeTrip else entity.duration,
                averageSpeed = entity.averageSpeed,
                distance = if (entity.distanceKm != 0.0) entity.distanceKm else entity.distance / 1000.0,
                createTmst = if (entity.createTmst != 0L) entity.createTmst else entity.timestamp
            )
        }
    }

    override suspend fun getTripById(tripId: Long): Trip? {
        val entity = tripDao.getTripById(tripId) ?: return null
        return Trip(
            id = entity.id,
            timeTrip = if (entity.timeTrip != 0L) entity.timeTrip else entity.duration,
            averageSpeed = entity.averageSpeed,
            distance = if (entity.distanceKm != 0.0) entity.distanceKm else entity.distance / 1000.0,
            createTmst = if (entity.createTmst != 0L) entity.createTmst else entity.timestamp
        )
    }

    override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> {
        return tripDao.getGpsPointsByTripId(tripId).map { entity ->
            TripGps(
                id = entity.id,
                tripId = entity.tripId,
                orderIndex = entity.orderIndex,
                speed = entity.speed,
                distance = entity.distance,
                x = entity.x,
                y = entity.y,
                createTmst = entity.createTmst
            )
        }
    }
}
