package co.japl.android.ev_ride_connect.core.domain

data class ActiveSession(
    val id: Long = 1L,
    val isRideActive: Boolean = false,
    val startTimeMillis: Long = 0L,
    val currentDurationMillis: Long = 0L,
    val currentDistanceKm: Double = 0.0,
    val pendingLlmPrompt: String? = null,
    val pendingLlmResponse: String? = null,
    val isLlmProcessing: Boolean = false,
    val cachedTelemetryCount: Int = 0,
    val lastUpdatedTmst: Long = System.currentTimeMillis()
)
