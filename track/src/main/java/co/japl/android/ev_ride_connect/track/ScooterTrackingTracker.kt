package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.MotionState
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScooterTrackingTracker(
    private val tripDatabasePort: TripDatabasePort,
    private val sessionStatePort: SessionStatePort? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var segmentStartTimeMillis: Long = 0L
    private var accumulatedDurationMillis: Long = 0L
    private val telemetryQueue = mutableListOf<TripGps>()
    private var currentDistanceKm: Double = 0.0
    private var lastSavedLocation: Pair<Double, Double>? = null

    fun startTracking() {
        if (_isTracking.value) return
        _isTracking.value = true
        _isPaused.value = false
        segmentStartTimeMillis = System.currentTimeMillis()
        accumulatedDurationMillis = 0L
        telemetryQueue.clear()
        currentDistanceKm = 0.0
        lastSavedLocation = null

        coroutineScope.launch {
            val session = ActiveSession(
                isRideActive = true,
                isPaused = false,
                startTimeMillis = segmentStartTimeMillis,
                currentDurationMillis = 0L,
                currentDistanceKm = 0.0,
                cachedTelemetryCount = 0,
                lastUpdatedTmst = System.currentTimeMillis(),
                motionState = MotionState.STOPPED
            )
            sessionStatePort?.saveActiveSession(session)
        }
    }

    fun pauseTracking() {
        if (!_isTracking.value || _isPaused.value) return
        _isPaused.value = true
        val currentSegmentDuration = System.currentTimeMillis() - segmentStartTimeMillis
        accumulatedDurationMillis += currentSegmentDuration

        coroutineScope.launch {
            val existing = sessionStatePort?.getActiveSession() ?: ActiveSession()
            val updated = existing.copy(
                isRideActive = true,
                isPaused = true,
                currentDurationMillis = accumulatedDurationMillis,
                currentDistanceKm = currentDistanceKm,
                cachedTelemetryCount = telemetryQueue.size,
                lastUpdatedTmst = System.currentTimeMillis()
            )
            sessionStatePort?.saveActiveSession(updated)
        }
    }

    fun resumeTracking() {
        if (!_isTracking.value || !_isPaused.value) return
        _isPaused.value = false
        segmentStartTimeMillis = System.currentTimeMillis()

        coroutineScope.launch {
            val existing = sessionStatePort?.getActiveSession() ?: ActiveSession()
            val updated = existing.copy(
                isRideActive = true,
                isPaused = false,
                currentDurationMillis = accumulatedDurationMillis,
                currentDistanceKm = currentDistanceKm,
                cachedTelemetryCount = telemetryQueue.size,
                lastUpdatedTmst = System.currentTimeMillis()
            )
            sessionStatePort?.saveActiveSession(updated)
        }
    }

    fun recordTelemetry(
        x: Double,
        y: Double,
        speed: Double,
        distanceDelta: Double,
        motionState: MotionState = MotionState.STOPPED
    ) {
        if (!_isTracking.value || _isPaused.value) return
        if (x == 0.0 && y == 0.0) return
        if (lastSavedLocation?.first == x && lastSavedLocation?.second == y) return

        lastSavedLocation = Pair(x, y)
        currentDistanceKm += distanceDelta

        val gpsPoint = TripGps(
            orderIndex = telemetryQueue.size + 1,
            speed = speed,
            distance = currentDistanceKm,
            x = x,
            y = y,
            createTmst = System.currentTimeMillis(),
            motionState = motionState
        )
        telemetryQueue.add(gpsPoint)

        val totalDuration = getCurrentDurationMillis()

        coroutineScope.launch {
            val existing = sessionStatePort?.getActiveSession() ?: ActiveSession()
            val updated = existing.copy(
                isRideActive = true,
                isPaused = false,
                startTimeMillis = if (existing.startTimeMillis > 0) existing.startTimeMillis else segmentStartTimeMillis,
                currentDurationMillis = totalDuration,
                currentDistanceKm = currentDistanceKm,
                cachedTelemetryCount = telemetryQueue.size,
                lastUpdatedTmst = System.currentTimeMillis(),
                motionState = motionState
            )
            sessionStatePort?.saveActiveSession(updated)
        }
    }

    suspend fun stopTracking() {
        if (!_isTracking.value) return
        val finalDuration = getCurrentDurationMillis()
        _isTracking.value = false
        _isPaused.value = false

        val durationSeconds = finalDuration / 1000L
        val averageSpeed = if (finalDuration > 0) (currentDistanceKm / (finalDuration / 3600000.0)) else 0.0

        val trip = Trip(
            timeTrip = durationSeconds,
            averageSpeed = averageSpeed,
            distance = currentDistanceKm,
            createTmst = System.currentTimeMillis()
        )

        val tripId = tripDatabasePort.saveTrip(trip, telemetryQueue.toList())
        telemetryQueue.clear()
        lastSavedLocation = null

        val existing = sessionStatePort?.getActiveSession()
        if (existing != null) {
            val updated = existing.copy(
                isRideActive = false,
                isPaused = false,
                currentDurationMillis = finalDuration,
                currentDistanceKm = currentDistanceKm,
                cachedTelemetryCount = 0,
                lastUpdatedTmst = System.currentTimeMillis(),
                motionState = MotionState.STOPPED
            )
            if (updated.pendingLlmPrompt == null && !updated.isLlmProcessing) {
                sessionStatePort.clearActiveSession()
            } else {
                sessionStatePort.saveActiveSession(updated)
            }
        }
    }

    fun getCurrentDurationMillis(): Long {
        return if (_isPaused.value) {
            accumulatedDurationMillis
        } else {
            accumulatedDurationMillis + (System.currentTimeMillis() - segmentStartTimeMillis)
        }
    }

    fun getCachedTelemetryCount(): Int = telemetryQueue.size
}
