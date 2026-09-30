package co.japl.android.ev_ride_connect.interfaces.model

data class ActiveSession(
    val isRideActive: Boolean = false,
    val isPaused: Boolean = false,
    val startTimeMs: Long = 0L,
    val currentDistanceKm: Double = 0.0,
    val currentSpeedKmH: Double = 0.0,
    val batteryConsumedPct: Int = 0,
    val initialBatteryPct: Short = 0,
    val initialOdometerKm: Long = 0L,
    val initialVoltage: Double = 0.0
)
