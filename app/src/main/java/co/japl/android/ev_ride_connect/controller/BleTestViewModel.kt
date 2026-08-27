package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.BleLogEntry
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BleTestViewModel @Inject constructor(
    private val bleScooterPort: BleScooterPort
) : ViewModel() {

    val isConnected: StateFlow<Boolean> = bleScooterPort.observeConnectionState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val logs: StateFlow<List<BleLogEntry>> = bleScooterPort.observeRawLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleConnection() {
        if (isConnected.value) {
            bleScooterPort.disconnect()
        } else {
            bleScooterPort.connect()
        }
    }

    fun sendLockCommand(lockState: Boolean) {
        bleScooterPort.sendCommand(1, lockState)
    }

    fun sendSpeedModeCommand(mode: Int) {
        bleScooterPort.sendCommand(2, mode)
    }

    fun sendLightCommand(lightState: Boolean) {
        bleScooterPort.sendCommand(4, lightState)
    }

    fun sendBatteryQueryCommand() {
        bleScooterPort.sendCommand(7, 0)
    }

    fun clearLogs() {
        bleScooterPort.clearLogs()
    }

    fun formatLogsForExport(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        val sb = StringBuilder()

        sb.appendLine("=== BLE COMMUNICATION TEST LOG ===")
        sb.appendLine("Exported At: ${dateFormat.format(Date())}")
        sb.appendLine("Connected Status: ${if (isConnected.value) "CONNECTED" else "DISCONNECTED"}")
        sb.appendLine("Total Entries: ${logs.value.size}")
        sb.appendLine("=================================")

        logs.value.forEachIndexed { index, entry ->
            val timestampStr = dateFormat.format(Date(entry.timestamp))
            val statusTag = if (entry.isValid) "VALID" else "INVALID"
            sb.appendLine("[$index] $timestampStr | ${entry.direction} | Status: $statusTag")
            sb.appendLine("  HEX Payload: ${entry.rawBytesHex}")
            sb.appendLine("  Parsed Data: ${entry.parsedData}")
            if (!entry.errorMessage.isNull_or_blank()) {
                sb.appendLine("  Error: ${entry.errorMessage}")
            }
            sb.appendLine("---------------------------------")
        }

        return sb.toString()
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
