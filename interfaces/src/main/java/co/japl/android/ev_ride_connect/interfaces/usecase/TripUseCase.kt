package co.japl.android.ev_ride_connect.interfaces.usecase

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.interfaces.model.TripSummary
import kotlinx.coroutines.flow.Flow

interface TripUseCase {
    suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long
    suspend fun endTrip()
    suspend fun pauseTrip()
    suspend fun resumeTrip()
    fun calculateTripSummary(
        distanceKm: Double,
        durationSeconds: Long,
        gpsPointsCount: Int,
        batteryConsumed: Int
    ): TripSummary
    fun calculateCo2Saved(distanceKm: Double): Double
    fun calculateConsumption(
        batteryConsumedPercentage: Int,
        batteryVoltage: Double,
        batteryAmperes: Double,
        distanceKm: Double
    ): Double
    fun calculateDynamicBatteryPercentage(batteryInputValue: Double, config: EvConfig?): Short
    suspend fun getActiveSession(): ActiveSession?
    fun observeActiveSession(): Flow<ActiveSession?>
    suspend fun saveActiveSession(session: ActiveSession)
    suspend fun getEvConfig(): EvConfig?
    suspend fun getLatestEvData(): EvData?
    suspend fun saveEvData(evData: EvData): Long
    suspend fun getTripDetails(tripId: Long): Pair<Trip, List<TripGps>>?
    suspend fun getAllTrips(): List<Trip>
    suspend fun getTripsByDate(startDateMs: Long, endDateMs: Long): List<Trip>
}
