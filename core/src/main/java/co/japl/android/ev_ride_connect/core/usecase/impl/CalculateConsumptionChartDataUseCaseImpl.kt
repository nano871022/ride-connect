package co.japl.android.ev_ride_connect.core.usecase.impl

import co.japl.android.ev_ride_connect.core.usecase.api.CalculateConsumptionChartDataUseCase
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import javax.inject.Inject

class CalculateConsumptionChartDataUseCaseImpl @Inject constructor() : CalculateConsumptionChartDataUseCase {

    private val POINTS_COUNT = 7

    override suspend fun execute(trips: List<Trip>): List<Float> {
        if (trips.isEmpty()) {
            return List(POINTS_COUNT) { 0f }
        }

        // Ordenar viajes por fecha (más reciente primero) y tomar los últimos 7
        val sortedTrips = trips.sortedByDescending { it.createTmst }
        val recentTrips = sortedTrips.take(POINTS_COUNT)

        // Calcular consumo relativo para cada viaje: batteryConsumed / distancia
        // Esto da una aproximación de Wh/km que se puede graficar
        val chartData = recentTrips.map { trip ->
            val distance = trip.distance.coerceAtLeast(0.1)
            val consumptionRel = (trip.batteryConsumed / distance) * 10.0
            consumptionRel.coerceIn(0.0, 100.0).toFloat()
        }

        // Si hay menos de 7 viajes, rellenar con 0 al principio
        if (chartData.size < POINTS_COUNT) {
            val padding = List(POINTS_COUNT - chartData.size) { 0f }
            return padding + chartData
        }

        return chartData
    }
}
