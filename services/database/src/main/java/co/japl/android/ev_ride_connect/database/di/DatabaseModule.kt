package co.japl.android.ev_ride_connect.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.database.AppDatabase
import co.japl.android.ev_ride_connect.database.GoogleDriveBackupHelper
import co.japl.android.ev_ride_connect.database.RoomActiveSessionAdapter
import co.japl.android.ev_ride_connect.database.RoomEvConfigAdapter
import co.japl.android.ev_ride_connect.database.RoomEvDataAdapter
import co.japl.android.ev_ride_connect.database.RoomLlmConfigAdapter
import co.japl.android.ev_ride_connect.database.RoomTripAdapter
import co.japl.android.ev_ride_connect.database.dao.ActiveSessionDao
import co.japl.android.ev_ride_connect.database.dao.EvConfigDao
import co.japl.android.ev_ride_connect.database.dao.EvDataDao
import co.japl.android.ev_ride_connect.database.dao.LlmConfigDao
import co.japl.android.ev_ride_connect.database.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE ev_configs ADD COLUMN image_url TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE trip_gps ADD COLUMN motion_state TEXT NOT NULL DEFAULT 'STOPPED'")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database.db"
        )
        .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
        .fallbackToDestructiveMigration(true)
        .build()
    }

    @Provides
    @Singleton
    fun provideTripDao(database: AppDatabase): TripDao {
        return database.tripDao()
    }

    @Provides
    @Singleton
    fun provideTripDatabasePort(tripDao: TripDao): TripDatabasePort {
        return RoomTripAdapter(tripDao)
    }

    @Provides
    @Singleton
    fun provideLlmConfigDao(database: AppDatabase): LlmConfigDao {
        return database.llmConfigDao()
    }

    @Provides
    @Singleton
    fun provideLlmConfigPort(llmConfigDao: LlmConfigDao): LlmConfigPort {
        return RoomLlmConfigAdapter(llmConfigDao)
    }

    @Provides
    @Singleton
    fun provideEvConfigDao(database: AppDatabase): EvConfigDao {
        return database.evConfigDao()
    }

    @Provides
    @Singleton
    fun provideEvConfigPort(evConfigDao: EvConfigDao): EvConfigPort {
        return RoomEvConfigAdapter(evConfigDao)
    }

    @Provides
    @Singleton
    fun provideEvDataDao(database: AppDatabase): EvDataDao {
        return database.evDataDao()
    }

    @Provides
    @Singleton
    fun provideEvDataPort(evDataDao: EvDataDao): EvDataPort {
        return RoomEvDataAdapter(evDataDao)
    }

    @Provides
    @Singleton
    fun provideActiveSessionDao(database: AppDatabase): ActiveSessionDao {
        return database.activeSessionDao()
    }

    @Provides
    @Singleton
    fun provideSessionStatePort(activeSessionDao: ActiveSessionDao): SessionStatePort {
        return RoomActiveSessionAdapter(activeSessionDao)
    }

    @Provides
    @Singleton
    fun provideGoogleDriveBackupPort(@ApplicationContext context: Context): GoogleDriveBackupPort {
        return GoogleDriveBackupHelper(context)
    }
}
