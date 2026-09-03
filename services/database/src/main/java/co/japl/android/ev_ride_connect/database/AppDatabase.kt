package co.japl.android.ev_ride_connect.database

import androidx.room.Database
import androidx.room.RoomDatabase
import co.japl.android.ev_ride_connect.database.dao.EvConfigDao
import co.japl.android.ev_ride_connect.database.dao.EvDataDao
import co.japl.android.ev_ride_connect.database.dao.LlmConfigDao
import co.japl.android.ev_ride_connect.database.dao.TripDao
import co.japl.android.ev_ride_connect.database.entities.EvConfigEntity
import co.japl.android.ev_ride_connect.database.entities.EvDataEntity
import co.japl.android.ev_ride_connect.database.entities.LlmConfigEntity
import co.japl.android.ev_ride_connect.database.entities.TripEntity
import co.japl.android.ev_ride_connect.database.entities.TripGpsEntity

@Database(
    entities = [
        TripEntity::class,
        TripGpsEntity::class,
        LlmConfigEntity::class,
        EvConfigEntity::class,
        EvDataEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun llmConfigDao(): LlmConfigDao
    abstract fun evConfigDao(): EvConfigDao
    abstract fun evDataDao(): EvDataDao
}
