package co.japl.android.ev_ride_connect.core.domain

object EvConstants {
    val EV_CONFIG_CLASS_NAME: String = EvConfig::class.java.name
    const val EV_LLM_PROMPT_TEMPLATE: String = """
        Identify specs for EV query: "%s".
        Respond with JSON containing keys:
        brand, version, motors (array of objects with name and watts), manufactoryYear,
        manufactoryCompany, batteryTechnology, batteryVolts, batteryAmpers, brakeQuantity,
        brakeTechnology, suspensionTechnology, chargePower, otherCharacteristics.
    """
}
