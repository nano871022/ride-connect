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
}
