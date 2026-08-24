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
import android.util.Log
import co.japl.android.ev_ride_connect.core.domain.BleLogDirection
import co.japl.android.ev_ride_connect.core.domain.BleLogEntry
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

    companion object {
        private const val TAG = "TuyaBleAdapter"
    }

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
    private val _rawLogs = MutableStateFlow<List<BleLogEntry>>(emptyList())

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

    override fun observeRawLogs(): Flow<List<BleLogEntry>> {
        return _rawLogs.asStateFlow()
    }

    override fun clearLogs() {
        _rawLogs.value = emptyList()
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
        logMessage("Initiating connection attempt (macAddress=$macAddress)")
        val adapter = bluetoothAdapter
        if (adapter == null) {
            logError("Bluetooth adapter is null")
            addLogEntry(
                direction = BleLogDirection.SENT,
                rawBytesHex = "",
                parsedData = "CONNECT_ATTEMPT",
                isValid = false,
                errorMessage = "Bluetooth adapter is null or Context is missing"
            )
            return
        }
        if (!adapter.isEnabled) {
            logError("Bluetooth is disabled")
            addLogEntry(
                direction = BleLogDirection.SENT,
                rawBytesHex = "",
                parsedData = "CONNECT_ATTEMPT",
                isValid = false,
                errorMessage = "Bluetooth adapter is disabled"
            )
            return
        }

        // Close previous GATT instance if present to avoid status 133 resource leaks
        bluetoothGatt?.close()
        bluetoothGatt = null

        if (!macAddress.isNullOrBlank()) {
            try {
                logMessage("Connecting directly to MAC: $macAddress")
                val device = adapter.getRemoteDevice(macAddress)
                addLogEntry(
                    direction = BleLogDirection.SENT,
                    rawBytesHex = "",
                    parsedData = "CONNECTING_TO_MAC: $macAddress",
                    isValid = true
                )
                bluetoothGatt = device.connectGatt(
                    context,
                    false,
                    gattCallback,
                    android.bluetooth.BluetoothDevice.TRANSPORT_LE
                )
            } catch (e: Exception) {
                logError("Failed to connect to MAC $macAddress: ${e.message}", e)
                addLogEntry(
                    direction = BleLogDirection.SENT,
                    rawBytesHex = "",
                    parsedData = "CONNECT_ERROR: $macAddress",
                    isValid = false,
                    errorMessage = "Failed to connect to device $macAddress: ${e.message}"
                )
            }
        } else {
            startScanAndConnect()
        }
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        logMessage("Disconnect requested")
        addLogEntry(
            direction = BleLogDirection.SENT,
            rawBytesHex = "",
            parsedData = "DISCONNECT_REQUEST",
            isValid = true
        )
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        writeCharacteristic = null
        _isConnected.value = false
    }

    @SuppressLint("MissingPermission")
    private fun startScanAndConnect() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            logError("Bluetooth LE scanner is null")
            addLogEntry(
                direction = BleLogDirection.SENT,
                rawBytesHex = "",
                parsedData = "SCAN_START",
                isValid = false,
                errorMessage = "Bluetooth LE Scanner is null"
            )
            return
        }

        logMessage("Starting BLE scan for Tuya service: ${TuyaBleProtocol.TUYA_SERVICE_UUID}")
        addLogEntry(
            direction = BleLogDirection.SENT,
            rawBytesHex = "",
            parsedData = "START_BLE_SCAN (UUID: ${TuyaBleProtocol.TUYA_SERVICE_UUID})",
            isValid = true
        )

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(TuyaBleProtocol.TUYA_SERVICE_UUID))
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                val device = result?.device ?: return
                logMessage("Device found during scan: ${device.address} (${device.name})")
                addLogEntry(
                    direction = BleLogDirection.RECEIVED,
                    rawBytesHex = "",
                    parsedData = "DEVICE_DISCOVERED: ${device.address} (${device.name ?: "Unknown"})",
                    isValid = true
                )
                scanner.stopScan(this)
                bluetoothGatt?.close()
                bluetoothGatt = device.connectGatt(
                    context,
                    false,
                    gattCallback,
                    android.bluetooth.BluetoothDevice.TRANSPORT_LE
                )
            }

            override fun onScanFailed(errorCode: Int) {
                logError("BLE scan failed with error code: $errorCode")
                _isConnected.value = false
                addLogEntry(
                    direction = BleLogDirection.RECEIVED,
                    rawBytesHex = "",
                    parsedData = "SCAN_FAILED",
                    isValid = false,
                    errorMessage = "BLE scan failed with error code: $errorCode"
                )
            }
        }

        try {
            scanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
        } catch (e: Exception) {
            logError("Exception starting BLE scan: ${e.message}", e)
            _isConnected.value = false
            addLogEntry(
                direction = BleLogDirection.SENT,
                rawBytesHex = "",
                parsedData = "SCAN_START_ERROR",
                isValid = false,
                errorMessage = "Failed to start BLE scan: ${e.message}"
            )
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            logMessage("onConnectionStateChange: status=$status, newState=$newState")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                logError("GATT connection state change failed with status $status")
                _isConnected.value = false
                addLogEntry(
                    direction = BleLogDirection.RECEIVED,
                    rawBytesHex = "",
                    parsedData = "GATT_STATE_CHANGE_ERROR (Status: $status, State: $newState)",
                    isValid = false,
                    errorMessage = "GATT operation failed with status $status"
                )
                gatt.close()
                if (bluetoothGatt == gatt) {
                    bluetoothGatt = null
                }
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                logMessage("GATT Connected to ${gatt.device?.address}. Discovering services...")
                _isConnected.value = true
                addLogEntry(
                    direction = BleLogDirection.RECEIVED,
                    rawBytesHex = "",
                    parsedData = "GATT_CONNECTED: ${gatt.device?.address}",
                    isValid = true
                )
                val started = gatt.discoverServices()
                if (!started) {
                    logError("Failed to initiate service discovery")
                    addLogEntry(
                        direction = BleLogDirection.SENT,
                        rawBytesHex = "",
                        parsedData = "DISCOVER_SERVICES_INIT_FAILED",
                        isValid = false,
                        errorMessage = "gatt.discoverServices() returned false"
                    )
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                logMessage("GATT Disconnected from ${gatt.device?.address}")
                _isConnected.value = false
                writeCharacteristic = null
                addLogEntry(
                    direction = BleLogDirection.RECEIVED,
                    rawBytesHex = "",
                    parsedData = "GATT_DISCONNECTED: ${gatt.device?.address}",
                    isValid = true
                )
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            logMessage("onServicesDiscovered: status=$status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                logError("Service discovery failed with status $status")
                addLogEntry(
                    direction = BleLogDirection.RECEIVED,
                    rawBytesHex = "",
                    parsedData = "SERVICES_DISCOVERY_FAILED (Status: $status)",
                    isValid = false,
                    errorMessage = "Service discovery failed with status $status"
                )
                return
            }

            var service = gatt.getService(TuyaBleProtocol.TUYA_SERVICE_UUID)
            if (service == null) {
                logMessage("Primary Tuya service not found, trying alt service...")
                service = gatt.getService(TuyaBleProtocol.ALT_SERVICE_UUID)
            }

            if (service == null) {
                logError("Neither primary nor alt Tuya service found on device")
                addLogEntry(
                    direction = BleLogDirection.RECEIVED,
                    rawBytesHex = "",
                    parsedData = "SERVICES_DISCOVERED",
                    isValid = false,
                    errorMessage = "No compatible Tuya service found on device (Checked: ${TuyaBleProtocol.TUYA_SERVICE_UUID}, ${TuyaBleProtocol.ALT_SERVICE_UUID})"
                )
                return
            }

            val writeChar = service.getCharacteristic(TuyaBleProtocol.TUYA_WRITE_CHARACTERISTIC_UUID)
                ?: service.getCharacteristic(TuyaBleProtocol.ALT_CHARACTERISTIC_UUID)
            val notifyChar = service.getCharacteristic(TuyaBleProtocol.TUYA_NOTIFY_CHARACTERISTIC_UUID)
                ?: service.getCharacteristic(TuyaBleProtocol.ALT_CHARACTERISTIC_UUID)

            this@TuyaBleAdapter.writeCharacteristic = writeChar

            if (writeChar == null) {
                logError("Write characteristic not found in service")
                addLogEntry(
                    direction = BleLogDirection.RECEIVED,
                    rawBytesHex = "",
                    parsedData = "CHARACTERISTICS_DISCOVERED",
                    isValid = false,
                    errorMessage = "Write characteristic missing from Tuya service"
                )
            }

            if (notifyChar != null) {
                logMessage("Enabling notifications on characteristic ${notifyChar.uuid}")
                val notificationSet = gatt.setCharacteristicNotification(notifyChar, true)
                if (!notificationSet) {
                    logError("Failed to set characteristic notification locally")
                }
                val descriptor = notifyChar.getDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                )
                if (descriptor != null) {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    val descWritten = gatt.writeDescriptor(descriptor)
                    logMessage("Writing descriptor for notification: result=$descWritten")
                } else {
                    logError("Notification descriptor (0x2902) not found on notify characteristic")
                }
            } else {
                logError("Notify characteristic not found in service")
                addLogEntry(
                    direction = BleLogDirection.RECEIVED,
                    rawBytesHex = "",
                    parsedData = "CHARACTERISTICS_DISCOVERED",
                    isValid = false,
                    errorMessage = "Notify characteristic missing from Tuya service"
                )
            }

            addLogEntry(
                direction = BleLogDirection.RECEIVED,
                rawBytesHex = "",
                parsedData = "SERVICES_CONFIGURED (Write: ${writeChar?.uuid != null}, Notify: ${notifyChar?.uuid != null})",
                isValid = writeChar != null && notifyChar != null,
                errorMessage = if (writeChar == null || notifyChar == null) "Missing write or notify characteristic" else null
            )
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
        val hexString = payload.joinToString(" ") { "%02X".format(it) }
        val isValid = decodedDps.isNotEmpty()

        addLogEntry(
            direction = BleLogDirection.RECEIVED,
            rawBytesHex = hexString,
            parsedData = if (isValid) decodedDps.toString() else "INVALID_PAYLOAD_OR_CHECKSUM",
            isValid = isValid,
            errorMessage = if (!isValid) "Header, checksum, or frame parsing failed" else null
        )

        if (isValid) {
            onDataPointsReceived(decodedDps)
        }
    }

    @SuppressLint("MissingPermission")
    private fun writeDpCommand(dpId: Int, value: Any) {
        val encodedPacket = TuyaBleProtocol.encodeDpCommand(dpId, value)
        val hexString = encodedPacket.joinToString(" ") { "%02X".format(it) }

        val gatt = bluetoothGatt
        val characteristic = writeCharacteristic

        val (isValid, errorMsg) = when {
            gatt == null -> false to "BluetoothGatt is null (Not connected)"
            characteristic == null -> false to "Write characteristic is null"
            else -> true to null
        }

        addLogEntry(
            direction = BleLogDirection.SENT,
            rawBytesHex = hexString,
            parsedData = "DP ID: $dpId, Value: $value",
            isValid = isValid,
            errorMessage = errorMsg
        )

        if (isValid && gatt != null && characteristic != null) {
            characteristic.value = encodedPacket
            gatt.writeCharacteristic(characteristic)
        }
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

    private fun addLogEntry(
        direction: BleLogDirection,
        rawBytesHex: String,
        parsedData: String,
        isValid: Boolean,
        errorMessage: String? = null
    ) {
        val logEntry = BleLogEntry(
            direction = direction,
            rawBytesHex = rawBytesHex,
            parsedData = parsedData,
            isValid = isValid,
            errorMessage = errorMessage
        )
        _rawLogs.update { it + logEntry }
    }

    private fun logMessage(msg: String) {
        try {
            Log.d(TAG, msg)
        } catch (_: Throwable) {
            // Log class might not be mocked in standard JVM unit test environments without Robolectric or try-catch
        }
    }

    private fun logError(msg: String, throwable: Throwable? = null) {
        try {
            if (throwable != null) {
                Log.e(TAG, msg, throwable)
            } else {
                Log.e(TAG, msg)
            }
        } catch (_: Throwable) {
            // Log class might not be mocked in standard JVM unit test environments without Robolectric or try-catch
        }
    }
}
