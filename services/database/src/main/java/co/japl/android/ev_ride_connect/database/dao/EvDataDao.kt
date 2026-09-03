package co.japl.android.ev_ride_connect.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.japl.android.ev_ride_connect.database.entities.EvDataEntity

@Dao
interface EvDataDao {

    @Query("SELECT * FROM ev_data ORDER BY crte_tmst DESC LIMIT 1")
    suspend fun getLatestEvData(): EvDataEntity?

    @Query("SELECT * FROM ev_data ORDER BY crte_tmst DESC")
    suspend fun getAllEvData(): List<EvDataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvData(evData: EvDataEntity): Long
}
