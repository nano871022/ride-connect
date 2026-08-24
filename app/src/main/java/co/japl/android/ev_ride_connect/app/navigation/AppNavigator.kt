package co.japl.android.ev_ride_connect.app.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppScreen {
    DASHBOARD,
    EV_CONFIG,
    LLM_CONFIG,
    BACKUP,
    BLE_TEST
}

class AppNavigator(initialScreen: AppScreen = AppScreen.DASHBOARD) {

    private val _currentScreen = MutableStateFlow(initialScreen)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

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
}
