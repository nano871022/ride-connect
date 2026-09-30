package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.domain.TripSummary
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.MotionDetectorPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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

class TripUseCaseImpl @Inject constructor(
    private val tripDatabasePort: TripDatabasePort,
    private val sessionStatePort: SessionStatePort,
    private val evConfigPort: EvConfigPort,
    private val evDataPort: EvDataPort,
    private val motionDetectorPort: MotionDetectorPort,
    private val saveTripUseCase: SaveTripUseCase,
    private val endTripUseCase: EndTripUseCase,
    private val pauseTripUseCase: PauseTripUseCase,
    private val resumeTripUseCase: ResumeTripUseCase,
    private val calculateTripSummaryUseCase: CalculateTripSummaryUseCase,
    private val calculateCo2SavedUseCase: CalculateCo2SavedUseCase,
    private val calculateConsumptionUseCase: CalculateConsumptionUseCase,
    private val calculateDynamicBatteryPercentageUseCase: CalculateDynamicBatteryPercentageUseCase,
    private val getTripDetailsUseCase: GetTripDetailsUseCase,
    private val getTripsByDateUseCase: GetTripsByDateUseCase,
    private val getEvConfigUseCase: GetEvConfigUseCase,
    private val getLatestEvDataUseCase: GetLatestEvDataUseCase,
    private val saveEvDataUseCase: SaveEvDataUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val observeActiveSessionUseCase: ObserveActiveSessionUseCase,
    private val saveActiveSessionUseCase: SaveActiveSessionUseCase
) : TripUseCase {
    override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
        return saveTripUseCase.execute(trip, gpsPoints)
    }

    override suspend fun endTrip() {
        endTripUseCase.execute()
    }

    override suspend fun pauseTrip() {
        pauseTripUseCase.execute()
    }

    override suspend fun resumeTrip() {
        resumeTripUseCase.execute()
    }

    override fun calculateTripSummary(
        distanceKm: Double,
        durationSeconds: Long,
        gpsPointsCount: Int,
        batteryConsumed: Int
    ): TripSummary {
        return calculateTripSummaryUseCase.calculateFromRawData(
            distanceKm,
            durationSeconds,
            gpsPointsCount,
            batteryConsumed
        )
    }

    override fun calculateCo2Saved(distanceKm: Double): Double {
        return calculateCo2SavedUseCase.execute(distanceKm)
    }

    override fun calculateConsumption(
        batteryConsumedPercentage: Int,
        batteryVoltage: Double,
        batteryAmperes: Double,
        distanceKm: Double
    ): Double {
        return calculateConsumptionUseCase.execute(
            batteryConsumedPercentage,
            batteryVoltage,
            batteryAmperes,
            distanceKm
        )
    }

    override fun calculateDynamicBatteryPercentage(batteryInputValue: Double, config: EvConfig?): Short {
        return calculateDynamicBatteryPercentageUseCase.execute(batteryInputValue, config)
    }

    override suspend fun getActiveSession(): ActiveSession? {
        return getActiveSessionUseCase.execute()
    }

    override fun observeActiveSession(): Flow<ActiveSession?> {
        return observeActiveSessionUseCase.execute()
    }

    override suspend fun saveActiveSession(session: ActiveSession) {
        saveActiveSessionUseCase.execute(session)
    }

    override suspend fun getEvConfig(): EvConfig? {
        return getEvConfigUseCase.execute()
    }

    override suspend fun getLatestEvData(): EvData? {
        return getLatestEvDataUseCase.execute()
    }

    override suspend fun saveEvData(evData: EvData): Long {
        return saveEvDataUseCase.execute(evData)
    }

    override suspend fun getTripDetails(tripId: Long): Pair<Trip, List<TripGps>>? {
        return getTripDetailsUseCase.execute(tripId)
    }

    override suspend fun getAllTrips(): List<Trip> {
        return tripDatabasePort.getAllTrips()
    }

    override suspend fun getTripsByDate(startDateMs: Long, endDateMs: Long): List<Trip> {
        return getTripsByDateUseCase.execute(startDateMs, endDateMs)
    }
}
