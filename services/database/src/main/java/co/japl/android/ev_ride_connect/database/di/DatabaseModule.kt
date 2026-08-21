package co.japl.android.ev_ride_connect.database.di

import android.content.Context
import androidx.room.Room
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.database.AppDatabase
import co.japl.android.ev_ride_connect.database.GoogleDriveBackupHelper
import co.japl.android.ev_ride_connect.database.LlmClientAdapter
import co.japl.android.ev_ride_connect.database.RoomEvConfigAdapter
import co.japl.android.ev_ride_connect.database.RoomLlmConfigAdapter
import co.japl.android.ev_ride_connect.database.RoomTripAdapter
import co.japl.android.ev_ride_connect.database.dao.EvConfigDao
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database.db"
        ).fallbackToDestructiveMigration(true).build()
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
    fun provideLlmClientPort(): LlmClientPort {
        return LlmClientAdapter()
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
    fun provideGoogleDriveBackupPort(@ApplicationContext context: Context): GoogleDriveBackupPort {
        return GoogleDriveBackupHelper(context)
    }
}
