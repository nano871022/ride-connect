package co.japl.android.ev_ride_connect.ble.di

import android.content.Context
import co.japl.android.ev_ride_connect.ble.TuyaBleAdapter
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    @Provides
    @Singleton
    fun provideBleScooterPort(
        @ApplicationContext context: Context
    ): BleScooterPort {
        return TuyaBleAdapter(context)
    }
}
