package co.japl.android.ev_ride_connect.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.japl.android.ev_ride_connect.database.entities.TripEntity
import co.japl.android.ev_ride_connect.database.entities.TripGpsEntity

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripGpsList(gpsPoints: List<TripGpsEntity>)

    @Query("SELECT * FROM trips ORDER BY create_tmst DESC, timestamp DESC")
    suspend fun getAllTrips(): List<TripEntity>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): TripEntity?

    @Query("SELECT * FROM trip_gps WHERE trip_id = :tripId ORDER BY order_index ASC")
    suspend fun getGpsPointsByTripId(tripId: Long): List<TripGpsEntity>

    @Query("SELECT * FROM trips WHERE (create_tmst >= :startTimestamp AND create_tmst <= :endTimestamp) OR (create_tmst = 0 AND timestamp >= :startTimestamp AND timestamp <= :endTimestamp) ORDER BY create_tmst DESC, timestamp DESC")
    suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<TripEntity>

    @Query("SELECT COUNT(*) FROM trips")
    suspend fun getTotalTripsCount(): Int

    @Query("SELECT SUM(CASE WHEN distance_km > 0 THEN distance_km ELSE distance / 1000.0 END) FROM trips")
    suspend fun getTotalDistanceKm(): Double?

    @Query("SELECT COUNT(*) FROM trips WHERE battery_consumed >= :threshold")
    suspend fun getChargeDetectionsCount(threshold: Int): Int
}
