package co.japl.android.ev_ride_connect.controller

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.MotionState
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.interfaces.model.TripSummary
import co.japl.android.ev_ride_connect.interfaces.usecase.TripUseCase
import co.japl.android.ev_ride_connect.track.ScooterTrackingService
import co.japl.android.ev_ride_connect.track.TrackingSettings
import co.japl.android.ev_ride_connect.ui.HistoryFilter
import co.japl.android.ev_ride_connect.utils.GpsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tripUseCase: TripUseCase
) : ViewModel() {

    private val _isTripActive = MutableStateFlow(false)
    val isTripActive: StateFlow<Boolean> = _isTripActive.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _elapsedTimeSeconds = MutableStateFlow(0L)
    val elapsedTimeSeconds: StateFlow<Long> = _elapsedTimeSeconds.asStateFlow()

    private val _currentDistance = MutableStateFlow(0.0)
    val currentDistance: StateFlow<Double> = _currentDistance.asStateFlow()

    private val _currentSpeed = MutableStateFlow(0.0)
    val currentSpeed: StateFlow<Double> = _currentSpeed.asStateFlow()

    private val _currentAverageSpeed = MutableStateFlow(0.0)
    val currentAverageSpeed: StateFlow<Double> = _currentAverageSpeed.asStateFlow()

    private val _recordedGpsCount = MutableStateFlow(0)
    val recordedGpsCount: StateFlow<Int> = _recordedGpsCount.asStateFlow()

    private val _expectedGpsCount = MutableStateFlow(0)
    val expectedGpsCount: StateFlow<Int> = _expectedGpsCount.asStateFlow()

    private val _gpsIntervalSeconds = MutableStateFlow(5)
    val gpsIntervalSeconds: StateFlow<Int> = _gpsIntervalSeconds.asStateFlow()

    private val _gpsPointsList = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val gpsPointsList: StateFlow<List<Pair<Double, Double>>> = _gpsPointsList.asStateFlow()

    private val _co2SavedGrams = MutableStateFlow(0.0)
    val co2SavedGrams: StateFlow<Double> = _co2SavedGrams.asStateFlow()

    private val _estimatedConsumptionWh = MutableStateFlow(0.0)
    val estimatedConsumptionWh: StateFlow<Double> = _estimatedConsumptionWh.asStateFlow()

    private val _showBatteryWarning = MutableStateFlow(false)
    val showBatteryWarning: StateFlow<Boolean> = _showBatteryWarning.asStateFlow()

    private val _showStartBatteryDialog = MutableStateFlow(false)
    val showStartBatteryDialog: StateFlow<Boolean> = _showStartBatteryDialog.asStateFlow()

    private val _latestBatteryLevel = MutableStateFlow<Short>(100)
    val latestBatteryLevel: StateFlow<Short> = _latestBatteryLevel.asStateFlow()

    private val _showEndBatteryDialog = MutableStateFlow(false)
    val showEndBatteryDialog: StateFlow<Boolean> = _showEndBatteryDialog.asStateFlow()

    private val _endBatteryInputValue = MutableStateFlow(100.0)
    val endBatteryInputValue: StateFlow<Double> = _endBatteryInputValue.asStateFlow()

    private val _calculatedNewKm = MutableStateFlow(0L)
    val calculatedNewKm: StateFlow<Long> = _calculatedNewKm.asStateFlow()

    private val _tripSummary = MutableStateFlow<TripSummary?>(null)
    val tripSummary: StateFlow<TripSummary?> = _tripSummary.asStateFlow()

    private val _showSummaryDialog = MutableStateFlow(false)
    val showSummaryDialog: StateFlow<Boolean> = _showSummaryDialog.asStateFlow()


    private val _activeSession = MutableStateFlow<ActiveSession?>(null)
    val activeSession: StateFlow<ActiveSession?> = _activeSession.asStateFlow()

    private val _evConfig = MutableStateFlow<EvConfig?>(null)
    val evConfig: StateFlow<EvConfig?> = _evConfig.asStateFlow()

    private val _tripHistory = MutableStateFlow<List<Trip>>(emptyList())
    val tripHistory: StateFlow<List<Trip>> = _tripHistory.asStateFlow()

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter.asStateFlow()

    val recordedGpsPoints = mutableListOf<TripGps>()
    private var timerJob: Job? = null
    private var gpsSamplingJob: Job? = null
    private var startBatteryLevel: Short = 100
    private var locationListener: LocationListener? = null

    init {
        loadEvConfig()
        observeActiveSession()
        loadTripHistory()
    }

    fun loadEvConfig() {
        viewModelScope.launch {
            _evConfig.value = tripUseCase.getEvConfig()
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            tripUseCase.observeActiveSession().collect { session ->
                _activeSession.value = session
            }
        }
    }

    fun loadTripHistory() {
        viewModelScope.launch {
            try {
                _tripHistory.value = tripUseCase.getAllTrips()
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
            }
        }
    }

    fun filterTripsByDate(filter: HistoryFilter) {
        _selectedFilter.value = filter
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()

                when (filter) {
                    HistoryFilter.ALL -> {
                        _tripHistory.value = tripUseCase.getAllTrips()
                    }
                    HistoryFilter.WEEK -> {
                        calendar.timeInMillis = now
                        calendar.add(Calendar.DAY_OF_YEAR, -7)
                        _tripHistory.value = tripUseCase.getTripsByDate(calendar.timeInMillis, now)
                    }
                    HistoryFilter.MONTH -> {
                        calendar.timeInMillis = now
                        calendar.add(Calendar.MONTH, -1)
                        _tripHistory.value = tripUseCase.getTripsByDate(calendar.timeInMillis, now)
                    }
                    HistoryFilter.CHARGE -> {
                        val trips = tripUseCase.getAllTrips()
                        _tripHistory.value = trips.filter { it.batteryConsumed > 0 }
                    }
                }
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
            }
        }
    }

    fun onStartTripRequested() {
        viewModelScope.launch {
            val latestEvData = tripUseCase.getLatestEvData()
            _latestBatteryLevel.value = latestEvData?.batteryLevel ?: 100
            _showStartBatteryDialog.value = true
        }
    }

    fun cancelStartTrip() {
        _showStartBatteryDialog.value = false
    }

    fun confirmStartTrip(inputBatteryValue: Double) {
        _showStartBatteryDialog.value = false
        startTrip()
    }

    fun onStopTripRequested() {
        requestStopTrip()
    }

    fun startTrip() {
        if (_isTripActive.value) return
        _isTripActive.value = true
        _isPaused.value = false
        _elapsedTimeSeconds.value = 0L
        _currentDistance.value = 0.0
        _currentSpeed.value = 0.0
        _currentAverageSpeed.value = 0.0
        _recordedGpsCount.value = 0
        _expectedGpsCount.value = 0
        _co2SavedGrams.value = 0.0
        _estimatedConsumptionWh.value = 0.0
        recordedGpsPoints.clear()
        _gpsPointsList.value = emptyList()

        viewModelScope.launch {
            val latestEvData = tripUseCase.getLatestEvData()
            startBatteryLevel = latestEvData?.batteryLevel ?: 100
        }

        startTimer()
        startGpsSamplingTracker()
        startLocationUpdates()
        startTrackingService()

        val loc = fetchCurrentLocation()
        if (loc != null) {
            addLocationPoint(loc.first, loc.second)
        }
    }

    fun pauseTrip() {
        if (!_isTripActive.value || _isPaused.value) return
        _isPaused.value = true

        viewModelScope.launch {
            val updated = ActiveSession(
                isRideActive = true,
                isPaused = true,
                currentDurationMillis = _elapsedTimeSeconds.value * 1000L,
                currentDistanceKm = _currentDistance.value,
                cachedTelemetryCount = recordedGpsPoints.size,
                lastUpdatedTmst = System.currentTimeMillis()
            )
            tripUseCase.saveActiveSession(updated)
            tripUseCase.pauseTrip()
        }
    }

    fun resumeTrip() {
        if (!_isTripActive.value || !_isPaused.value) return
        _isPaused.value = false

        viewModelScope.launch {
            val existing = _activeSession.value ?: ActiveSession()
            val updated = existing.copy(
                isRideActive = true,
                isPaused = false,
                lastUpdatedTmst = System.currentTimeMillis()
            )
            tripUseCase.saveActiveSession(updated)
            tripUseCase.resumeTrip()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTripActive.value) {
                delay(1000L)
                if (!_isPaused.value) {
                    _elapsedTimeSeconds.value += 1
                }
            }
        }
    }

    private fun startGpsSamplingTracker() {
        gpsSamplingJob?.cancel()
        gpsSamplingJob = viewModelScope.launch {
            while (_isTripActive.value) {
                val intervalMs = _gpsIntervalSeconds.value * 1000L
                delay(intervalMs)
                if (!_isPaused.value) {
                    _expectedGpsCount.value += 1
                }
            }
        }
    }

    fun requestStopTrip() {
        viewModelScope.launch {
            val latestEvData = tripUseCase.getLatestEvData()
            val currentKm = latestEvData?.km ?: 0L
            _calculatedNewKm.value = currentKm + _currentDistance.value.toLong()
            _endBatteryInputValue.value = (latestEvData?.batteryLevel ?: startBatteryLevel).toDouble()
            _showEndBatteryDialog.value = true
        }
    }

    fun confirmStopTrip(inputBatteryValue: Double) {
        _showEndBatteryDialog.value = false
        val newKm = _calculatedNewKm.value
        val calculatedPercentage = tripUseCase.calculateDynamicBatteryPercentage(inputBatteryValue, _evConfig.value)
        val consumed = (startBatteryLevel - calculatedPercentage).coerceAtLeast(0)

        val summary = tripUseCase.calculateTripSummary(
            distanceKm = _currentDistance.value,
            durationSeconds = _elapsedTimeSeconds.value,
            gpsPointsCount = recordedGpsPoints.size,
            batteryConsumed = consumed
        )
        _tripSummary.value = summary
        _showSummaryDialog.value = true

        viewModelScope.launch {
            try {
                val config = _evConfig.value ?: tripUseCase.getEvConfig()
                val evCode = config?.id?.takeIf { it > 0 }?.toString()
                    ?: config?.request?.takeIf { it.isNotBlank() }
                    ?: "EV01"

                tripUseCase.saveEvData(
                    EvData(
                        evCode = evCode,
                        km = newKm,
                        batteryLevel = calculatedPercentage,
                        createTmst = System.currentTimeMillis()
                    )
                )
                tripUseCase.endTrip()
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
            }
            stopTrip(consumed)
        }
    }

    fun dismissSummaryDialog() {
        _showSummaryDialog.value = false
        _tripSummary.value = null
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
                        if (_isTripActive.value && !_isPaused.value) {
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
        }
    }

    private fun startTrackingService() {
        try {
            val intent = Intent(context, ScooterTrackingService::class.java).apply {
                action = TrackingSettings.ACTION_START_TRACKING
            }
            context.startForegroundService(intent)
        } catch (e: Exception) {
            Log.e(this@TripViewModel.javaClass.name, e.message, e)
        }
    }

    private fun stopTrackingService() {
        try {
            val intent = Intent(context, ScooterTrackingService::class.java).apply {
                action = TrackingSettings.ACTION_STOP_TRACKING
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(this@TripViewModel.javaClass.name, e.message, e)
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
        if (!_isTripActive.value || _isPaused.value) return
        if (x == 0.0 && y == 0.0) return

        val previousPoint = recordedGpsPoints.lastOrNull()
        if (previousPoint != null && previousPoint.x == x && previousPoint.y == y) return

        val orderIndex = recordedGpsPoints.size + 1

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
        _currentSpeed.value = speedSegment
        _recordedGpsCount.value = recordedGpsPoints.size
        _gpsPointsList.value = recordedGpsPoints.map { Pair(it.x, it.y) }
        _co2SavedGrams.value = tripUseCase.calculateCo2Saved(_currentDistance.value)
        val config = _evConfig.value
        val voltageVal = config?.batteryVolts?.replace("V", "")?.toDoubleOrNull() ?: 52.0
        val ampersVal = config?.batteryAmpers?.replace("Ah", "")?.toDoubleOrNull() ?: 20.0
        _estimatedConsumptionWh.value = tripUseCase.calculateConsumption(
            batteryConsumedPercentage = 10,
            batteryVoltage = voltageVal,
            batteryAmperes = ampersVal,
            distanceKm = _currentDistance.value
        )
    }

    fun stopTrip(batteryConsumed: Int = 0) {
        if (!_isTripActive.value) return
        _isTripActive.value = false
        _isPaused.value = false
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
                tripUseCase.saveTrip(trip, pointsToSave)
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
