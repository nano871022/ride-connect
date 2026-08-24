package co.japl.android.ev_ride_connect.ble

import co.japl.android.ev_ride_connect.core.domain.BleLogDirection
import co.japl.android.ev_ride_connect.core.domain.ScooterState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TuyaBleAdapterTest {

    private lateinit var adapter: TuyaBleAdapter

    @Before
    fun setUp() {
        adapter = TuyaBleAdapter()
    }

    @Test
    fun shouldReturnDefaultScooterStateInitially() = runTest {
        val state = adapter.observeScooterState().first()

        assertThat(state).isEqualTo(
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
    }

    @Test
    fun shouldUpdateLockStatusWhenDp1Received() = runTest {
        adapter.onDataPointReceived(dpId = 1, value = true)

        val state = adapter.observeScooterState().first()

        assertThat(state.isLocked).isTrue()
    }

    @Test
    fun shouldUpdateSpeedModeWhenDp2Received() = runTest {
        adapter.onDataPointReceived(dpId = 2, value = 2)

        val state = adapter.observeScooterState().first()

        assertThat(state.speedMode).isEqualTo(2)
    }

    @Test
    fun shouldUpdateLightSwitchWhenDp4Received() = runTest {
        adapter.onDataPointReceived(dpId = 4, value = true)

        val state = adapter.observeScooterState().first()

        assertThat(state.isLightOn).isTrue()
    }

    @Test
    fun shouldUpdateCurrentSpeedWhenDp5Received() = runTest {
        adapter.onDataPointReceived(dpId = 5, value = 25)

        val state = adapter.observeScooterState().first()

        assertThat(state.currentSpeed).isEqualTo(25)
    }

    @Test
    fun shouldUpdateTotalOdometerWhenDp6Received() = runTest {
        adapter.onDataPointReceived(dpId = 6, value = 1500)

        val state = adapter.observeScooterState().first()

        assertThat(state.totalOdometer).isEqualTo(1500)
    }

    @Test
    fun shouldUpdateVoltageAndCalculateBatteryPercentageWhenDp7Received() = runTest {
        // Voltage = 546 (54.6V) -> 100%
        adapter.onDataPointReceived(dpId = 7, value = 546)

        val state = adapter.observeScooterState().first()

        assertThat(state.realtimeVoltage).isEqualTo(546)
        assertThat(state.batteryPercentage).isEqualTo(100)
    }

    @Test
    fun shouldUpdateMultipleDataPointsWhenPayloadMapReceived() = runTest {
        val payload = mapOf<Int, Any>(
            1 to true,
            2 to 3,
            4 to true,
            5 to 30,
            6 to 2500,
            7 to 468
        )

        adapter.onDataPointsReceived(payload)

        val state = adapter.observeScooterState().first()

        assertThat(state.isLocked).isTrue()
        assertThat(state.speedMode).isEqualTo(3)
        assertThat(state.isLightOn).isTrue()
        assertThat(state.currentSpeed).isEqualTo(30)
        assertThat(state.totalOdometer).isEqualTo(2500)
        assertThat(state.realtimeVoltage).isEqualTo(468)
        assertThat(state.batteryPercentage).isEqualTo(50)
    }

    @Test
    fun shouldEmitCommandAndRecordSentLogWhenSendCommandCalled() = runTest {
        val sentCommands = mutableListOf<Pair<Int, Any>>()
        val listener: (Int, Any) -> Unit = { dpId, value ->
            sentCommands.add(dpId to value)
        }
        adapter.setOnSendCommandListener(listener)

        adapter.sendCommand(dpId = 1, value = true)

        assertThat(sentCommands).containsExactly(1 to true)

        val logs = adapter.observeRawLogs().first()
        assertThat(logs).hasSize(1)
        assertThat(logs[0].direction).isEqualTo(BleLogDirection.SENT)
        assertThat(logs[0].parsedData).contains("DP ID: 1, Value: true")
    }

    @Test
    fun shouldClearLogsWhenClearLogsCalled() = runTest {
        adapter.sendCommand(dpId = 1, value = true)
        assertThat(adapter.observeRawLogs().first()).isNotEmpty

        adapter.clearLogs()

        assertThat(adapter.observeRawLogs().first()).isEmpty()
    }

    @Test
    fun shouldLogConnectionFailureWhenBluetoothAdapterIsNull() = runTest {
        // TuyaBleAdapter created with null context has null bluetoothAdapter
        adapter.connect("AA:BB:CC:DD:EE:FF")

        val logs = adapter.observeRawLogs().first()
        assertThat(logs).hasSize(1)
        assertThat(logs[0].direction).isEqualTo(BleLogDirection.SENT)
        assertThat(logs[0].isValid).isFalse()
        assertThat(logs[0].errorMessage).contains("Bluetooth adapter is null")
    }

    @Test
    fun shouldLogDisconnectRequestWhenDisconnectCalled() = runTest {
        adapter.disconnect()

        val logs = adapter.observeRawLogs().first()
        assertThat(logs).hasSize(1)
        assertThat(logs[0].direction).isEqualTo(BleLogDirection.SENT)
        assertThat(logs[0].parsedData).isEqualTo("DISCONNECT_REQUEST")
        assertThat(logs[0].isValid).isTrue()
    }

    @Test
    fun shouldDiscoverCharacteristicsByPropertiesWhenUuidsDoNotMatchStandardTuya() = runTest {
        val customServiceUuid = java.util.UUID.fromString("00001234-0000-1000-8000-00805f9b34fb")
        val customWriteUuid = java.util.UUID.fromString("00001235-0000-1000-8000-00805f9b34fb")
        val customNotifyUuid = java.util.UUID.fromString("00001236-0000-1000-8000-00805f9b34fb")

        val writeChar = object : android.bluetooth.BluetoothGattCharacteristic(
            customWriteUuid,
            PROPERTY_WRITE,
            PERMISSION_WRITE
        ) {
            override fun getUuid(): java.util.UUID = customWriteUuid
            override fun getProperties(): Int = PROPERTY_WRITE
        }
        val notifyChar = object : android.bluetooth.BluetoothGattCharacteristic(
            customNotifyUuid,
            PROPERTY_NOTIFY,
            PERMISSION_READ
        ) {
            override fun getUuid(): java.util.UUID = customNotifyUuid
            override fun getProperties(): Int = PROPERTY_NOTIFY
        }

        val service = object : android.bluetooth.BluetoothGattService(
            customServiceUuid,
            SERVICE_TYPE_PRIMARY
        ) {
            override fun getUuid(): java.util.UUID = customServiceUuid

            override fun getCharacteristics(): List<android.bluetooth.BluetoothGattCharacteristic> =
                listOf(writeChar, notifyChar)

            override fun getCharacteristic(uuid: java.util.UUID?): android.bluetooth.BluetoothGattCharacteristic? =
                when (uuid) {
                    customWriteUuid -> writeChar
                    customNotifyUuid -> notifyChar
                    else -> null
                }
        }

        val (_, discoveredWrite, discoveredNotify) = adapter.discoverCharacteristicsFromServices(listOf(service))

        assertThat(discoveredWrite).isNotNull
        assertThat(discoveredWrite?.uuid).isEqualTo(customWriteUuid)
        assertThat(discoveredNotify).isNotNull
        assertThat(discoveredNotify?.uuid).isEqualTo(customNotifyUuid)
    }
}
