package co.japl.android.ev_ride_connect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import co.com.japl.ui.theme.MaterialThemeComposeUI
import co.japl.android.ev_ride_connect.app.controller.BackupViewModel
import co.japl.android.ev_ride_connect.app.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.app.controller.EvConfigViewModel
import co.japl.android.ev_ride_connect.app.controller.LlmConfigViewModel
import co.japl.android.ev_ride_connect.app.ui.AppScreen
import co.japl.android.ev_ride_connect.app.ui.BackupScreen
import co.japl.android.ev_ride_connect.app.ui.DashboardScreen
import co.japl.android.ev_ride_connect.app.ui.EvConfigScreen
import co.japl.android.ev_ride_connect.app.ui.LlmConfigScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val evConfigViewModel: EvConfigViewModel by viewModels()
    private val llmConfigViewModel: LlmConfigViewModel by viewModels()
    private val backupViewModel: BackupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialThemeComposeUI {
                var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigate = { currentScreen = it }
                    )
                    AppScreen.EV_CONFIG -> EvConfigScreen(
                        viewModel = evConfigViewModel,
                        onNavigate = { currentScreen = it }
                    )
                    AppScreen.LLM_CONFIG -> LlmConfigScreen(
                        viewModel = llmConfigViewModel,
                        onNavigate = { currentScreen = it }
                    )
                    AppScreen.BACKUP -> BackupScreen(
                        viewModel = backupViewModel,
                        onNavigate = { currentScreen = it }
                    )
                }
            }
        }
    }
}
