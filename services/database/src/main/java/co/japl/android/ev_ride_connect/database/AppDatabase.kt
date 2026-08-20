package co.japl.android.ev_ride_connect.database

import androidx.room.Database
import androidx.room.RoomDatabase
import co.japl.android.ev_ride_connect.database.dao.EvConfigDao
import co.japl.android.ev_ride_connect.database.dao.LlmConfigDao
import co.japl.android.ev_ride_connect.database.dao.TripDao
import co.japl.android.ev_ride_connect.database.entities.EvConfigEntity
import co.japl.android.ev_ride_connect.database.entities.LlmConfigEntity
import co.japl.android.ev_ride_connect.database.entities.TripEntity

@Database(entities = [TripEntity::class, LlmConfigEntity::class, EvConfigEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun llmConfigDao(): LlmConfigDao
    abstract fun evConfigDao(): EvConfigDao
}
