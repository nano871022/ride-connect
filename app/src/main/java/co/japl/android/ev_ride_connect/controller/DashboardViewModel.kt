package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.core.usecase.api.CalculateConsumptionChartDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.CalculateDashboardMetricsUseCase
import co.japl.android.ev_ride_connect.core.usecase.api.DashboardUseCase
import co.japl.android.ev_ride_connect.interfaces.model.DashboardMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardUseCase: DashboardUseCase,
    private val calculateDashboardMetricsUseCase: CalculateDashboardMetricsUseCase,
    private val calculateConsumptionChartDataUseCase: CalculateConsumptionChartDataUseCase
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

    private val _consumptionWhPerKm = MutableStateFlow(0.0)
    val consumptionWhPerKm: StateFlow<Double> = _consumptionWhPerKm.asStateFlow()

    private val _cyclesUsed = MutableStateFlow(0)
    val cyclesUsed: StateFlow<Int> = _cyclesUsed.asStateFlow()

    private val _optimalBatteryPercentage = MutableStateFlow(0.0)
    val optimalBatteryPercentage: StateFlow<Double> = _optimalBatteryPercentage.asStateFlow()

    private val _estimatedVoltage = MutableStateFlow("N/D")
    val estimatedVoltage: StateFlow<String> = _estimatedVoltage.asStateFlow()

    private val _lastMaxChargeDate = MutableStateFlow("N/D")
    val lastMaxChargeDate: StateFlow<String> = _lastMaxChargeDate.asStateFlow()

    private val _lastHigherChargeDayInfo = MutableStateFlow("N/D")
    val lastHigherChargeDayInfo: StateFlow<String> = _lastHigherChargeDayInfo.asStateFlow()

    private val _consumptionChartData = MutableStateFlow<List<Float>>(emptyList())
    val consumptionChartData: StateFlow<List<Float>> = _consumptionChartData.asStateFlow()

    init {
        loadLatestEvData()
        loadEvConfig()
        checkActiveLlmConfigs()
        observeActiveSession()
        loadTripsAndMetrics()
    }

    fun loadEvConfig() {
        viewModelScope.launch {
            val config = dashboardUseCase.getEvConfig()
            _evConfig.value = config
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            dashboardUseCase.observeActiveSession().collect { session ->
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
            val active = dashboardUseCase.getActiveLlmConfigs()
            _showApiKeyPrompt.value = active.isEmpty() || active.all { it.apiKey.isBlank() }
        }
    }

    fun dismissApiKeyPrompt() {
        _showApiKeyPrompt.value = false
    }

    fun loadLatestEvData() {
        viewModelScope.launch {
            val data = dashboardUseCase.getLatestEvData()
            _latestEvData.value = data
        }
    }

    fun loadTripsAndMetrics() {
        viewModelScope.launch {
            val trips = dashboardUseCase.getAllTrips()
            _lastTrip.value = trips.lastOrNull()

            val latestEvData = dashboardUseCase.getLatestEvData()
            _latestEvData.value = latestEvData

            val evConfig = _evConfig.value ?: dashboardUseCase.getEvConfig()

            val metrics = calculateDashboardMetricsUseCase.execute(
                trips = trips,
                latestEvData = latestEvData,
                evConfig = evConfig
            )

            _consumptionWhPerKm.value = metrics.consumptionWhPerKm
            _cyclesUsed.value = metrics.cyclesUsed
            _optimalBatteryPercentage.value = metrics.optimalBatteryPercentage
            _estimatedVoltage.value = metrics.estimatedVoltage
            _lastMaxChargeDate.value = metrics.lastMaxChargeDate
            _lastHigherChargeDayInfo.value = metrics.lastHigherChargeDayInfo

            // RF-07: Calcular datos del gráfico de consumo desde viajes reales
            _consumptionChartData.value = calculateConsumptionChartDataUseCase.execute(trips = trips)
        }
    }

    fun saveBatteryLevel(batteryInputValue: Double) {
        viewModelScope.launch {
            val config = _evConfig.value ?: dashboardUseCase.getEvConfig()
            val evCode = config?.id?.takeIf { it > 0 }?.toString()
                ?: config?.request?.takeIf { it.isNotBlank() }
                ?: "EV01"

            val currentKm = _latestEvData.value?.km ?: 0L
            val calculatedPercentage = dashboardUseCase.calculateDynamicBatteryPercentage(batteryInputValue, config)

            val evData = EvData(
                evCode = evCode,
                km = currentKm,
                batteryLevel = calculatedPercentage,
                createTmst = System.currentTimeMillis()
            )
            dashboardUseCase.saveEvData(evData)
            loadLatestEvData()
        }
    }

    fun saveOdometer(newKm: Long) {
        viewModelScope.launch {
            val config = _evConfig.value ?: dashboardUseCase.getEvConfig()
            val evCode = config?.id?.takeIf { it > 0 }?.toString()
                ?: config?.request?.takeIf { it.isNotBlank() }
                ?: "EV01"

            val currentBattery = _latestEvData.value?.batteryLevel ?: 100
            dashboardUseCase.updateOdometer(evCode, newKm, currentBattery)
            loadLatestEvData()
        }
    }

    fun saveEvData(km: Long, batteryInputValue: Double) {
        viewModelScope.launch {
            val config = _evConfig.value ?: dashboardUseCase.getEvConfig()
            val evCode = config?.id?.takeIf { it > 0 }?.toString()
                ?: config?.request?.takeIf { it.isNotBlank() }
                ?: "EV01"

            val calculatedPercentage = dashboardUseCase.calculateDynamicBatteryPercentage(batteryInputValue, config)

            val evData = EvData(
                evCode = evCode,
                km = km,
                batteryLevel = calculatedPercentage,
                createTmst = System.currentTimeMillis()
            )
            dashboardUseCase.saveEvData(evData)
            loadLatestEvData()
        }
    }
}

