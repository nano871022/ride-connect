package co.japl.android.ev_ride_connect.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import co.com.japl.ui.theme.MaterialThemeComposeUI
import co.japl.android.ev_ride_connect.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.core.usecase.CalculateConsumptionUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateDynamicBatteryPercentageUseCase
import co.japl.android.ev_ride_connect.core.usecase.CalculateOptimalBatteryPercentageUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetAllTripsUseCase
import co.japl.android.ev_ride_connect.core.usecase.UpdateOdometerUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetActiveLlmConfigsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetLatestEvDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.ObserveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvDataUseCase
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DashboardScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun captureDashboardScreenshot() {
        val testEvData = EvData(
            evCode = "EV01",
            km = 120L,
            batteryLevel = 85
        )

        val fakeEvDataPort = object : EvDataPort {
            override suspend fun getLatestEvData(): EvData? = testEvData
            override suspend fun getAllEvData(): List<EvData> = listOf(testEvData)
            override suspend fun saveEvData(evData: EvData): Long = 1L
        }

        val fakeEvConfigPort = object : EvConfigPort {
            override suspend fun getEvConfig(): EvConfig? = EvConfig(id = 1L, request = "Vsett C7")
            override suspend fun saveEvConfig(config: EvConfig): Long = 1L
        }

        val fakeLlmConfigPort = object : LlmConfigPort {
            override suspend fun getAllConfigs(): List<LlmConfig> = listOf(LlmConfig(id = 1L, apiKey = "test-key", isActive = true))
            override suspend fun getActiveConfigs(): List<LlmConfig> = listOf(LlmConfig(id = 1L, apiKey = "test-key", isActive = true))
            override suspend fun saveConfig(config: LlmConfig): Long = 1L
            override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean = true
            override suspend fun deleteConfig(id: Long): Boolean = true
        }

        val fakeSessionStatePort = object : SessionStatePort {
            override suspend fun saveActiveSession(session: ActiveSession) {}
            override suspend fun getActiveSession(): ActiveSession? = null
            override fun observeActiveSession(): Flow<ActiveSession?> = flowOf(null)
            override suspend fun clearActiveSession() {}
        }

        val fakeTripPort = object : TripDatabasePort {
            override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
            override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long = 1L
            override suspend fun getAllTrips(): List<Trip> = emptyList()
            override suspend fun getTripById(tripId: Long): Trip? = null
            override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = emptyList()
            override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> = emptyList()
        }

        val viewModel = DashboardViewModel(
            GetLatestEvDataUseCase(fakeEvDataPort),
            SaveEvDataUseCase(fakeEvDataPort),
            GetEvConfigUseCase(fakeEvConfigPort),
            GetActiveLlmConfigsUseCase(fakeLlmConfigPort),
            ObserveActiveSessionUseCase(fakeSessionStatePort),
            CalculateDynamicBatteryPercentageUseCase(),
            CalculateOptimalBatteryPercentageUseCase(),
            CalculateConsumptionUseCase(),
            UpdateOdometerUseCase(fakeEvDataPort, GetLatestEvDataUseCase(fakeEvDataPort)),
            GetAllTripsUseCase(fakeTripPort)
        )

        val navigator = AppNavigator()

        composeTestRule.setContent {
            MaterialThemeComposeUI {
                DashboardScreen(viewModel = viewModel, navigator = navigator)
            }
        }

        composeTestRule.onNodeWithText("120", substring = true).assertExists()
    }
}
