package co.japl.android.ev_ride_connect.database.di

import android.content.Context
import androidx.room.Room
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.database.AppDatabase
import co.japl.android.ev_ride_connect.database.GoogleDriveBackupHelper
import co.japl.android.ev_ride_connect.database.RoomTripAdapter
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
        ).fallbackToDestructiveMigration().build()
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
    fun provideGoogleDriveBackupPort(): GoogleDriveBackupPort {
        return GoogleDriveBackupHelper()
    }
}
