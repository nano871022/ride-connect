package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.BatteryMode
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.usecase.CalculateConsumptionUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateDynamicBatteryPercentageUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateOptimalBatteryPercentageUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetActiveLlmConfigsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetAllTripsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetLatestEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.ObserveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.UpdateOdometerUseCase
import co.japl.android.ev_ride_connect.utils.BatteryCalculator
import co.japl.android.ev_ride_connect.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getLatestEvDataUseCase: GetLatestEvDataUseCase,
    private val saveEvDataUseCase: SaveEvDataUseCase,
    private val getEvConfigUseCase: GetEvConfigUseCase,
    private val getActiveLlmConfigsUseCase: GetActiveLlmConfigsUseCase,
    private val observeActiveSessionUseCase: ObserveActiveSessionUseCase,
    private val calculateDynamicBatteryPercentageUseCase: CalculateDynamicBatteryPercentageUseCase,
    private val calculateOptimalBatteryPercentageUseCase: CalculateOptimalBatteryPercentageUseCase,
    private val calculateConsumptionUseCase: CalculateConsumptionUseCase,
    private val updateOdometerUseCase: UpdateOdometerUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase
) : ViewModel() {

    private val _latestEvData = MutableStateFlow<EvData?>(null)
    val latestEvData: StateFlow<EvData?> = _latestEvData.asStateFlow()

    private val _showApiKeyPrompt = MutableStateFlow(false)
    val showApiKeyPrompt: StateFlow<Boolean> = _showApiKeyPrompt.asStateFlow()

    private val _resumedSession = MutableStateFlow<ActiveSession?>(null)
    val resumedSession: StateFlow<ActiveSession?> = _resumedSession.asStateFlow()

    private val _vehicles = MutableStateFlow<List<EvConfig>>(emptyList())
    val vehicles: StateFlow<List<EvConfig>> = _vehicles.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _evConfig = MutableStateFlow<EvConfig?>(null)
    val evConfig: StateFlow<EvConfig?> = _evConfig.asStateFlow()

    private val _lastTrip = MutableStateFlow<Trip?>(null)
    val lastTrip: StateFlow<Trip?> = _lastTrip.asStateFlow()

    private val _consumptionWhPerKm = MutableStateFlow(18.5)
    val consumptionWhPerKm: StateFlow<Double> = _consumptionWhPerKm.asStateFlow()

    private val _cyclesUsed = MutableStateFlow(42)
    val cyclesUsed: StateFlow<Int> = _cyclesUsed.asStateFlow()

    private val _optimalBatteryPercentage = MutableStateFlow(91.6)
    val optimalBatteryPercentage: StateFlow<Double> = _optimalBatteryPercentage.asStateFlow()

    private val _estimatedVoltage = MutableStateFlow("52.4V")
    val estimatedVoltage: StateFlow<String> = _estimatedVoltage.asStateFlow()

    private val _lastMaxChargeDate = MutableStateFlow("Yesterday")
    val lastMaxChargeDate: StateFlow<String> = _lastMaxChargeDate.asStateFlow()

    private val _lastHigherChargeDayInfo = MutableStateFlow("12.0 km")
    val lastHigherChargeDayInfo: StateFlow<String> = _lastHigherChargeDayInfo.asStateFlow()

    init {
        loadLatestEvData()
        loadEvConfig()
        checkActiveLlmConfigs()
        observeActiveSession()
        loadTripsAndMetrics()
    }

    fun loadEvConfig() {
        viewModelScope.launch {
            val config = getEvConfigUseCase.execute()
            _evConfig.value = config
            recalculateMetrics()
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            observeActiveSessionUseCase.execute().collect { session ->
                if (session != null && (session.isRideActive || session.isLlmProcessing || session.pendingLlmResponse != null)) {
                    _resumedSession.value = session
                } else {
                    _resumedSession.value = null
                }
                _isTracking.value = session?.isRideActive == true
                _isPaused.value = session?.isPaused == true
            }
        }
    }

    fun checkActiveLlmConfigs() {
        viewModelScope.launch {
            val active = getActiveLlmConfigsUseCase.execute()
            _showApiKeyPrompt.value = active.isEmpty() || active.all { it.apiKey.isBlank() }
        }
    }

    fun dismissApiKeyPrompt() {
        _showApiKeyPrompt.value = false
    }

    fun loadLatestEvData() {
        viewModelScope.launch {
            val data = getLatestEvDataUseCase.execute()
            _latestEvData.value = data
            recalculateMetrics()
        }
    }

    fun loadTripsAndMetrics() {
        viewModelScope.launch {
            val trips = getAllTripsUseCase.execute()
            _lastTrip.value = trips.lastOrNull()

            val lastTripDistance = _lastTrip.value?.distance ?: 0.0
            val batteryConsumed = _lastTrip.value?.batteryConsumed ?: 10

            val config = _evConfig.value ?: getEvConfigUseCase.execute()
            val voltageVal = config?.batteryVolts?.replace("V", "")?.toDoubleOrNull() ?: 52.0
            val ampersVal = config?.batteryAmpers?.replace("Ah", "")?.toDoubleOrNull() ?: 20.0

            if (lastTripDistance > 0.0) {
                _consumptionWhPerKm.value = calculateConsumptionUseCase.execute(
                    batteryConsumedPercentage = batteryConsumed,
                    batteryVoltage = voltageVal,
                    batteryAmperes = ampersVal,
                    distanceKm = lastTripDistance
                )
            }
            recalculateMetrics()
        }
    }

    private fun recalculateMetrics() {
        val data = _latestEvData.value
        val config = _evConfig.value
        val minV = config?.minVoltage ?: 39.0
        val maxV = config?.maxVoltage ?: 54.6
        val batteryPct = data?.batteryLevel?.toDouble() ?: 100.0

        val currentVoltage = BatteryCalculator.calculateVoltage(batteryPct.toInt().toShort(), minV, maxV)
        _estimatedVoltage.value = "${String.format("%.1f", currentVoltage)}V"

        val km = data?.km ?: 0L
        val estimatedCycles = (km / 50L).toInt().coerceAtLeast(1)
        _cyclesUsed.value = estimatedCycles

        _optimalBatteryPercentage.value = calculateOptimalBatteryPercentageUseCase.execute(estimatedCycles)

        data?.createTmst?.let { tmst ->
            _lastMaxChargeDate.value = DateUtils.formatTimestamp(tmst)
        }
    }

    fun saveBatteryLevel(batteryInputValue: Double) {
        viewModelScope.launch {
            val config = _evConfig.value ?: getEvConfigUseCase.execute()
            val evCode = config?.id?.takeIf { it > 0 }?.toString()
                ?: config?.request?.takeIf { it.isNotBlank() }
                ?: "EV01"

            val currentKm = _latestEvData.value?.km ?: 0L
            val calculatedPercentage = calculateDynamicBatteryPercentageUseCase.execute(batteryInputValue, config)

            val evData = EvData(
                evCode = evCode,
                km = currentKm,
                batteryLevel = calculatedPercentage,
                createTmst = System.currentTimeMillis()
            )
            saveEvDataUseCase.execute(evData)
            loadLatestEvData()
        }
    }

    fun saveOdometer(newKm: Long) {
        viewModelScope.launch {
            val config = _evConfig.value ?: getEvConfigUseCase.execute()
            val evCode = config?.id?.takeIf { it > 0 }?.toString()
                ?: config?.request?.takeIf { it.isNotBlank() }
                ?: "EV01"

            val currentBattery = _latestEvData.value?.batteryLevel ?: 100
            updateOdometerUseCase.execute(evCode, newKm, currentBattery)
            loadLatestEvData()
        }
    }

    fun saveEvData(km: Long, batteryInputValue: Double) {
        viewModelScope.launch {
            val config = _evConfig.value ?: getEvConfigUseCase.execute()
            val evCode = config?.id?.takeIf { it > 0 }?.toString()
                ?: config?.request?.takeIf { it.isNotBlank() }
                ?: "EV01"

            val calculatedPercentage = calculateDynamicBatteryPercentageUseCase.execute(batteryInputValue, config)

            val evData = EvData(
                evCode = evCode,
                km = km,
                batteryLevel = calculatedPercentage,
                createTmst = System.currentTimeMillis()
            )
            saveEvDataUseCase.execute(evData)
            loadLatestEvData()
        }
    }
}
