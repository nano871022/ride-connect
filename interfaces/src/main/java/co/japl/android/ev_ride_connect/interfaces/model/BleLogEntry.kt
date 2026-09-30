package interfaces/src/main/java/co/japl/android/ev_ride_connect/interfaces/model/BleLogEntry.kt
package co.japl.android.ev_ride_connect.interfaces.model

data class BleLogEntry(
    val timestamp: Long,
    val message: String,
    val isError: Boolean = false
)
