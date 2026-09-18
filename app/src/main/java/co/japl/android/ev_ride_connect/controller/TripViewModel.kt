package co.japl.android.ev_ride_connect.controller

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.MotionState
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.usecase.GetAllTripsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetGpsPointsByTripIdUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetLatestEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripByIdUseCase
import co.japl.android.ev_ride_connect.core.usecase.ObserveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveTripUseCase
import co.japl.android.ev_ride_connect.track.ScooterTrackingService
import co.japl.android.ev_ride_connect.track.TrackingSettings
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
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class TripViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saveTripUseCase: SaveTripUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase,
    private val getTripByIdUseCase: GetTripByIdUseCase,
    private val getGpsPointsByTripIdUseCase: GetGpsPointsByTripIdUseCase,
    private val getLatestEvDataUseCase: GetLatestEvDataUseCase,
    private val saveEvDataUseCase: SaveEvDataUseCase,
    private val getEvConfigUseCase: GetEvConfigUseCase,
    private val observeActiveSessionUseCase: ObserveActiveSessionUseCase
) : ViewModel() {

    private val _isTripActive = MutableStateFlow(false)
    val isTripActive: StateFlow<Boolean> = _isTripActive.asStateFlow()

    private val _elapsedTimeSeconds = MutableStateFlow(0L)
    val elapsedTimeSeconds: StateFlow<Long> = _elapsedTimeSeconds.asStateFlow()

    private val _sateliteCount = MutableStateFlow(0L)
    val sateliteCount: StateFlow<Long> = _sateliteCount.asStateFlow()

    private val _metersPrecisionSatelite = MutableStateFlow(0.0)
    val metersPrecisionSatelite: StateFlow<Double> = _metersPrecisionSatelite.asStateFlow()

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

    private val _activeSession = MutableStateFlow<ActiveSession?>(null)
    val activeSession: StateFlow<ActiveSession?> = _activeSession.asStateFlow()

    private var startBatteryLevel: Short = 0
    val recordedGpsPoints = mutableListOf<TripGps>()

    private var timerJob: Job? = null
    private var gpsSamplingJob: Job? = null
    private var locationListener: LocationListener? = null

    init {
        loadTripHistory()
        viewModelScope.launch {
            observeActiveSessionUseCase.execute().collect { session ->
                _activeSession.value = session
            }
        }
    }

    fun onStartTripRequested() {
        viewModelScope.launch {
            val latestEvData = try {
                getLatestEvDataUseCase.execute()
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
                null
            }
            _latestBatteryLevel.value = latestEvData?.batteryLevel ?: 0
            _showStartBatteryDialog.value = true
        }
    }

    fun confirmStartTrip(batteryLevel: Short) {
        _showStartBatteryDialog.value = false
        startBatteryLevel = batteryLevel
        startTrip()
    }

    fun cancelStartTrip() {
        _showStartBatteryDialog.value = false
    }

    fun setGpsInterval(seconds: Long) {
        _gpsIntervalSeconds.value = seconds.coerceAtLeast(10L)
    }

    fun checkBatteryWarning(batteryLevel: Short) {
        _showBatteryWarning.value = batteryLevel <= 20
    }

    fun startTrip() {
        if (_isTripActive.value) return
        _isTripActive.value = true
        _elapsedTimeSeconds.value = 0L
        _currentDistance.value = 0.0
        _currentAverageSpeed.value = 0.0
        recordedGpsPoints.clear()

        startTrackingService()
        startLocationUpdates()

        val initialLocation = fetchCurrentLocation()
        if (initialLocation != null) {
            addLocationPoint(initialLocation.first, initialLocation.second)
        }

        timerJob = viewModelScope.launch {
            while (isActive && _isTripActive.value) {
                delay(1000L.milliseconds)
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
            val latestEvData = try {
                getLatestEvDataUseCase.execute()
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
                null
            }
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
        val consumed = (startBatteryLevel - batteryLevel).coerceAtLeast(0)
        viewModelScope.launch {
            try {
                val evConfig = getEvConfigUseCase.execute()
                val evCode = evConfig?.id?.takeIf { it > 0 }?.toString()
                    ?: evConfig?.request?.takeIf { it.isNotBlank() }
                    ?: "EV01"

                saveEvDataUseCase.execute(
                    EvData(
                        evCode = evCode,
                        km = newKm,
                        batteryLevel = batteryLevel,
                        createTmst = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
            }
            stopTrip(consumed)
        }
    }

    fun cancelStopTrip() {
        _showEndBatteryDialog.value = false
    }

    private fun startLocationUpdates() {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) return

            if (locationListener == null) {
                locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (_isTripActive.value) {
                            addLocationPoint(location.latitude, location.longitude)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
            }

            val minTimeMs = _gpsIntervalSeconds.value * 1000L
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTimeMs,
                    1f,
                    locationListener!!
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTimeMs,
                    1f,
                    locationListener!!
                )
            }
        } catch (e: Exception) {
            Log.e(this@TripViewModel.javaClass.name, e.message, e)
            null
        }
    }

    private fun stopLocationUpdates() {
        try {
            locationListener?.let { listener ->
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                locationManager?.removeUpdates(listener)
            }
            locationListener = null
        } catch (e: Exception) {
            Log.e(this@TripViewModel.javaClass.name, e.message, e)
            null
        }
    }

    private fun startTrackingService() {
        try {
            val intent = Intent(context, ScooterTrackingService::class.java).apply {
                action = TrackingSettings.ACTION_START_TRACKING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e(this@TripViewModel.javaClass.name, e.message, e)
            null
        }
    }

    private fun stopTrackingService() {
        try {
            val intent = Intent(context, ScooterTrackingService::class.java).apply {
                action = TrackingSettings.ACTION_STOP_TRACKING
            }
            context.startService(intent)
        } catch (_: Exception) {
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

            if (location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                Pair(location.latitude, location.longitude)
            } else null
        } catch (e: Exception) {
            Log.e(this@TripViewModel.javaClass.name, e.message, e)
            null
        }
    }

    fun addLocationPoint(x: Double, y: Double, timestamp: Long = System.currentTimeMillis()) {
        if (!_isTripActive.value) return
        if (x == 0.0 && y == 0.0) return

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

        val currentMotionState = _activeSession.value?.motionState ?: MotionState.STOPPED

        val point = TripGps(
            orderIndex = orderIndex,
            speed = speedSegment,
            distance = distanceSegment,
            x = x,
            y = y,
            createTmst = timestamp,
            motionState = currentMotionState
        )
        recordedGpsPoints.add(point)

        _currentDistance.value = recordedGpsPoints.sumOf { it.distance }
        _currentAverageSpeed.value = GpsUtils.calculateAverageSpeed(
            _currentDistance.value,
            _elapsedTimeSeconds.value
        )
    }

    fun stopTrip(batteryConsumed: Int = 0) {
        if (!_isTripActive.value) return
        _isTripActive.value = false
        timerJob?.cancel()
        timerJob = null
        gpsSamplingJob?.cancel()
        gpsSamplingJob = null

        stopLocationUpdates()
        stopTrackingService()

        val totalTime = _elapsedTimeSeconds.value
        val totalDistance = recordedGpsPoints.sumOf { it.distance }
        val finalAverageSpeed = GpsUtils.calculateAverageSpeed(totalDistance, totalTime)

        val trip = Trip(
            timeTrip = totalTime,
            averageSpeed = finalAverageSpeed,
            distance = totalDistance,
            batteryConsumed = batteryConsumed,
            createTmst = System.currentTimeMillis()
        )

        val pointsToSave = recordedGpsPoints.toList()

        viewModelScope.launch {
            try {
                saveTripUseCase.execute(trip, pointsToSave)
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
            }
            loadTripHistory()
        }
    }

    fun loadTripHistory() {
        viewModelScope.launch {
            _tripHistory.value = try {
                getAllTripsUseCase.execute()
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
                emptyList()
            }
        }
    }

    fun loadTripDetail(tripId: Long) {
        viewModelScope.launch {
            try {
                val trip = getTripByIdUseCase.execute(tripId)
                if (trip != null) {
                    val gpsPoints = getGpsPointsByTripIdUseCase.execute(tripId)
                    _selectedTripDetail.value = Pair(trip, gpsPoints)
                } else {
                    _selectedTripDetail.value = null
                }
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
                _selectedTripDetail.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
