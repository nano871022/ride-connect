package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.japl.ui.components.HistoryRecordData
import co.com.japl.ui.components.MaintenanceIndicatorItem
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.usecase.GetAllEvDataUseCase
import co.japl.android.ev_ride_connect.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import co.com.japl.ui.components.HistoryRecordType

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
            val data = getAllEvDataUseCase.execute()
            _evDataList.value = data
            
            val mappedRecords = mutableListOf<HistoryRecordData>()
            // Assuming data is sorted by createTmst DESC
            for (i in 0 until data.size - 1 step 2) {
                val end = data[i]
                val start = data[i + 1]
                
                mappedRecords.add(HistoryRecordData(
                    id = end.createTmst.toString(),
                    type = HistoryRecordType.RIDE,
                    timestamp = DateUtils.formatTimestamp(end.createTmst),
                    subtitle = "Urban Connect Route",
                    statusText = "Completed",
                    distanceValue = (end.km - start.km).toString(),
                    consumptionValue = "${(start.batteryLevel - end.batteryLevel)}%",
                    durationValue = DateUtils.formatDurationSeconds((end.createTmst - start.createTmst) / 1000),
                    avgSpeedValue = String.format(Locale.getDefault(), "%.1f km/h", 
                        if (end.createTmst != start.createTmst) 
                            (end.km - start.km).toDouble() / ((end.createTmst - start.createTmst) / 3600000.0)
                        else 0.0
                    )
                ))
            }
            _records.value = mappedRecords
        }
    }
}
