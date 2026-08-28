package co.japl.android.ev_ride_connect.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppScreen {
    DASHBOARD,
    EV_CONFIG,
    LLM_CONFIG,
    BACKUP,
    BLE_TEST,
    TRIP,
    TRIP_DETAIL
}

class AppNavigator(initialScreen: AppScreen = AppScreen.DASHBOARD) {

    private val _currentScreen = MutableStateFlow(initialScreen)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedTripId = MutableStateFlow<Long?>(null)
    val selectedTripId: StateFlow<Long?> = _selectedTripId.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun navigateToDashboard() {
        navigateTo(AppScreen.DASHBOARD)
    }

    fun navigateToEvConfig() {
        navigateTo(AppScreen.EV_CONFIG)
    }

    fun navigateToLlmConfig() {
        navigateTo(AppScreen.LLM_CONFIG)
    }

    fun navigateToBackup() {
        navigateTo(AppScreen.BACKUP)
    }

    fun navigateToBleTest() {
        navigateTo(AppScreen.BLE_TEST)
    }

    fun navigateToTrip() {
        navigateTo(AppScreen.TRIP)
    }

    fun navigateToTripDetail(tripId: Long) {
        _selectedTripId.value = tripId
        navigateTo(AppScreen.TRIP_DETAIL)
    }
}
