package co.japl.android.ev_ride_connect.interfaces.model

data class Trip(
    val id: Long = 0,
    val distance: Double = 0.0,
    val timeTrip: Long = 0,
    val maxSpeed: Double = 0.0,
    val avgSpeed: Double = 0.0,
    val averageSpeed: Double = avgSpeed,
    val initialBattery: Short = 0,
    val endBattery: Short = 0,
    val batteryConsumed: Int = 0,
    val initialOdometer: Long = 0,
    val endOdometer: Long = 0,
    val co2SavedGrams: Double = 0.0,
    val estimatedConsumptionWh: Double = 0.0,
    val chargeCountDetected: Int = 0,
    val createTmst: Long = System.currentTimeMillis()
)
