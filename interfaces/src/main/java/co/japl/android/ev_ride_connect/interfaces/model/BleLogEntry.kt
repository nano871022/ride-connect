package co.japl.android.ev_ride_connect.interfaces.model

data class BleLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String = "",
    val isError: Boolean = false,
    val direction: BleLogDirection = BleLogDirection.SENT,
    val rawBytesHex: String = "",
    val parsedData: String = "",
    val isValid: Boolean = true,
    val errorMessage: String? = null
)
