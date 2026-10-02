package co.japl.android.ev_ride_connect.interfaces.model

data class ActiveSession(
    val id: Long = 1L,
    val isRideActive: Boolean = false,
    val isPaused: Boolean = false,
    val startTimeMs: Long = 0L,
    val startTimeMillis: Long = startTimeMs,
    val currentDurationMillis: Long = 0L,
    val currentDistanceKm: Double = 0.0,
    val currentSpeedKmH: Double = 0.0,
    val batteryConsumedPct: Int = 0,
    val initialBatteryPct: Short = 0,
    val initialOdometerKm: Long = 0L,
    val initialVoltage: Double = 0.0,
    val pendingLlmPrompt: String? = null,
    val pendingLlmResponse: String? = null,
    val isLlmProcessing: Boolean = false,
    val cachedTelemetryCount: Int = 0,
    val lastUpdatedTmst: Long = System.currentTimeMillis(),
    val motionState: MotionState = MotionState.STOPPED
)
