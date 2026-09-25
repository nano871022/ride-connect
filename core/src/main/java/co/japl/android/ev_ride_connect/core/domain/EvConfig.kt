package co.japl.android.ev_ride_connect.core.domain

data class EvConfig(
    val id: Long = 0,
    val request: String = "",
    val brand: String = "",
    val version: String = "",
    val motors: List<MotorSpec> = emptyList(),
    val manufactoryYear: String = "",
    val manufactoryCompany: String = "",
    val boughtDate: String = "",
    val batteryTechnology: String = "",
    val batteryVolts: String = "",
    val batteryAmpers: String = "",
    val brakeQuantity: Int = 0,
    val brakeTechnology: String = "",
    val suspensionTechnology: String = "",
    val chargePower: String = "",
    val otherCharacteristics: String = "",
    val imageUrl: String = "",
    val isLoaded: Boolean = false,
    val batteryMode: BatteryMode = BatteryMode.PERCENTAGE,
    val maxVoltage: Double = 54.6,
    val minVoltage: Double = 39.0
)
