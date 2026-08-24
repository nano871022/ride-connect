package co.japl.android.ev_ride_connect.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import co.japl.android.ev_ride_connect.core.domain.ScooterState
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.utils.BatteryCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class TuyaBleAdapter(
    private val context: Context? = null
) : BleScooterPort {

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

    private val _isConnected = MutableStateFlow(false)

    private var onSendCommandListener: ((Int, Any) -> Unit)? = null

    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        context?.let {
            val manager = it.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            manager?.adapter
        }
    }

    override fun observeScooterState(): Flow<ScooterState> {
        return _scooterState.asStateFlow()
    }

    override fun observeConnectionState(): Flow<Boolean> {
        return _isConnected.asStateFlow()
    }

    override fun sendCommand(dpId: Int, value: Any) {
        onSendCommandListener?.invoke(dpId, value)
        writeDpCommand(dpId, value)
    }

    fun setOnSendCommandListener(listener: (Int, Any) -> Unit) {
        this.onSendCommandListener = listener
    }

    @SuppressLint("MissingPermission")
    override fun connect(macAddress: String?) {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) return

        if (!macAddress.isNullOrBlank()) {
            val device = adapter.getRemoteDevice(macAddress)
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        } else {
            startScanAndConnect()
        }
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        writeCharacteristic = null
        _isConnected.value = false
    }

    @SuppressLint("MissingPermission")
    private fun startScanAndConnect() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(TuyaBleProtocol.TUYA_SERVICE_UUID))
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                val device = result?.device ?: return
                scanner.stopScan(this)
                bluetoothGatt = device.connectGatt(context, false, gattCallback)
            }

            override fun onScanFailed(errorCode: Int) {
                _isConnected.value = false
            }
        }

        try {
            scanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
        } catch (e: Exception) {
            _isConnected.value = false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _isConnected.value = true
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _isConnected.value = false
                writeCharacteristic = null
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return

            var service = gatt.getService(TuyaBleProtocol.TUYA_SERVICE_UUID)
            if (service == null) {
                service = gatt.getService(TuyaBleProtocol.ALT_SERVICE_UUID)
            }

            if (service == null) return

            val writeChar = service.getCharacteristic(TuyaBleProtocol.TUYA_WRITE_CHARACTERISTIC_UUID)
                ?: service.getCharacteristic(TuyaBleProtocol.ALT_CHARACTERISTIC_UUID)
            val notifyChar = service.getCharacteristic(TuyaBleProtocol.TUYA_NOTIFY_CHARACTERISTIC_UUID)
                ?: service.getCharacteristic(TuyaBleProtocol.ALT_CHARACTERISTIC_UUID)

            this@TuyaBleAdapter.writeCharacteristic = writeChar

            if (notifyChar != null) {
                gatt.setCharacteristicNotification(notifyChar, true)
                val descriptor = notifyChar.getDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                )
                if (descriptor != null) {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }
        }

        @Deprecated("Deprecated in Java / Android API")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = characteristic.value ?: return
            handleReceivedPayload(value)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleReceivedPayload(value)
        }
    }

    private fun handleReceivedPayload(payload: ByteArray) {
        val decodedDps = TuyaBleProtocol.decodeDpFrame(payload)
        if (decodedDps.isNotEmpty()) {
            onDataPointsReceived(decodedDps)
        }
    }

    @SuppressLint("MissingPermission")
    private fun writeDpCommand(dpId: Int, value: Any) {
        val gatt = bluetoothGatt ?: return
        val characteristic = writeCharacteristic ?: return

        val encodedPacket = TuyaBleProtocol.encodeDpCommand(dpId, value)
        characteristic.value = encodedPacket
        gatt.writeCharacteristic(characteristic)
    }

    fun onDataPointReceived(dpId: Int, value: Any) {
        _scooterState.update { currentState ->
            updateStateWithDp(currentState, dpId, value)
        }
    }

    fun onDataPointsReceived(dps: Map<Int, Any>) {
        _scooterState.update { currentState ->
            var updatedState = currentState
            dps.forEach { (dpId, value) ->
                updatedState = updateStateWithDp(updatedState, dpId, value)
            }
            updatedState
        }
    }

    private fun updateStateWithDp(currentState: ScooterState, dpId: Int, value: Any): ScooterState {
        return when (dpId) {
            1 -> currentState.copy(isLocked = value as? Boolean ?: currentState.isLocked)
            2 -> currentState.copy(speedMode = (value as? Number)?.toInt() ?: currentState.speedMode)
            4 -> currentState.copy(isLightOn = value as? Boolean ?: currentState.isLightOn)
            5 -> currentState.copy(currentSpeed = (value as? Number)?.toInt() ?: currentState.currentSpeed)
            6 -> currentState.copy(totalOdometer = (value as? Number)?.toInt() ?: currentState.totalOdometer)
            7 -> {
                val voltage = (value as? Number)?.toInt() ?: currentState.realtimeVoltage
                val batteryPercentage = BatteryCalculator.calculate13SPercentage(voltage)
                currentState.copy(
                    realtimeVoltage = voltage,
                    batteryPercentage = batteryPercentage
                )
            }
            else -> currentState
        }
    }
}
