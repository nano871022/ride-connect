package co.japl.android.ev_ride_connect.navigation

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class AppNavigatorTest {

    private lateinit var navigator: AppNavigator

    @Before
    fun setUp() {
        navigator = AppNavigator(AppScreen.DASHBOARD)
    }

    @Test
    fun shouldInitializeWithGivenScreen() {
        assertThat(navigator.currentScreen.value).isEqualTo(AppScreen.DASHBOARD)
    }

    @Test
    fun shouldNavigateToEvConfig() {
        navigator.navigateToEvConfig()
        assertThat(navigator.currentScreen.value).isEqualTo(AppScreen.EV_CONFIG)
    }

    @Test
    fun shouldNavigateToLlmConfig() {
        navigator.navigateToLlmConfig()
        assertThat(navigator.currentScreen.value).isEqualTo(AppScreen.LLM_CONFIG)
    }

    @Test
    fun shouldNavigateToBackup() {
        navigator.navigateToBackup()
        assertThat(navigator.currentScreen.value).isEqualTo(AppScreen.BACKUP)
    }

    @Test
    fun shouldNavigateToDashboard() {
        navigator.navigateToEvConfig()
        navigator.navigateToDashboard()
        assertThat(navigator.currentScreen.value).isEqualTo(AppScreen.DASHBOARD)
    }

    @Test
    fun shouldNavigateToTrip() {
        navigator.navigateToTrip()
        assertThat(navigator.currentScreen.value).isEqualTo(AppScreen.TRIP)
    }

    @Test
    fun shouldNavigateToTripDetail() {
        navigator.navigateToTripDetail(123L)
        assertThat(navigator.currentScreen.value).isEqualTo(AppScreen.TRIP_DETAIL)
        assertThat(navigator.selectedTripId.value).isEqualTo(123L)
    }
}
