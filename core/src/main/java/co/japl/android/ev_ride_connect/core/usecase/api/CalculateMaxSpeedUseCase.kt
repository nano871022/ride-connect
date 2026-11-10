package co.japl.android.ev_ride_connect.core.usecase.api

import co.japl.android.ev_ride_connect.interfaces.model.TripGps

interface CalculateMaxSpeedUseCase {
    suspend fun execute(gpsPoints: List<TripGps>): Double
}
