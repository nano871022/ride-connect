package co.japl.android.ev_ride_connect.ble.di

import co.japl.android.ev_ride_connect.ble.TuyaBleAdapter
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    @Provides
    @Singleton
    fun provideBleScooterPort(): BleScooterPort {
        return TuyaBleAdapter()
    }
}
