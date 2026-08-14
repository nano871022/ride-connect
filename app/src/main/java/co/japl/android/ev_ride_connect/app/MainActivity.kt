package co.japl.android.ev_ride_connect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import co.com.japl.ui.theme.MaterialThemeComposeUI
import co.japl.android.ev_ride_connect.app.ui.DashboardScreen
import co.japl.android.ev_ride_connect.app.ui.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialThemeComposeUI {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}
