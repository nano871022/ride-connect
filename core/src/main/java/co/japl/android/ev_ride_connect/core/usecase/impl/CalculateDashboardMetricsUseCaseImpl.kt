package co.japl.android.ev_ride_connect.core.usecase.impl

import co.japl.android.ev_ride_connect.core.usecase.api.CalculateDashboardMetricsUseCase
import co.japl.android.ev_ride_connect.interfaces.model.DashboardMetrics
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.utils.BatteryCalculator
import co.japl.android.ev_ride_connect.utils.DateUtils
import javax.inject.Inject

class CalculateDashboardMetricsUseCaseImpl @Inject constructor() : CalculateDashboardMetricsUseCase {
    private val calculateConsumptionUseCase = CalculateConsumptionUseCase()
    private val calculateOptimalBatteryPercentageUseCase = CalculateOptimalBatteryPercentageUseCase()

    override suspend fun execute(
        trips: List<Trip>,
        latestEvData: EvData?,
        evConfig: EvConfig?
    ): DashboardMetrics {
        // Si no hay datos en absoluto, devolver valores por defecto
        if (trips.isEmpty() && latestEvData == null && evConfig == null) {
            return DashboardMetrics()
        }

        val lastTrip = trips.lastOrNull()

        // Consumo basado en el último viaje
        val consumptionWhPerKm = if (lastTrip != null && lastTrip.distance > 0.0 && evConfig != null) {
            val voltageVal = evConfig.batteryVolts?.replace("V", "")?.toDoubleOrNull() ?: 52.0
            val ampersVal = evConfig.batteryAmpers?.replace("Ah", "")?.toDoubleOrNull() ?: 20.0
            calculateConsumptionUseCase.execute(
                batteryConsumedPercentage = lastTrip.batteryConsumed,
                batteryVoltage = voltageVal,
                batteryAmperes = ampersVal,
                distanceKm = lastTrip.distance
            )
        } else {
            0.0
        }

        // Ciclos basado en km totales del vehículo
        val totalKm = latestEvData?.km ?: 0L
        val cyclesUsed = if (totalKm > 0L) {
            (totalKm / 50L).toInt().coerceAtLeast(1)
        } else {
            0
        }

        // Porcentaje óptimo de batería basado en ciclos usados
        val optimalBatteryPercentage = calculateOptimalBatteryPercentageUseCase.execute(cyclesUsed)

        // Voltaje estimado basado en nivel de batería actual y configuración
        val estimatedVoltage = if (latestEvData != null && evConfig != null) {
            val batteryPct = latestEvData.batteryLevel.toDouble()
            val minV = evConfig.minVoltage
            val maxV = evConfig.maxVoltage
            val currentVoltage = BatteryCalculator.calculateVoltage(batteryPct.toInt().toShort(), minV, maxV)
            "${String.format("%.1f", currentVoltage)}V"
        } else {
            "N/D"
        }

        // Fecha de última carga basada en timestamp del último dato de batería
        val lastMaxChargeDate = latestEvData?.createTmst?.let { tmst ->
            if (tmst > 0) DateUtils.formatTimestamp(tmst) else "N/D"
        } ?: "N/D"

        // Día con mayor distancia recorrida
        val maxDistanceTrip = trips.maxByOrNull { it.distance }
        val lastHigherChargeDayInfo = if (maxDistanceTrip != null && maxDistanceTrip.distance > 0.0) {
            String.format("%.1f km", maxDistanceTrip.distance)
        } else {
            "N/D"
        }

        return DashboardMetrics(
            consumptionWhPerKm = consumptionWhPerKm,
            cyclesUsed = cyclesUsed,
            optimalBatteryPercentage = optimalBatteryPercentage,
            estimatedVoltage = estimatedVoltage,
            lastMaxChargeDate = lastMaxChargeDate,
            lastHigherChargeDayInfo = lastHigherChargeDayInfo
        )
    }
}
