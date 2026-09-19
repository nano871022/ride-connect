package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.TripSummary
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import javax.inject.Inject

class CalculateTripSummaryUseCase @Inject constructor(
    private val tripDatabasePort: TripDatabasePort
) {
    suspend fun execute(
        tripId: Long,
        batteryConsumed: Int = 0
    ): TripSummary? {
        val trip = tripDatabasePort.getTripById(tripId) ?: return null
        val points = tripDatabasePort.getGpsPointsByTripId(tripId)
        val locationCount = points.size

        return TripSummary(
            totalDistanceKm = trip.distance,
            averageSpeedKmH = trip.averageSpeed,
            totalGpsLocationsCount = locationCount,
            totalDurationSeconds = trip.timeTrip,
            batteryConsumedPercentage = batteryConsumed
        )
    }

    fun calculateFromRawData(
        distanceKm: Double,
        durationSeconds: Long,
        gpsPointsCount: Int,
        batteryConsumed: Int = 0
    ): TripSummary {
        val avgSpeed = if (durationSeconds > 0) {
            (distanceKm / (durationSeconds / 3600.0))
        } else {
            0.0
        }
        return TripSummary(
            totalDistanceKm = distanceKm,
            averageSpeedKmH = avgSpeed,
            totalGpsLocationsCount = gpsPointsCount,
            totalDurationSeconds = durationSeconds,
            batteryConsumedPercentage = batteryConsumed
        )
    }
}
