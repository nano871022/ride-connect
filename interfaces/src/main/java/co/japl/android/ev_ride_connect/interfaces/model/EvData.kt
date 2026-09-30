package co.japl.android.ev_ride_connect.interfaces.model

data class EvData(
    val id: Long = 0,
    val evCode: String = "",
    val km: Long = 0,
    val batteryLevel: Short = 0,
    val createTmst: Long = 0
)
