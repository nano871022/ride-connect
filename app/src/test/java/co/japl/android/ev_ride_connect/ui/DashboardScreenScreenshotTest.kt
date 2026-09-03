package co.japl.android.ev_ride_connect.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import co.com.japl.ui.theme.MaterialThemeComposeUI
import co.japl.android.ev_ride_connect.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
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

        val viewModel = DashboardViewModel(fakeEvDataPort, fakeEvConfigPort, fakeLlmConfigPort)

        composeTestRule.setContent {
            MaterialThemeComposeUI {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("120", substring = true).assertExists()
    }
}
