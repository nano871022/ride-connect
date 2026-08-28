package co.japl.android.ev_ride_connect.controller

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.utils.GpsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tripDatabasePort: TripDatabasePort
) : ViewModel() {

    private val _isTripActive = MutableStateFlow(false)
    val isTripActive: StateFlow<Boolean> = _isTripActive.asStateFlow()

    private val _elapsedTimeSeconds = MutableStateFlow(0L)
    val elapsedTimeSeconds: StateFlow<Long> = _elapsedTimeSeconds.asStateFlow()

    private val _gpsIntervalSeconds = MutableStateFlow(60L)
    val gpsIntervalSeconds: StateFlow<Long> = _gpsIntervalSeconds.asStateFlow()

    private val _showBatteryWarning = MutableStateFlow(false)
    val showBatteryWarning: StateFlow<Boolean> = _showBatteryWarning.asStateFlow()

    private val _tripHistory = MutableStateFlow<List<Trip>>(emptyList())
    val tripHistory: StateFlow<List<Trip>> = _tripHistory.asStateFlow()

    private val _selectedTripDetail = MutableStateFlow<Pair<Trip, List<TripGps>>?>(null)
    val selectedTripDetail: StateFlow<Pair<Trip, List<TripGps>>?> = _selectedTripDetail.asStateFlow()

    private val _currentDistance = MutableStateFlow(0.0)
    val currentDistance: StateFlow<Double> = _currentDistance.asStateFlow()

    private val _currentAverageSpeed = MutableStateFlow(0.0)
    val currentAverageSpeed: StateFlow<Double> = _currentAverageSpeed.asStateFlow()

    private val recordedGpsPoints = mutableListOf<TripGps>()

    private var timerJob: Job? = null
    private var gpsSamplingJob: Job? = null

    init {
        loadTripHistory()
    }

    fun setGpsInterval(seconds: Long) {
        val interval = seconds.coerceAtLeast(1L)
        _gpsIntervalSeconds.value = interval
        _showBatteryWarning.value = interval < 30L
    }

    fun startTrip() {
        if (_isTripActive.value) return
        _isTripActive.value = true
        _elapsedTimeSeconds.value = 0L
        _currentDistance.value = 0.0
        _currentAverageSpeed.value = 0.0
        recordedGpsPoints.clear()

        val initialLocation = fetchCurrentLocation()
        if (initialLocation != null) {
            addLocationPoint(initialLocation.first, initialLocation.second)
        }

        timerJob = viewModelScope.launch {
            while (isActive && _isTripActive.value) {
                delay(1000L)
                _elapsedTimeSeconds.value += 1L
                if (_elapsedTimeSeconds.value > 0) {
                    _currentAverageSpeed.value = GpsUtils.calculateAverageSpeed(
                        _currentDistance.value,
                        _elapsedTimeSeconds.value
                    )
                }
            }
        }

        gpsSamplingJob = viewModelScope.launch {
            while (isActive && _isTripActive.value) {
                val intervalMs = _gpsIntervalSeconds.value * 1000L
                delay(intervalMs)
                if (_isTripActive.value) {
                    val location = fetchCurrentLocation()
                    if (location != null) {
                        addLocationPoint(location.first, location.second)
                    }
                }
            }
        }
    }

    fun fetchCurrentLocation(): Pair<Double, Double>? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) return null

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            if (location != null) {
                Pair(location.latitude, location.longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun addLocationPoint(x: Double, y: Double, timestamp: Long = System.currentTimeMillis()) {
        if (!_isTripActive.value) return

        val orderIndex = recordedGpsPoints.size + 1
        val previousPoint = recordedGpsPoints.lastOrNull()

        val distanceSegment = if (previousPoint != null) {
            GpsUtils.calculateDistanceKm(previousPoint.x, previousPoint.y, x, y)
        } else {
            0.0
        }

        val speedSegment = if (previousPoint != null) {
            val timeDiffMs = timestamp - previousPoint.createTmst
            GpsUtils.calculateSpeedKmH(distanceSegment, timeDiffMs)
        } else {
            0.0
        }

        val point = TripGps(
            orderIndex = orderIndex,
            speed = speedSegment,
            distance = distanceSegment,
            x = x,
            y = y,
            createTmst = timestamp
        )
        recordedGpsPoints.add(point)

        _currentDistance.value = recordedGpsPoints.sumOf { it.distance }
        _currentAverageSpeed.value = GpsUtils.calculateAverageSpeed(
            _currentDistance.value,
            _elapsedTimeSeconds.value
        )
    }

    fun stopTrip() {
        if (!_isTripActive.value) return
        _isTripActive.value = false
        timerJob?.cancel()
        timerJob = null
        gpsSamplingJob?.cancel()
        gpsSamplingJob = null

        val totalTime = _elapsedTimeSeconds.value
        val totalDistance = recordedGpsPoints.sumOf { it.distance }
        val finalAverageSpeed = GpsUtils.calculateAverageSpeed(totalDistance, totalTime)

        val trip = Trip(
            timeTrip = totalTime,
            averageSpeed = finalAverageSpeed,
            distance = totalDistance,
            createTmst = System.currentTimeMillis()
        )

        val pointsToSave = recordedGpsPoints.toList()

        viewModelScope.launch {
            tripDatabasePort.saveTrip(trip, pointsToSave)
            loadTripHistory()
        }
    }

    fun loadTripHistory() {
        viewModelScope.launch {
            _tripHistory.value = tripDatabasePort.getAllTrips()
        }
    }

    fun loadTripDetail(tripId: Long) {
        viewModelScope.launch {
            val trip = tripDatabasePort.getTripById(tripId)
            if (trip != null) {
                val gpsPoints = tripDatabasePort.getGpsPointsByTripId(tripId)
                _selectedTripDetail.value = Pair(trip, gpsPoints)
            } else {
                _selectedTripDetail.value = null
            }
        }
    }
}
