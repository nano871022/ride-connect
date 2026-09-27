package co.japl.android.ev_ride_connect.core.usecase

import javax.inject.Inject

class CalculateOptimalBatteryPercentageUseCase @Inject constructor() {

    fun execute(usedCycles: Int, maxCycles: Int = 500, maxDegradation: Double = 20.0): Double {
        if (maxCycles <= 0) return 100.0
        val cycleRatio = (usedCycles.toDouble() / maxCycles.toDouble()).coerceIn(0.0, 1.0)
        val optimal = 100.0 - (cycleRatio * maxDegradation)
        return (Math.round(optimal * 10.0) / 10.0).coerceIn(0.0, 100.0)
    }
}
