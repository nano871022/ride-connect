package co.japl.android.ev_ride_connect.core.usecase.impl

import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps

/**
 * Interfaz única para escribir un viaje y sus puntos GPS a la base de datos.
 * RF-01: un único punto de escritura para evitar duplicados y datos inconsistentes.
 */
interface TripWriterUseCase {
    suspend fun execute(trip: Trip, gpsPoints: List<TripGps>): Long
}

