package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.usecase.GetActiveLlmConfigsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetLatestEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.ObserveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvDataUseCase
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
    private val observeActiveSessionUseCase: ObserveActiveSessionUseCase
) : ViewModel() {

    private val _latestEvData = MutableStateFlow<EvData?>(null)
    val latestEvData: StateFlow<EvData?> = _latestEvData.asStateFlow()

    private val _showApiKeyPrompt = MutableStateFlow(false)
    val showApiKeyPrompt: StateFlow<Boolean> = _showApiKeyPrompt.asStateFlow()

    private val _resumedSession = MutableStateFlow<ActiveSession?>(null)
    val resumedSession: StateFlow<ActiveSession?> = _resumedSession.asStateFlow()

    private val _vehicles = MutableStateFlow<List<EvConfig>>( emptyList() )
    val vehicles: StateFlow<List<EvConfig>> = _vehicles.asStateFlow()

    init {
        loadLatestEvData()
        checkActiveLlmConfigs()
        observeActiveSession()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            observeActiveSessionUseCase.execute().collect { session ->
                if (session != null && (session.isRideActive || session.isLlmProcessing || session.pendingLlmResponse != null)) {
                    _resumedSession.value = session
                } else {
                    _resumedSession.value = null
                }
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
            _latestEvData.value = getLatestEvDataUseCase.execute()
        }
    }

    fun saveEvData(km: Long, batteryLevel: Short) {
        viewModelScope.launch {
            val evConfig = getEvConfigUseCase.execute()
            val evCode = evConfig?.id?.takeIf { it > 0 }?.toString()
                ?: evConfig?.request?.takeIf { it.isNotBlank() }
                ?: "EV01"

            val evData = EvData(
                evCode = evCode,
                km = km,
                batteryLevel = batteryLevel,
                createTmst = System.currentTimeMillis()
            )
            saveEvDataUseCase.execute(evData)
            loadLatestEvData()
        }
    }
}
