package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val evDataPort: EvDataPort,
    private val evConfigPort: EvConfigPort,
    private val llmConfigPort: LlmConfigPort,
    private val sessionStatePort: SessionStatePort
) : ViewModel() {

    private val _latestEvData = MutableStateFlow<EvData?>(null)
    val latestEvData: StateFlow<EvData?> = _latestEvData.asStateFlow()

    private val _showApiKeyPrompt = MutableStateFlow(false)
    val showApiKeyPrompt: StateFlow<Boolean> = _showApiKeyPrompt.asStateFlow()

    private val _resumedSession = MutableStateFlow<ActiveSession?>(null)
    val resumedSession: StateFlow<ActiveSession?> = _resumedSession.asStateFlow()

    init {
        loadLatestEvData()
        checkActiveLlmConfigs()
        observeActiveSession()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            sessionStatePort.observeActiveSession().collect { session ->
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
            val active = llmConfigPort.getActiveConfigs()
            _showApiKeyPrompt.value = active.isEmpty() || active.all { it.apiKey.isBlank() }
        }
    }

    fun dismissApiKeyPrompt() {
        _showApiKeyPrompt.value = false
    }

    fun loadLatestEvData() {
        viewModelScope.launch {
            _latestEvData.value = evDataPort.getLatestEvData()
        }
    }

    fun saveEvData(km: Long, batteryLevel: Short) {
        viewModelScope.launch {
            val evConfig = evConfigPort.getEvConfig()
            val evCode = evConfig?.id?.takeIf { it > 0 }?.toString()
                ?: evConfig?.request?.takeIf { it.isNotBlank() }
                ?: "EV01"

            val evData = EvData(
                evCode = evCode,
                km = km,
                batteryLevel = batteryLevel,
                createTmst = System.currentTimeMillis()
            )
            evDataPort.saveEvData(evData)
            loadLatestEvData()
        }
    }
}
