package co.japl.android.ev_ride_connect.interfaces.model

data class ScooterState(
    val isLocked: Boolean = false,
    val speedMode: Int = 1,
    val isLightOn: Boolean = false,
    val currentSpeedKmH: Double = 0.0,
    val totalOdometerKm: Long = 0L,
    val realtimeVoltageVolts: Double = 0.0,
    val isConnected: Boolean = false
)
