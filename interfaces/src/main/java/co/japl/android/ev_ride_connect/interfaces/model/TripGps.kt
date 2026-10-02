package co.japl.android.ev_ride_connect.interfaces.model

data class TripGps(
    val id: Long = 0,
    val tripId: Long = 0,
    val orderIndex: Int = 0,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val speed: Double = 0.0,
    val distance: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val createTmst: Long = timestamp,
    val motionState: MotionState = MotionState.STOPPED
)
