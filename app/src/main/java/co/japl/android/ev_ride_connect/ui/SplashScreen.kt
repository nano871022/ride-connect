package co.japl.android.ev_ride_connect.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.controller.EvConfigViewModel
import co.japl.android.ev_ride_connect.controller.LlmConfigViewModel
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navigator: AppNavigator,
    dashboardViewModel: DashboardViewModel? = null,
    evConfigViewModel: EvConfigViewModel? = null,
    llmConfigViewModel: LlmConfigViewModel? = null,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()

        // Asynchronously preload data from ViewModels / Database
        val loadJob = async<Unit> {
            dashboardViewModel?.loadLatestEvData()
            dashboardViewModel?.checkActiveLlmConfigs()
            evConfigViewModel?.loadSavedConfig()
            llmConfigViewModel?.loadConfigs()
        }

        val minSplashDelayJob = async {
            delay(3000L)
        }

        loadJob.await()
        minSplashDelayJob.await()

        val elapsedTime = System.currentTimeMillis() - startTime
        val remainingTime = 3000L - elapsedTime
        if (remainingTime > 0) {
            delay(remainingTime)
        }

        navigator.navigateToDashboard()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D111D)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_bg),
            contentDescription = stringResource(R.string.splash_loading_message),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF00E676),
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.splash_loading_message),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.splash_loading_data),
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}
