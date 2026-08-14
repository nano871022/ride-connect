package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.domain.ScooterState
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScooterTrackingTracker(
    private val bleScooterPort: BleScooterPort,
    private val tripDatabasePort: TripDatabasePort,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var initialScooterState: ScooterState? = null
    private var lastScooterState: ScooterState? = null
    private var trackingJob: Job? = null

    fun startTracking() {
        if (_isTracking.value) return
        _isTracking.value = true
        initialScooterState = null
        lastScooterState = null

        trackingJob = coroutineScope.launch {
            bleScooterPort.observeScooterState().collect { state ->
                if (initialScooterState == null) {
                    initialScooterState = state
                }
                lastScooterState = state
            }
        }
    }

    suspend fun stopTracking() {
        if (!_isTracking.value) return
        _isTracking.value = false
        trackingJob?.cancel()
        trackingJob = null

        val start = initialScooterState
        val end = lastScooterState

        if (start != null && end != null) {
            val distance = (end.totalOdometer - start.totalOdometer).coerceAtLeast(0)
            val batteryConsumed = (start.batteryPercentage - end.batteryPercentage).coerceAtLeast(0)
            tripDatabasePort.saveTripData(distance = distance, batteryConsumed = batteryConsumed)
        }
    }
}
