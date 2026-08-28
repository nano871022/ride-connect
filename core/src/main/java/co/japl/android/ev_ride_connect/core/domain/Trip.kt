package co.japl.android.ev_ride_connect.core.domain

data class Trip(
    val id: Long = 0,
    val timeTrip: Long = 0,
    val averageSpeed: Double = 0.0,
    val distance: Double = 0.0,
    val createTmst: Long = System.currentTimeMillis()
)
