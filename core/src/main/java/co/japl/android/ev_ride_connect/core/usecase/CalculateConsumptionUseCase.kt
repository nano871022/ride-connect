package co.japl.android.ev_ride_connect.core.usecase

import javax.inject.Inject

class CalculateConsumptionUseCase @Inject constructor() {

    fun execute(batteryConsumedPercentage: Int, batteryVoltage: Double, batteryAmperes: Double, distanceKm: Double): Double {
        if (distanceKm <= 0.0) return 0.0
        val totalCapacityWh = batteryVoltage * batteryAmperes
        val energyUsedWh = (batteryConsumedPercentage.toDouble() / 100.0) * totalCapacityWh
        val whPerKm = energyUsedWh / distanceKm
        return Math.round(whPerKm * 10.0) / 10.0
    }
}
