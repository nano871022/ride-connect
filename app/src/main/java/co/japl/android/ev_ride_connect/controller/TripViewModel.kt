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
import co.japl.android.ev_ride_connect.core.domain.TripSummary
import co.japl.android.ev_ride_connect.core.usecase.CalculateTripSummaryUseCase
import co.japl.android.ev_ride_connect.core.usecase.EndTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetAllTripsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetGpsPointsByTripIdUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetLatestEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripByIdUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripDetailsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetTripsByDateUseCase
import co.japl.android.ev_ride_connect.core.usecase.ObserveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.PauseTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.ResumeTripUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveTripUseCase
import co.japl.android.ev_ride_connect.track.ScooterTrackingService
import co.japl.android.ev_ride_connect.track.TrackingSettings
import co.japl.android.ev_ride_connect.ui.HistoryFilter
import co.japl.android.ev_ride_connect.utils.DateUtils
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
    private val observeActiveSessionUseCase: ObserveActiveSessionUseCase,
    private val pauseTripUseCase: PauseTripUseCase,
    private val resumeTripUseCase: ResumeTripUseCase,
    private val endTripUseCase: EndTripUseCase,
    private val calculateTripSummaryUseCase: CalculateTripSummaryUseCase,
    private val getTripsByDateUseCase: GetTripsByDateUseCase,
    private val getTripDetailsUseCase: GetTripDetailsUseCase
) : ViewModel() {

    private val _isTripActive = MutableStateFlow(false)
    val isTripActive: StateFlow<Boolean> = _isTripActive.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

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

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter.asStateFlow()

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

    private val _tripSummary = MutableStateFlow<TripSummary?>(null)
    val tripSummary: StateFlow<TripSummary?> = _tripSummary.asStateFlow()

    private val _showSummaryDialog = MutableStateFlow(false)
    val showSummaryDialog: StateFlow<Boolean> = _showSummaryDialog.asStateFlow()

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
                if (session != null) {
                    _isTripActive.value = session.isRideActive
                    _isPaused.value = session.isPaused
                } else {
                    _isTripActive.value = false
                    _isPaused.value = false
                }
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
            startBatteryLevel = latestEvData?.batteryLevel ?: 0
            _latestBatteryLevel.value = startBatteryLevel
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

    fun startTrip() {
        if (_isTripActive.value) return
        _isTripActive.value = true
        _isPaused.value = false
        _elapsedTimeSeconds.value = 0L
        _currentDistance.value = 0.0
        _currentAverageSpeed.value = 0.0
        recordedGpsPoints.clear()

        startTimer()
        startGpsSampling()
        startLocationUpdates()
        startTrackingService()
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (isActive && _isTripActive.value && !_isPaused.value) {
                delay(1000.milliseconds)
                _elapsedTimeSeconds.value += 1
                _currentAverageSpeed.value = GpsUtils.calculateAverageSpeed(
                    _currentDistance.value,
                    _elapsedTimeSeconds.value
                )
            }
        }
    }

    fun pauseTrip() {
        if (!_isTripActive.value || _isPaused.value) return
        _isPaused.value = true
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch {
            try {
                pauseTripUseCase.execute()
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
            }
        }
        sendTrackingServiceAction(TrackingSettings.ACTION_PAUSE_TRACKING)
    }

    fun resumeTrip() {
        if (!_isTripActive.value || !_isPaused.value) return
        _isPaused.value = false
        viewModelScope.launch {
            try {
                resumeTripUseCase.execute()
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
            }
        }
        sendTrackingServiceAction(TrackingSettings.ACTION_RESUME_TRACKING)
        startTimer()
    }

    private fun sendTrackingServiceAction(actionName: String) {
        try {
            val intent = Intent(context, ScooterTrackingService::class.java).apply {
                action = actionName
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(this@TripViewModel.javaClass.name, e.message, e)
        }
    }

    fun setGpsInterval(intervalSeconds: Long) {
        _gpsIntervalSeconds.value = intervalSeconds
        _showBatteryWarning.value = (intervalSeconds <= 10)
        if (_isTripActive.value) {
            stopLocationUpdates()
            startLocationUpdates()
            gpsSamplingJob?.cancel()
            startGpsSampling()
        }
    }

    private fun startGpsSampling() {
        gpsSamplingJob?.cancel()
        gpsSamplingJob = viewModelScope.launch {
            while (isActive && _isTripActive.value) {
                val intervalMs = _gpsIntervalSeconds.value * 1000L
                delay(intervalMs)
                if (_isTripActive.value && !_isPaused.value) {
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
            _latestBatteryLevel.value = latestEvData?.batteryLevel ?: startBatteryLevel
            _showEndBatteryDialog.value = true
        }
    }

    fun confirmStopTrip(batteryLevel: Short) {
        _showEndBatteryDialog.value = false
        val newKm = _calculatedNewKm.value
        val consumed = (startBatteryLevel - batteryLevel).coerceAtLeast(0)

        val summary = calculateTripSummaryUseCase.calculateFromRawData(
            distanceKm = _currentDistance.value,
            durationSeconds = _elapsedTimeSeconds.value,
            gpsPointsCount = recordedGpsPoints.size,
            batteryConsumed = consumed
        )
        _tripSummary.value = summary
        _showSummaryDialog.value = true

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
                endTripUseCase.execute()
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
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
                saveTripUseCase.execute(trip, pointsToSave)
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
            }
            loadTripHistory()
        }
    }

    fun filterTripsByDate(filter: HistoryFilter) {
        _selectedFilter.value = filter
        loadTripHistory(filter)
    }

    fun loadTripHistory(filter: HistoryFilter = _selectedFilter.value) {
        viewModelScope.launch {
            _tripHistory.value = try {
                val now = System.currentTimeMillis()
                when (filter) {
                    HistoryFilter.ALL -> getAllTripsUseCase.execute()
                    HistoryFilter.WEEK -> getTripsByDateUseCase.execute(DateUtils.getStartOfDaysAgo(7, now), now)
                    HistoryFilter.MONTH -> getTripsByDateUseCase.execute(DateUtils.getStartOfDaysAgo(30, now), now)
                    HistoryFilter.CHARGE -> getAllTripsUseCase.execute().filter { it.batteryConsumed > 0 }
                }
            } catch (e: Exception) {
                Log.e(this@TripViewModel.javaClass.name, e.message, e)
                emptyList()
            }
        }
    }

    fun loadTripDetail(tripId: Long) {
        viewModelScope.launch {
            try {
                _selectedTripDetail.value = getTripDetailsUseCase.execute(tripId)
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
