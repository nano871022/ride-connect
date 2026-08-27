package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.core.domain.ScooterState
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val podamFactory = PodamFactoryImpl()
    private lateinit var fakeBlePort: FakeBleScooterPort
    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeBlePort = FakeBleScooterPort()
        fakeTripPort = FakeTripDatabasePort()
        viewModel = DashboardViewModel(fakeBlePort, fakeTripPort)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldObserveScooterStateFromBlePort() = runTest {
        val expectedState = ScooterState(
            isLocked = true,
            speedMode = 2,
            currentSpeed = 25,
            realtimeVoltage = 520,
            batteryPercentage = 85,
            totalOdometer = 250,
            isLightOn = true
        )

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.scooterState.collect {}
        }
        fakeBlePort.emitState(expectedState)
        testScheduler.runCurrent()

        assertThat(viewModel.scooterState.value).isEqualTo(expectedState)
        collectJob.cancel()
    }

    @Test
    fun shouldToggleLockCommand() = runTest {
        val initialState = ScooterState(
            isLocked = false,
            speedMode = 1,
            currentSpeed = 0,
            realtimeVoltage = 500,
            batteryPercentage = 80,
            totalOdometer = 100,
            isLightOn = false
        )
        fakeBlePort.emitState(initialState)
        testScheduler.runCurrent()

        viewModel.toggleLock()

        assertThat(fakeBlePort.lastCommandDpId).isEqualTo(1)
        assertThat(fakeBlePort.lastCommandValue).isEqualTo(true)
    }

    @Test
    fun shouldToggleLightCommand() = runTest {
        val initialState = ScooterState(
            isLocked = false,
            speedMode = 1,
            currentSpeed = 0,
            realtimeVoltage = 500,
            batteryPercentage = 80,
            totalOdometer = 100,
            isLightOn = false
        )
        fakeBlePort.emitState(initialState)
        testScheduler.runCurrent()

        viewModel.toggleLight()

        assertThat(fakeBlePort.lastCommandDpId).isEqualTo(4)
        assertThat(fakeBlePort.lastCommandValue).isEqualTo(true)
    }

    @Test
    fun shouldSetSpeedModeCommand() = runTest {
        viewModel.setSpeedMode(2)

        assertThat(fakeBlePort.lastCommandDpId).isEqualTo(2)
        assertThat(fakeBlePort.lastCommandValue).isEqualTo(2)
    }

    private class FakeBleScooterPort : BleScooterPort {
        private val _stateFlow = MutableStateFlow(
            ScooterState(
                isLocked = false,
                speedMode = 0,
                currentSpeed = 0,
                realtimeVoltage = 0,
                batteryPercentage = 0,
                totalOdometer = 0,
                isLightOn = false
            )
        )

        var lastCommandDpId: Int? = null
        var lastCommandValue: Any? = null

        fun emitState(state: ScooterState) {
            _stateFlow.value = state
        }

        override fun observeScooterState() = _stateFlow.asStateFlow()

        override fun sendCommand(dpId: Int, value: Any) {
            lastCommandDpId = dpId
            lastCommandValue = value
        }
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
    }
}
