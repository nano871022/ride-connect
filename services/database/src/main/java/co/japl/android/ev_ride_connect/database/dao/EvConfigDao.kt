package co.japl.android.ev_ride_connect.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.japl.android.ev_ride_connect.database.entities.EvConfigEntity

@Dao
interface EvConfigDao {

    @Query("SELECT * FROM ev_configs ORDER BY id DESC LIMIT 1")
    suspend fun getLatestEvConfig(): EvConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvConfig(config: EvConfigEntity): Long
}
