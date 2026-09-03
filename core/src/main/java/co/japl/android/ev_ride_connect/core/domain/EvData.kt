package co.japl.android.ev_ride_connect.core.domain

data class EvData(
    val evCode: String = "",
    val km: Long = 0L,
    val batteryLevel: Short = 0,
    val createTmst: Long = System.currentTimeMillis()
)
