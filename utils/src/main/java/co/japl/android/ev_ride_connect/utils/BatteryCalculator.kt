package co.japl.android.ev_ride_connect.utils

import kotlin.math.roundToInt

object BatteryCalculator {
    private const val DEFAULT_13S_MAX_VOLTAGE = 54.6
    private const val DEFAULT_13S_MIN_VOLTAGE = 39.0

    fun calculatePercentage(
        inputVoltage: Double,
        minVoltage: Double = DEFAULT_13S_MIN_VOLTAGE,
        maxVoltage: Double = DEFAULT_13S_MAX_VOLTAGE
    ): Int {
        if (maxVoltage <= minVoltage) return 0
        if (inputVoltage >= maxVoltage) return 100
        if (inputVoltage <= minVoltage) return 0

        val percentage = ((inputVoltage - minVoltage) / (maxVoltage - minVoltage) * 100).roundToInt()
        return percentage.coerceIn(0, 100)
    }

    fun calculate13SPercentage(voltage: Int): Int {
        val inputVoltage = voltage.toDouble() / 10.0
        return calculatePercentage(inputVoltage, DEFAULT_13S_MIN_VOLTAGE, DEFAULT_13S_MAX_VOLTAGE)
    }
}
