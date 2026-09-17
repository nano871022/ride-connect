package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.japl.ui.components.HistoryRecordData
import co.com.japl.ui.components.MaintenanceIndicatorItem
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.usecase.GetAllEvDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvDataViewModel @Inject constructor(
    private val getAllEvDataUseCase: GetAllEvDataUseCase
) : ViewModel() {

    private val _evDataList = MutableStateFlow<List<EvData>>(emptyList())
    val evDataList: StateFlow<List<EvData>> = _evDataList.asStateFlow()

    private val _records = MutableStateFlow<List<HistoryRecordData>>(emptyList())
    val records: StateFlow<List<HistoryRecordData>> = _records.asStateFlow()

    private val _maintenanceIndicators = MutableStateFlow<List<MaintenanceIndicatorItem>>(emptyList())
    val maintenanceIndicators: StateFlow<List<MaintenanceIndicatorItem>> = _maintenanceIndicators.asStateFlow()


    init {
        loadEvDataHistory()
    }

    fun loadEvDataHistory() {
        viewModelScope.launch {
            _evDataList.value = getAllEvDataUseCase.execute()
        }
    }
}
