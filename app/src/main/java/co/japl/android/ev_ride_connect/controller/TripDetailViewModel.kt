package co.japl.android.ev_ride_connect.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.interfaces.usecase.TripDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val tripDetailUseCase: TripDetailUseCase
) : ViewModel() {

    private val _selectedTripDetail = MutableStateFlow<Pair<Trip, List<TripGps>>?>(null)
    val selectedTripDetail: StateFlow<Pair<Trip, List<TripGps>>?> = _selectedTripDetail.asStateFlow()

    fun loadTripDetail(tripId: Long) {
        viewModelScope.launch {
            try {
                _selectedTripDetail.value = tripDetailUseCase.getTripDetails(tripId)
            } catch (e: Exception) {
                runCatching { Log.e(this@TripDetailViewModel.javaClass.name, e.message, e) }
                _selectedTripDetail.value = null
            }
        }
    }
}
