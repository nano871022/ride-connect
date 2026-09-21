package co.japl.android.ev_ride_connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.com.japl.ui.theme.MaterialThemeComposeUI
import co.japl.android.ev_ride_connect.controller.BackupViewModel
import co.japl.android.ev_ride_connect.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.controller.EvConfigViewModel
import co.japl.android.ev_ride_connect.controller.EvDataViewModel
import co.japl.android.ev_ride_connect.controller.LlmConfigViewModel
import co.japl.android.ev_ride_connect.controller.TripViewModel
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import co.japl.android.ev_ride_connect.navigation.AppScreen
import co.japl.android.ev_ride_connect.ui.BackupScreen
import co.japl.android.ev_ride_connect.ui.DashboardScreen
import co.japl.android.ev_ride_connect.ui.EvConfigScreen
import co.japl.android.ev_ride_connect.ui.EvDataScreen
import co.japl.android.ev_ride_connect.ui.LlmConfigScreen
import co.japl.android.ev_ride_connect.ui.MainScaffold
import co.japl.android.ev_ride_connect.ui.SplashScreen
import co.japl.android.ev_ride_connect.ui.TripDetailScreen
import co.japl.android.ev_ride_connect.ui.TripScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val evConfigViewModel: EvConfigViewModel by viewModels()
    private val evDataViewModel: EvDataViewModel by viewModels()
    private val llmConfigViewModel: LlmConfigViewModel by viewModels()
    private val tripViewModel: TripViewModel by viewModels()
    private val backupViewModel: BackupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialThemeComposeUI {
                MainAppContent(
                    dashboardViewModel = dashboardViewModel,
                    evConfigViewModel = evConfigViewModel,
                    evDataViewModel = evDataViewModel,
                    llmConfigViewModel = llmConfigViewModel,
                    tripViewModel = tripViewModel,
                    backupViewModel = backupViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    dashboardViewModel: DashboardViewModel,
    evConfigViewModel: EvConfigViewModel,
    evDataViewModel: EvDataViewModel,
    llmConfigViewModel: LlmConfigViewModel,
    tripViewModel: TripViewModel,
    backupViewModel: BackupViewModel
) {
    val navigator = remember { AppNavigator(AppScreen.SPLASH) }
    val currentScreen by navigator.currentScreen.collectAsState()

    if (currentScreen == AppScreen.SPLASH) {
        SplashScreen(
            navigator = navigator,
            dashboardViewModel = dashboardViewModel,
            evConfigViewModel = evConfigViewModel,
            llmConfigViewModel = llmConfigViewModel
        )
    } else {
        MainScaffold(
            currentScreen = currentScreen,
            navigator = navigator,
            content = { innerPadding ->
                AppNavigationContent(
                    currentScreen = currentScreen,
                    navigator = navigator,
                    dashboardViewModel = dashboardViewModel,
                    evConfigViewModel = evConfigViewModel,
                    evDataViewModel = evDataViewModel,
                    llmConfigViewModel = llmConfigViewModel,
                    tripViewModel = tripViewModel,
                    backupViewModel = backupViewModel,
                    innerPadding = innerPadding
                )
            }
        )
    }
}

@Composable
fun AppNavigationContent(
    currentScreen: AppScreen,
    navigator: AppNavigator,
    dashboardViewModel: DashboardViewModel,
    evConfigViewModel: EvConfigViewModel,
    evDataViewModel: EvDataViewModel,
    llmConfigViewModel: LlmConfigViewModel,
    tripViewModel: TripViewModel,
    backupViewModel: BackupViewModel,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val pInfo = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val version = pInfo?.versionName ?: "1.0.0"
    val appId = context.packageName ?: "co.japl.android.ev_ride_connect"

    Box(modifier = Modifier.padding(innerPadding)) {
        when (currentScreen) {
            AppScreen.DASHBOARD -> DashboardScreen(
                viewModel = dashboardViewModel,
                navigator = navigator
            )

            AppScreen.EV_CONFIG -> EvConfigScreen(
                viewModel = evConfigViewModel,
                navigator = navigator
            )

            AppScreen.EV_DATA -> EvDataScreen(
                viewModel = evDataViewModel,
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

            AppScreen.TRIP -> TripScreen(
                viewModel = tripViewModel,
                navigator = navigator
            )

            AppScreen.TRIP_DETAIL -> TripDetailScreen(
                viewModel = tripViewModel,
                navigator = navigator
            )

            AppScreen.ABOUT -> Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.about),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            else -> {}
        }
    }
}
