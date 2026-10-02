package co.japl.android.ev_ride_connect.interfaces.model

data class TripSummary(
    val distanceKm: Double = 0.0,
    val durationSeconds: Long = 0L,
    val avgSpeedKmH: Double = 0.0,
    val maxSpeedKmH: Double = 0.0,
    val batteryConsumedPct: Int = 0,
    val co2SavedGrams: Double = 0.0,
    val estimatedWhConsumed: Double = 0.0,
    val totalGpsLocationsCount: Int = 0,
    val totalDistanceKm: Double = distanceKm,
    val averageSpeedKmH: Double = avgSpeedKmH,
    val totalDurationSeconds: Long = durationSeconds,
    val batteryConsumedPercentage: Int = batteryConsumedPct
)
