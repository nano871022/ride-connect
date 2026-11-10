package co.japl.android.ev_ride_connect.core.usecase.impl

import co.japl.android.ev_ride_connect.core.usecase.api.CalculateMaxSpeedUseCase
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import javax.inject.Inject

class CalculateMaxSpeedUseCaseImpl @Inject constructor() : CalculateMaxSpeedUseCase {
    override suspend fun execute(gpsPoints: List<TripGps>): Double {
        return gpsPoints.maxOfOrNull { it.speed } ?: 0.0
    }
}
