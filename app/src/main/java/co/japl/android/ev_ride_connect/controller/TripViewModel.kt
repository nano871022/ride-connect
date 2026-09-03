package co.japl.android.ev_ride_connect.controller

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
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
    private val tripDatabasePort: TripDatabasePort,
    private val evDataPort: EvDataPort,
    private val evConfigPort: EvConfigPort
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

    private val _showStartBatteryDialog = MutableStateFlow(false)
    val showStartBatteryDialog: StateFlow<Boolean> = _showStartBatteryDialog.asStateFlow()

    private val _showEndBatteryDialog = MutableStateFlow(false)
    val showEndBatteryDialog: StateFlow<Boolean> = _showEndBatteryDialog.asStateFlow()

    private val _latestBatteryLevel = MutableStateFlow<Short>(0)
    val latestBatteryLevel: StateFlow<Short> = _latestBatteryLevel.asStateFlow()

    private val _calculatedNewKm = MutableStateFlow(0L)
    val calculatedNewKm: StateFlow<Long> = _calculatedNewKm.asStateFlow()

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

    fun onStartTripRequested() {
        if (_isTripActive.value) return
        viewModelScope.launch {
            val latestEvData = evDataPort.getLatestEvData()
            _latestBatteryLevel.value = latestEvData?.batteryLevel ?: 0
            _showStartBatteryDialog.value = true
        }
    }

    fun confirmStartTrip(batteryLevel: Short) {
        _showStartBatteryDialog.value = false
        viewModelScope.launch {
            val evConfig = evConfigPort.getEvConfig()
            val evCode = evConfig?.id?.takeIf { it > 0 }?.toString()
                ?: evConfig?.request?.takeIf { it.isNotBlank() }
                ?: "EV01"
            val currentEvData = evDataPort.getLatestEvData()
            val currentKm = currentEvData?.km ?: 0L

            evDataPort.saveEvData(
                EvData(
                    evCode = evCode,
                    km = currentKm,
                    batteryLevel = batteryLevel,
                    createTmst = System.currentTimeMillis()
                )
            )
            startTrip()
        }
    }

    fun cancelStartTrip() {
        _showStartBatteryDialog.value = false
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

    fun onStopTripRequested() {
        if (!_isTripActive.value) return
        viewModelScope.launch {
            val totalDistance = recordedGpsPoints.sumOf { it.distance }
            val latestEvData = evDataPort.getLatestEvData()
            val previousKm = latestEvData?.km ?: 0L
            val addedKm = Math.round(totalDistance)
            _calculatedNewKm.value = previousKm + addedKm
            _latestBatteryLevel.value = latestEvData?.batteryLevel ?: 0
            _showEndBatteryDialog.value = true
        }
    }

    fun confirmStopTrip(batteryLevel: Short) {
        _showEndBatteryDialog.value = false
        val newKm = _calculatedNewKm.value
        viewModelScope.launch {
            val evConfig = evConfigPort.getEvConfig()
            val evCode = evConfig?.id?.takeIf { it > 0 }?.toString()
                ?: evConfig?.request?.takeIf { it.isNotBlank() }
                ?: "EV01"

            evDataPort.saveEvData(
                EvData(
                    evCode = evCode,
                    km = newKm,
                    batteryLevel = batteryLevel,
                    createTmst = System.currentTimeMillis()
                )
            )
            stopTrip()
        }
    }

    fun cancelStopTrip() {
        _showEndBatteryDialog.value = false
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
