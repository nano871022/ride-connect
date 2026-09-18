package co.japl.android.ev_ride_connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import co.com.japl.homeconnect.about.ui.About
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
    private val llmConfigViewModel: LlmConfigViewModel by viewModels()
    private val backupViewModel: BackupViewModel by viewModels()
    private val evDataViewModel: EvDataViewModel by viewModels()
    private val tripViewModel: TripViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
            val appId = context.packageName

            MaterialThemeComposeUI {
                val navigator = remember { AppNavigator() }
                val currentScreen by navigator.currentScreen.collectAsState()

                if (currentScreen == AppScreen.SPLASH) {
                    SplashScreen(
                        navigator = navigator,
                        dashboardViewModel = dashboardViewModel,
                        evConfigViewModel = evConfigViewModel,
                        llmConfigViewModel = llmConfigViewModel
                    )
                } else {
                    Scaffold(
                        navigator = navigator,
                        currentScreen = currentScreen,
                        version = version,
                        appId = appId
                    )
                }
            }
        }
    }

    @Composable
    private fun Scaffold(navigator: AppNavigator, currentScreen: AppScreen, version: String, appId: String){
        MainScaffold(
            navigator = navigator,
            currentScreen = currentScreen,
            topBarActions = {
                TopBarActions(currentScreen)
            }
        ) { innerPadding ->
            Screen(
                currentScreen = currentScreen,
                innerPadding = innerPadding,
                navigator = navigator,
                version = version,
                appId = appId
            )
        }
    }

    @Composable
    private fun TopBarActions(currentScreen: AppScreen){
        if (currentScreen == AppScreen.EV_DATA && false) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            androidx.compose.foundation.shape.RoundedCornerShape(
                                16.dp
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Text(
                        text = "%s \n ${stringResource(R.string.history_connected)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }
            }
        }
    }

    @Composable
    private fun Screen(currentScreen: AppScreen, innerPadding: PaddingValues, navigator: AppNavigator, version: String, appId: String){
        when (currentScreen) {
            AppScreen.DASHBOARD -> DashboardScreen(
                viewModel = dashboardViewModel,
                navigator = navigator,
                modifier = Modifier.padding(innerPadding)
            )

            AppScreen.EV_CONFIG -> EvConfigScreen(
                viewModel = evConfigViewModel,
                navigator = navigator,
                modifier = Modifier.padding(innerPadding)
            )

            AppScreen.LLM_CONFIG -> LlmConfigScreen(
                viewModel = llmConfigViewModel,
                navigator = navigator,
                modifier = Modifier.padding(innerPadding)
            )

            AppScreen.BACKUP -> BackupScreen(
                viewModel = backupViewModel,
                navigator = navigator,
                modifier = Modifier.padding(innerPadding)
            )

            AppScreen.EV_DATA -> EvDataScreen(
                viewModel = evDataViewModel,
                navigator = navigator,
                modifier = Modifier.padding(innerPadding)
            )

            AppScreen.TRIP -> TripScreen(
                viewModel = tripViewModel,
                navigator = navigator,
                modifier = Modifier.padding(innerPadding)
            )

            AppScreen.TRIP_DETAIL -> TripDetailScreen(
                viewModel = tripViewModel,
                navigator = navigator,
                modifier = Modifier.padding(innerPadding)
            )

            AppScreen.ABOUT -> Box(modifier = Modifier.padding(innerPadding)) {
                About(
                    versionDetail = version,
                    applicationId = appId
                )
            }

            else -> {}
        }
    }
}
