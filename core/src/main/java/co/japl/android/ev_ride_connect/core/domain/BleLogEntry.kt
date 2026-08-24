package co.japl.android.ev_ride_connect.core.domain

enum class BleLogDirection {
    SENT,
    RECEIVED
}

data class BleLogEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val direction: BleLogDirection,
    val rawBytesHex: String,
    val parsedData: String,
    val isValid: Boolean,
    val errorMessage: String? = null
)
