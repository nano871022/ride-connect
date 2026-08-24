package co.japl.android.ev_ride_connect.app.controller

import co.japl.android.ev_ride_connect.core.domain.BleLogDirection
import co.japl.android.ev_ride_connect.core.domain.BleLogEntry
import co.japl.android.ev_ride_connect.core.domain.ScooterState
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

@OptIn(ExperimentalCoroutinesApi::class)
class BleTestViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeBlePort: FakeBlePort
    private lateinit var viewModel: BleTestViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeBlePort = FakeBlePort()
        viewModel = BleTestViewModel(fakeBlePort)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldToggleConnectionState() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isConnected.collect {}
        }

        assertThat(viewModel.isConnected.value).isFalse()

        viewModel.toggleConnection()
        testScheduler.runCurrent()
        assertThat(viewModel.isConnected.value).isTrue()

        viewModel.toggleConnection()
        testScheduler.runCurrent()
        assertThat(viewModel.isConnected.value).isFalse()

        collectJob.cancel()
    }

    @Test
    fun shouldSendLockCommand() = runTest {
        viewModel.sendLockCommand(true)

        assertThat(fakeBlePort.lastSentDpId).isEqualTo(1)
        assertThat(fakeBlePort.lastSentValue).isEqualTo(true)
    }

    @Test
    fun shouldSendSpeedModeCommand() = runTest {
        viewModel.sendSpeedModeCommand(2)

        assertThat(fakeBlePort.lastSentDpId).isEqualTo(2)
        assertThat(fakeBlePort.lastSentValue).isEqualTo(2)
    }

    @Test
    fun shouldSendLightCommand() = runTest {
        viewModel.sendLightCommand(true)

        assertThat(fakeBlePort.lastSentDpId).isEqualTo(4)
        assertThat(fakeBlePort.lastSentValue).isEqualTo(true)
    }

    @Test
    fun shouldSendBatteryQueryCommand() = runTest {
        viewModel.sendBatteryQueryCommand()

        assertThat(fakeBlePort.lastSentDpId).isEqualTo(7)
        assertThat(fakeBlePort.lastSentValue).isEqualTo(0)
    }

    @Test
    fun shouldClearLogsWhenRequested() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.logs.collect {}
        }

        fakeBlePort.addLog(
            BleLogEntry(
                direction = BleLogDirection.SENT,
                rawBytesHex = "55 AA 00 06 00 05 01 01 00 01 01 ED",
                parsedData = "DP ID: 1, Value: true",
                isValid = true
            )
        )
        testScheduler.runCurrent()
        assertThat(viewModel.logs.value).isNotEmpty

        viewModel.clearLogs()
        testScheduler.runCurrent()

        assertThat(viewModel.logs.value).isEmpty()
        collectJob.cancel()
    }

    @Test
    fun shouldFormatLogsForExportCorrectly() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.logs.collect {}
        }

        fakeBlePort.addLog(
            BleLogEntry(
                direction = BleLogDirection.SENT,
                rawBytesHex = "55 AA 00 06 00 05 01 01 00 01 01 ED",
                parsedData = "DP ID: 1, Value: true",
                isValid = true
            )
        )
        testScheduler.runCurrent()

        val exportedText = viewModel.formatLogsForExport()

        assertThat(exportedText).contains("=== BLE COMMUNICATION TEST LOG ===")
        assertThat(exportedText).contains("DP ID: 1, Value: true")
        assertThat(exportedText).contains("Status: VALID")

        collectJob.cancel()
    }

    private class FakeBlePort : BleScooterPort {
        private val _isConnected = MutableStateFlow(false)
        private val _logs = MutableStateFlow<List<BleLogEntry>>(emptyList())
        private val _scooterState = MutableStateFlow(
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

        var lastSentDpId: Int? = null
        var lastSentValue: Any? = null

        override fun observeScooterState(): Flow<ScooterState> = _scooterState.asStateFlow()
        override fun observeConnectionState(): Flow<Boolean> = _isConnected.asStateFlow()
        override fun observeRawLogs(): Flow<List<BleLogEntry>> = _logs.asStateFlow()

        override fun sendCommand(dpId: Int, value: Any) {
            lastSentDpId = dpId
            lastSentValue = value
        }

        override fun connect(macAddress: String?) {
            _isConnected.value = true
        }

        override fun disconnect() {
            _isConnected.value = false
        }

        override fun clearLogs() {
            _logs.value = emptyList()
        }

        fun addLog(entry: BleLogEntry) {
            _logs.value = _logs.value + entry
        }
    }
}
