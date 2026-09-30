package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.TripSummary
import co.japl.android.ev_ride_connect.interfaces.ports.TripDatabasePort
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
            distanceKm = trip.distance,
            durationSeconds = trip.timeTrip,
            avgSpeedKmH = trip.avgSpeed,
            maxSpeedKmH = trip.maxSpeed,
            batteryConsumedPct = trip.batteryConsumed,
            co2SavedGrams = trip.co2SavedGrams,
            estimatedWhConsumed = trip.estimatedConsumptionWh
        )
    }

    fun calculateFromRawData(
        distanceKm: Double,
        durationSeconds: Long,
        gpsPointsCount: Int,
        batteryConsumed: Int = 0,
        maxSpeed: Double = 0.0,
        co2SavedGrams: Double = 0.0,
        estimatedConsumptionWh: Double = 0.0
    ): TripSummary {
        val avgSpeed = if (durationSeconds > 0) {
            (distanceKm / (durationSeconds / 3600.0))
        } else {
            0.0
        }
        return TripSummary(
            distanceKm = distanceKm,
            durationSeconds = durationSeconds,
            avgSpeedKmH = avgSpeed,
            maxSpeedKmH = maxSpeed,
            batteryConsumedPct = batteryConsumed,
            co2SavedGrams = co2SavedGrams,
            estimatedWhConsumed = estimatedConsumptionWh

        )
    }
}
