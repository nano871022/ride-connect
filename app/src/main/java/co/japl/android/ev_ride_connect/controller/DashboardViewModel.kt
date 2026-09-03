package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val evDataPort: EvDataPort,
    private val evConfigPort: EvConfigPort
) : ViewModel() {

    private val _latestEvData = MutableStateFlow<EvData?>(null)
    val latestEvData: StateFlow<EvData?> = _latestEvData.asStateFlow()

    init {
        loadLatestEvData()
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
