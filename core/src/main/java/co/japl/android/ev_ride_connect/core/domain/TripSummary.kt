package co.japl.android.ev_ride_connect.core.domain

data class TripSummary(
    val totalDistanceKm: Double,
    val averageSpeedKmH: Double,
    val totalGpsLocationsCount: Int,
    val totalDurationSeconds: Long,
    val batteryConsumedPercentage: Int = 0
)
