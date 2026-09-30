package co.japl.android.ev_ride_connect.interfaces.model

data class EvConfig(
    val id: Long = 0,
    val brand: String = "",
    val version: String = "",
    val manufactoryYear: String = "",
    val manufactoryCompany: String = "",
    val batteryTechnology: String = "",
    val batteryVolts: String = "",
    val batteryAmpers: String = "",
    val brakeQuantity: Int = 0,
    val brakeTechnology: String = "",
    val suspensionTechnology: String = "",
    val chargePower: String = "",
    val otherCharacteristics: String = "",
    val motors: List<MotorSpec> = emptyList(),
    val request: String = "",
    val batteryMode: BatteryMode = BatteryMode.PERCENTAGE,
    val maxVoltage: Double = 54.6,
    val minVoltage: Double = 39.0
)
