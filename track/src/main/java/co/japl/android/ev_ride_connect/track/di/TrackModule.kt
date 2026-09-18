package co.japl.android.ev_ride_connect.track.di

import android.content.Context
import co.japl.android.ev_ride_connect.core.ports.MotionDetectorPort
import co.japl.android.ev_ride_connect.track.MotionDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrackModule {

    @Provides
    @Singleton
    fun provideMotionDetectorPort(
        @ApplicationContext context: Context
    ): MotionDetectorPort {
        return MotionDetector(context)
    }
}
