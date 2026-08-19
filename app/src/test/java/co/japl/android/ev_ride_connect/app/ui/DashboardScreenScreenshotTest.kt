package co.japl.android.ev_ride_connect.app.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import co.com.japl.ui.theme.MaterialThemeComposeUI
import co.japl.android.ev_ride_connect.app.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.core.domain.ScooterState
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
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
        val testState = ScooterState(
            isLocked = false,
            speedMode = 2,
            currentSpeed = 25,
            realtimeVoltage = 520,
            batteryPercentage = 85,
            totalOdometer = 120,
            isLightOn = true
        )

        val fakeBlePort = object : BleScooterPort {
            override fun observeScooterState(): Flow<ScooterState> = flowOf(testState)
            override fun sendCommand(dpId: Int, value: Any) {}
        }

        val fakeTripPort = object : TripDatabasePort {
            override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
        }

        val viewModel = DashboardViewModel(fakeBlePort, fakeTripPort)

        composeTestRule.setContent {
            MaterialThemeComposeUI {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("km/h", ignoreCase = true).assertExists()
    }
}
