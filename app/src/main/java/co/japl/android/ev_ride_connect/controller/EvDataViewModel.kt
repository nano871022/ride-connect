package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.com.japl.ui.components.HistoryRecordData
import co.com.japl.ui.components.HistoryRecordType
import co.com.japl.ui.components.MaintenanceIndicatorItem
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.usecase.EvDataUseCase
import co.japl.android.ev_ride_connect.ui.HistoryFilter
import co.japl.android.ev_ride_connect.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EvDataViewModel @Inject constructor(
    private val evDataUseCase: EvDataUseCase
) : ViewModel() {

    private val _evDataList = MutableStateFlow<List<EvData>>(emptyList())
    val evDataList: StateFlow<List<EvData>> = _evDataList.asStateFlow()

    private val _records = MutableStateFlow<List<HistoryRecordData>>(emptyList())
    val records: StateFlow<List<HistoryRecordData>> = _records.asStateFlow()

    private val _maintenanceIndicators = MutableStateFlow<List<MaintenanceIndicatorItem>>(emptyList())
    val maintenanceIndicators: StateFlow<List<MaintenanceIndicatorItem>> = _maintenanceIndicators.asStateFlow()

    private val _tripHistory = MutableStateFlow<List<Trip>>(emptyList())
    val tripHistory: StateFlow<List<Trip>> = _tripHistory.asStateFlow()

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter.asStateFlow()

    init {
        loadEvDataHistory()
        loadTripHistory()
    }

    fun loadEvDataHistory() {
        viewModelScope.launch {
            val data = evDataUseCase.getAllEvData()
            _evDataList.value = data

            val mappedRecords = mutableListOf<HistoryRecordData>()
            for (i in 0 until data.size - 1 step 2) {
                val end = data[i]
                val start = data[i + 1]

                mappedRecords.add(
                    HistoryRecordData(
                        id = end.createTmst.toString(),
                        type = HistoryRecordType.RIDE,
                        timestamp = DateUtils.formatTimestamp(end.createTmst),
                        subtitle = "Urban Connect Route",
                        statusText = "Completed",
                        distanceValue = (end.km - start.km).toString(),
                        consumptionValue = "${(start.batteryLevel - end.batteryLevel)}%",
                        durationValue = DateUtils.formatDurationSeconds((end.createTmst - start.createTmst) / 1000),
                        avgSpeedValue = String.format(
                            Locale.getDefault(), "%.1f km/h",
                            if (end.createTmst != start.createTmst)
                                (end.km - start.km).toDouble() / ((end.createTmst - start.createTmst) / 3600000.0)
                            else 0.0
                        )
                    )
                )
            }
            _records.value = mappedRecords
        }
    }

    fun loadTripHistory() {
        viewModelScope.launch {
            _tripHistory.value = evDataUseCase.getAllTrips()
        }
    }

    fun filterTripsByDate(filter: HistoryFilter) {
        _selectedFilter.value = filter
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()

            when (filter) {
                HistoryFilter.ALL -> {
                    _tripHistory.value = evDataUseCase.getAllTrips()
                }
                HistoryFilter.WEEK -> {
                    calendar.timeInMillis = now
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    _tripHistory.value = evDataUseCase.getTripsByDate(calendar.timeInMillis, now)
                }
                HistoryFilter.MONTH -> {
                    calendar.timeInMillis = now
                    calendar.add(Calendar.MONTH, -1)
                    _tripHistory.value = evDataUseCase.getTripsByDate(calendar.timeInMillis, now)
                }
                HistoryFilter.CHARGE -> {
                    val trips = evDataUseCase.getAllTrips()
                    _tripHistory.value = trips.filter { it.batteryConsumed > 0 }
                }
            }
        }
    }
}
