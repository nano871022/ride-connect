package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
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

    private var startTimeMillis: Long = 0L
    private val telemetryQueue = mutableListOf<TripGps>()
    private var currentDistanceKm: Double = 0.0

    fun startTracking() {
        if (_isTracking.value) return
        _isTracking.value = true
        startTimeMillis = System.currentTimeMillis()
        telemetryQueue.clear()
        currentDistanceKm = 0.0

        coroutineScope.launch {
            val session = ActiveSession(
                isRideActive = true,
                startTimeMillis = startTimeMillis,
                currentDurationMillis = 0L,
                currentDistanceKm = 0.0,
                cachedTelemetryCount = 0,
                lastUpdatedTmst = System.currentTimeMillis()
            )
            sessionStatePort?.saveActiveSession(session)
        }
    }

    fun recordTelemetry(x: Double, y: Double, speed: Double, distanceDelta: Double) {
        if (!_isTracking.value) return
        currentDistanceKm += distanceDelta

        val gpsPoint = TripGps(
            orderIndex = telemetryQueue.size + 1,
            speed = speed,
            distance = currentDistanceKm,
            x = x,
            y = y,
            createTmst = System.currentTimeMillis()
        )
        telemetryQueue.add(gpsPoint)

        coroutineScope.launch {
            val duration = System.currentTimeMillis() - startTimeMillis
            val existing = sessionStatePort?.getActiveSession() ?: ActiveSession()
            val updated = existing.copy(
                isRideActive = true,
                startTimeMillis = if (existing.startTimeMillis > 0) existing.startTimeMillis else startTimeMillis,
                currentDurationMillis = duration,
                currentDistanceKm = currentDistanceKm,
                cachedTelemetryCount = telemetryQueue.size,
                lastUpdatedTmst = System.currentTimeMillis()
            )
            sessionStatePort?.saveActiveSession(updated)
        }
    }

    suspend fun stopTracking() {
        if (!_isTracking.value) return
        _isTracking.value = false
        val duration = System.currentTimeMillis() - startTimeMillis
        val averageSpeed = if (duration > 0) (currentDistanceKm / (duration / 3600000.0)) else 0.0

        val trip = Trip(
            timeTrip = duration,
            averageSpeed = averageSpeed,
            distance = currentDistanceKm,
            createTmst = System.currentTimeMillis()
        )

        val tripId = tripDatabasePort.saveTrip(trip, telemetryQueue.toList())
        telemetryQueue.clear()

        val existing = sessionStatePort?.getActiveSession()
        if (existing != null) {
            val updated = existing.copy(
                isRideActive = false,
                currentDurationMillis = duration,
                currentDistanceKm = currentDistanceKm,
                cachedTelemetryCount = 0,
                lastUpdatedTmst = System.currentTimeMillis()
            )
            if (updated.pendingLlmPrompt == null && !updated.isLlmProcessing) {
                sessionStatePort.clearActiveSession()
            } else {
                sessionStatePort.saveActiveSession(updated)
            }
        }
    }

    fun getCachedTelemetryCount(): Int = telemetryQueue.size
}
