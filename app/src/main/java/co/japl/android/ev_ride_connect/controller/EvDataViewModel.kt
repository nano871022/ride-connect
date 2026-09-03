package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvDataViewModel @Inject constructor(
    private val evDataPort: EvDataPort
) : ViewModel() {

    private val _evDataList = MutableStateFlow<List<EvData>>(emptyList())
    val evDataList: StateFlow<List<EvData>> = _evDataList.asStateFlow()

    init {
        loadEvDataHistory()
    }

    fun loadEvDataHistory() {
        viewModelScope.launch {
            _evDataList.value = evDataPort.getAllEvData()
        }
    }
}
