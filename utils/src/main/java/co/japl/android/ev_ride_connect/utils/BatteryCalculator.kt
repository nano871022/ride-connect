package co.japl.android.ev_ride_connect.utils

object BatteryCalculator {
    private const val MAX_VOLTAGE = 546
    private const val MIN_VOLTAGE = 390

    fun calculate13SPercentage(voltage: Int): Int {
        if (voltage >= MAX_VOLTAGE) return 100
        if (voltage <= MIN_VOLTAGE) return 0

        val percentage = ((voltage - MIN_VOLTAGE).toDouble() / (MAX_VOLTAGE - MIN_VOLTAGE) * 100).toInt()
        return percentage.coerceIn(0, 100)
    }
}
