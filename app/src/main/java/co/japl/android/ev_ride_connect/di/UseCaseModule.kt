package co.japl.android.ev_ride_connect.di

import co.japl.android.ev_ride_connect.core.usecase.impl.BackupUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.DashboardUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.EvConfigUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.EvDataUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.LlmConfigUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.MainScaffoldUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.SplashUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.TripDetailUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.TripUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.api.BackupUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.CalculateConsumptionChartDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.CalculateDashboardMetricsUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.CalculateMaxSpeedUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.DashboardUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.EvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.EvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.LlmConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.MainScaffoldUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.SplashUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.TripDetailUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.TripUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateConsumptionChartDataUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateDashboardMetricsUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateMaxSpeedUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.impl.SaveTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.TripWriterUseCase
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

    @Binds
    @Singleton
    abstract fun bindCalculateMaxSpeedUseCase(
        impl: CalculateMaxSpeedUseCaseImpl
    ): CalculateMaxSpeedUseCase

    @Binds
    @Singleton
    abstract fun bindCalculateDashboardMetricsUseCase(
        impl: CalculateDashboardMetricsUseCaseImpl
    ): CalculateDashboardMetricsUseCase

    @Binds
    @Singleton
    abstract fun bindCalculateConsumptionChartDataUseCase(
        impl: CalculateConsumptionChartDataUseCaseImpl
    ): CalculateConsumptionChartDataUseCase

    @Binds
    @Singleton
    abstract fun bindTripWriterUseCase(
        impl: SaveTripUseCase
    ): TripWriterUseCase
}


