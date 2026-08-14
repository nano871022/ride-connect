package co.japl.android.ev_ride_connect.core.domain

data class ScooterState(
    val isLocked: Boolean,
    val speedMode: Int,
    val currentSpeed: Int,
    val realtimeVoltage: Int,
    val batteryPercentage: Int,
    val totalOdometer: Int,
    val isLightOn: Boolean
)
