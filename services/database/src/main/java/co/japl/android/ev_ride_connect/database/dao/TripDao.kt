package co.japl.android.ev_ride_connect.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.japl.android.ev_ride_connect.database.entities.TripEntity

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Query("SELECT * FROM trips ORDER BY timestamp DESC")
    suspend fun getAllTrips(): List<TripEntity>
}
