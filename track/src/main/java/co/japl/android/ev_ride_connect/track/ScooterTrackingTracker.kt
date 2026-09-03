package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScooterTrackingTracker(
    private val tripDatabasePort: TripDatabasePort,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var startTimeMillis: Long = 0L

    fun startTracking() {
        if (_isTracking.value) return
        _isTracking.value = true
        startTimeMillis = System.currentTimeMillis()
    }

    suspend fun stopTracking() {
        if (!_isTracking.value) return
        _isTracking.value = false
        val duration = System.currentTimeMillis() - startTimeMillis
        tripDatabasePort.saveTripData(distance = 0, batteryConsumed = 0)
    }
}
