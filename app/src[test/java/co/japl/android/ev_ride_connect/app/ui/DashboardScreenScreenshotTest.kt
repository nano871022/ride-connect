package co.japl.android.ev_ride_connect.app.ui

import android.graphics.Bitmap
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.drawToBitmap
import co.com.japl.ui.theme.MaterialThemeComposeUI
import co.japl.android.ev_ride_connect.app.controller.DashboardViewModel
import co.japl.android.ev_ride_connect.core.domain.ScooterState
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DashboardScreenScreenshotTest {

    class TestDashboardViewModel(
        bleScooterPort: BleScooterPort,
        tripDatabasePort: TripDatabasePort,
        initialState: ScooterState
    ) : DashboardViewModel(bleScooterPort, tripDatabasePort) {
        override val scooterState: StateFlow<ScooterState> = MutableStateFlow(initialState)
    }

    @Test
    fun captureDashboardScreenshot() {
        val fakeBlePort = object : BleScooterPort {
            private val state = MutableStateFlow(
                ScooterState(
                    isLocked = false,
                    speedMode = 2,
                    currentSpeed = 25,
                    realtimeVoltage = 520,
                    batteryPercentage = 85,
                    totalOdometer = 120,
                    isLightOn = true
                )
            )
            override fun observeScooterState(): Flow<ScooterState> = state
            override fun sendCommand(dpId: Int, value: Any) {}
        }

        val fakeTripPort = object : TripDatabasePort {
            override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
        }

        val viewModel = TestDashboardViewModel(
            fakeBlePort,
            fakeTripPort,
            ScooterState(
                isLocked = false,
                speedMode = 2,
                currentSpeed = 25,
                realtimeVoltage = 520,
                batteryPercentage = 85,
                totalOdometer = 120,
                isLightOn = true
            )
        )

        val controller = Robolectric.buildActivity(ComponentActivity::class.java).create().start().resume()
        val activity = controller.get()

        val composeView = ComposeView(activity).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                MaterialThemeComposeUI {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }

        activity.setContentView(composeView)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
        )
        composeView.layout(0, 0, 1080, 1920)

        val bitmap = composeView.drawToBitmap()

        val screenshotDir = File("/home/jules/verification/screenshots")
        screenshotDir.mkdirs()
        val screenshotFile = File(screenshotDir, "verification.png")
        FileOutputStream(screenshotFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("Screenshot saved successfully to ${screenshotFile.absolutePath}")
    }
}
