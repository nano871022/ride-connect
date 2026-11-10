package co.japl.android.ev_ride_connect.core.usecase.impl

import co.japl.android.ev_ride_connect.core.usecase.api.CalculateMaxSpeedUseCase
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import javax.inject.Inject

/**
 * RF-01 / RF-02: Único punto de escritura de viajes en Core.
 * Orquesta el cálculo de maxSpeed desde los puntos GPS y la persistencia del viaje.
 * El ViewModel es el único llamador; el tracker deja de escribir viajes.
 */
class FinalizeTripUseCase @Inject constructor(
    private val calculateMaxSpeedUseCase: CalculateMaxSpeedUseCase,
    private val tripWriterUseCase: TripWriterUseCase
) {
    suspend fun execute(trip: Trip, gpsPoints: List<TripGps>): Long {
        val maxSpeed = calculateMaxSpeedUseCase.execute(gpsPoints)
        val tripWithMaxSpeed = trip.copy(maxSpeed = maxSpeed)
        return tripWriterUseCase.execute(tripWithMaxSpeed, gpsPoints)
    }
}
