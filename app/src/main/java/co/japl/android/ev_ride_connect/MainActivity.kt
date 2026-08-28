package co.japl.android.ev_ride_connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import co.com.japl.ui.theme.MaterialThemeComposeUI
import co.japl.android.ev_ride_connect.controller.BackupViewModel
import co.japl.android.ev_ride_connect.controller.BleTestViewModel
import co.japl.android.ev_ride_connect.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.controller.EvConfigViewModel
import co.japl.android.ev_ride_connect.controller.LlmConfigViewModel
import co.japl.android.ev_ride_connect.controller.TripViewModel
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import co.japl.android.ev_ride_connect.navigation.AppScreen
import co.japl.android.ev_ride_connect.ui.BackupScreen
import co.japl.android.ev_ride_connect.ui.BleTestScreen
import co.japl.android.ev_ride_connect.ui.DashboardScreen
import co.japl.android.ev_ride_connect.ui.EvConfigScreen
import co.japl.android.ev_ride_connect.ui.LlmConfigScreen
import co.japl.android.ev_ride_connect.ui.TripDetailScreen
import co.japl.android.ev_ride_connect.ui.TripScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val evConfigViewModel: EvConfigViewModel by viewModels()
    private val llmConfigViewModel: LlmConfigViewModel by viewModels()
    private val backupViewModel: BackupViewModel by viewModels()
    private val bleTestViewModel: BleTestViewModel by viewModels()
    private val tripViewModel: TripViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialThemeComposeUI {
                val navigator = remember { AppNavigator() }
                val currentScreen by navigator.currentScreen.collectAsState()

                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        viewModel = dashboardViewModel,
                        navigator = navigator
                    )
                    AppScreen.EV_CONFIG -> EvConfigScreen(
                        viewModel = evConfigViewModel,
                        navigator = navigator
                    )
                    AppScreen.LLM_CONFIG -> LlmConfigScreen(
                        viewModel = llmConfigViewModel,
                        navigator = navigator
                    )
                    AppScreen.BACKUP -> BackupScreen(
                        viewModel = backupViewModel,
                        navigator = navigator
                    )
                    AppScreen.BLE_TEST -> BleTestScreen(
                        viewModel = bleTestViewModel
                    )
                    AppScreen.TRIP -> TripScreen(
                        viewModel = tripViewModel,
                        navigator = navigator
                    )
                    AppScreen.TRIP_DETAIL -> TripDetailScreen(
                        viewModel = tripViewModel,
                        navigator = navigator
                    )
                }
            }
        }
    }
}
