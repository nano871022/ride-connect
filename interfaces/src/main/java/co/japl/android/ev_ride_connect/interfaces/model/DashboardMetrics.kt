package co.japl.android.ev_ride_connect.interfaces.model

data class DashboardMetrics(
    val consumptionWhPerKm: Double = 0.0,
    val cyclesUsed: Int = 0,
    val optimalBatteryPercentage: Double = 0.0,
    val estimatedVoltage: String = "N/D",
    val lastMaxChargeDate: String = "N/D",
    val lastHigherChargeDayInfo: String = "N/D"
)
