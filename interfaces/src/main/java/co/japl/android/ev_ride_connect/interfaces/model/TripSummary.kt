package co.japl.android.ev_ride_connect.interfaces.model

data class TripSummary(
    val distanceKm: Double,
    val durationSeconds: Long,
    val avgSpeedKmH: Double,
    val maxSpeedKmH: Double,
    val batteryConsumedPct: Int,
    val co2SavedGrams: Double,
    val estimatedWhConsumed: Double
)
