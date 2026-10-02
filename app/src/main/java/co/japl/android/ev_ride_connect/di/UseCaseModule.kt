package co.japl.android.ev_ride_connect.di

import co.japl.android.ev_ride_connect.core.usecase.BackupUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.DashboardUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.EvConfigUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.EvDataUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.LlmConfigUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.MainScaffoldUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.SplashUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.TripDetailUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.TripUseCaseImpl
import co.japl.android.ev_ride_connect.interfaces.usecase.BackupUseCase
import co.japl.android.ev_ride_connect.interfaces.usecase.DashboardUseCase
import co.japl.android.ev_ride_connect.interfaces.usecase.EvConfigUseCase
import co.japl.android.ev_ride_connect.interfaces.usecase.EvDataUseCase
import co.japl.android.ev_ride_connect.interfaces.usecase.LlmConfigUseCase
import co.japl.android.ev_ride_connect.interfaces.usecase.MainScaffoldUseCase
import co.japl.android.ev_ride_connect.interfaces.usecase.SplashUseCase
import co.japl.android.ev_ride_connect.interfaces.usecase.TripDetailUseCase
import co.japl.android.ev_ride_connect.interfaces.usecase.TripUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindBackupUseCase(
        impl: BackupUseCaseImpl
    ): BackupUseCase

    @Binds
    @Singleton
    abstract fun bindDashboardUseCase(
        impl: DashboardUseCaseImpl
    ): DashboardUseCase

    @Binds
    @Singleton
    abstract fun bindEvConfigUseCase(
        impl: EvConfigUseCaseImpl
    ): EvConfigUseCase

    @Binds
    @Singleton
    abstract fun bindEvDataUseCase(
        impl: EvDataUseCaseImpl
    ): EvDataUseCase

    @Binds
    @Singleton
    abstract fun bindLlmConfigUseCase(
        impl: LlmConfigUseCaseImpl
    ): LlmConfigUseCase

    @Binds
    @Singleton
    abstract fun bindMainScaffoldUseCase(
        impl: MainScaffoldUseCaseImpl
    ): MainScaffoldUseCase

    @Binds
    @Singleton
    abstract fun bindSplashUseCase(
        impl: SplashUseCaseImpl
    ): SplashUseCase

    @Binds
    @Singleton
    abstract fun bindTripDetailUseCase(
        impl: TripDetailUseCaseImpl
    ): TripDetailUseCase

    @Binds
    @Singleton
    abstract fun bindTripUseCase(
        impl: TripUseCaseImpl
    ): TripUseCase
}
