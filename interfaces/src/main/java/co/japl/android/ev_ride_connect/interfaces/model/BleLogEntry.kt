package co.japl.android.ev_ride_connect.interfaces.model

data class BleLogEntry(
    val timestamp: Long,
    val message: String,
    val isError: Boolean = false
)
