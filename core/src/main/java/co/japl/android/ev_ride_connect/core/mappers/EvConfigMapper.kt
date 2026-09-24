package co.japl.android.ev_ride_connect.core.mappers

import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.MotorSpec
import org.json.JSONArray
import org.json.JSONObject

object EvConfigMapper {

    private fun parseDoubleVal(optVal: Any?, defaultVal: Double): Double {
        if (optVal == null) return defaultVal
        return when (optVal) {
            is Number -> optVal.toDouble()
            else -> optVal.toString().replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: defaultVal
        }
    }

    fun fromLlmResponse(userRequest: String, responseText: String): EvConfig {
        var cleanedText = responseText.trim()
        if (cleanedText.contains("```json")) {
            cleanedText = cleanedText.substringAfter("```json").substringBefore("```").trim()
        } else if (cleanedText.startsWith("```")) {
            cleanedText = cleanedText.substringAfter("```").substringBefore("```").trim()
        }

        try {
            val json = JSONObject(cleanedText)
            val brand = json.optString("brand", "")
            val version = json.optString("version", "")
            val manufactoryYear = json.opt("manufactoryYear")?.toString() ?: ""
            val manufactoryCompany = json.optString("manufactoryCompany", "")
            val batteryTechnology = json.optString("batteryTechnology", "")
            val batteryVolts = json.opt("batteryVolts")?.toString() ?: ""
            val batteryAmpers = json.opt("batteryAmpers")?.toString() ?: ""
            val brakeQuantity = json.optInt("brakeQuantity", 0)
            val brakeTechnology = json.optString("brakeTechnology", "")
            val suspensionTechnology = json.optString("suspensionTechnology", "")
            val chargePower = json.optString("chargePower", "")
            val imageUrl = json.optString("imageUrl", "")

            val maxVoltageOpt = if (json.has("maxVoltage")) json.opt("maxVoltage") else json.opt("max_voltage")
            val minVoltageOpt = if (json.has("minVoltage")) json.opt("minVoltage") else json.opt("min_voltage")

            val maxVoltage = parseDoubleVal(maxVoltageOpt, 54.6)
            val minVoltage = parseDoubleVal(minVoltageOpt, 39.0)

            val otherCharacteristics = when (val opt = json.opt("otherCharacteristics")) {
                is JSONArray -> (0 until opt.length()).map { opt.get(it).toString() }.joinToString(", ")
                null -> ""
                else -> opt.toString()
            }

            val motorsList = mutableListOf<MotorSpec>()
            val motorsArray = json.optJSONArray("motors")
            if (motorsArray != null) {
                for (i in 0 until motorsArray.length()) {
                    val mObj = motorsArray.optJSONObject(i)
                    if (mObj != null) {
                        val mName = mObj.optString("name", "Motor ${i + 1}")
                        val mWatts = mObj.optInt("watts", 0)
                        motorsList.add(MotorSpec(mName, mWatts))
                    }
                }
            }

            if (brand.isNotBlank() || version.isNotBlank() || motorsList.isNotEmpty()) {
                return EvConfig(
                    brand = brand,
                    version = version,
                    motors = motorsList,
                    manufactoryYear = manufactoryYear,
                    manufactoryCompany = manufactoryCompany,
                    batteryTechnology = batteryTechnology,
                    batteryVolts = batteryVolts,
                    batteryAmpers = batteryAmpers,
                    brakeQuantity = brakeQuantity,
                    brakeTechnology = brakeTechnology,
                    suspensionTechnology = suspensionTechnology,
                    chargePower = chargePower,
                    otherCharacteristics = otherCharacteristics,
                    imageUrl = imageUrl,
                    maxVoltage = maxVoltage,
                    minVoltage = minVoltage
                )
            }
        } catch (e: Exception) {
            // Fall through to regex
        }

        fun extractKey(key: String): String {
            val regex = Regex(""""$key"\s*:\s*"([^"]*)"""", RegexOption.IGNORE_CASE)
            return regex.find(responseText)?.groupValues?.get(1) ?: ""
        }

        fun extractIntKey(key: String): Int {
            val regex = Regex(""""$key"\s*:\s*(\d+)""", RegexOption.IGNORE_CASE)
            return regex.find(responseText)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        }

        fun extractDoubleKey(key: String, defaultVal: Double): Double {
            val regex = Regex(""""$key"\s*:\s*"?([0-9.]+)[Vv]?"?""", RegexOption.IGNORE_CASE)
            return regex.find(responseText)?.groupValues?.get(1)?.toDoubleOrNull() ?: defaultVal
        }

        val brand = extractKey("brand")
        val version = extractKey("version")
        val manufactoryYear = extractKey("manufactoryYear")
        val manufactoryCompany = extractKey("manufactoryCompany")
        val batteryTechnology = extractKey("batteryTechnology")
        val batteryVolts = extractKey("batteryVolts")
        val batteryAmpers = extractKey("batteryAmpers")
        val brakeQuantity = extractIntKey("brakeQuantity")
        val brakeTechnology = extractKey("brakeTechnology")
        val suspensionTechnology = extractKey("suspensionTechnology")
        val chargePower = extractKey("chargePower")
        val otherCharacteristics = extractKey("otherCharacteristics")
        val imageUrl = extractKey("imageUrl")

        val maxVoltage = extractDoubleKey("maxVoltage", extractDoubleKey("max_voltage", 54.6))
        val minVoltage = extractDoubleKey("minVoltage", extractDoubleKey("min_voltage", 39.0))

        val motorsList = mutableListOf<MotorSpec>()
        val motorRegex = Regex("""\{\s*"name"\s*:\s*"([^"]+)"\s*,\s*"watts"\s*:\s*(\d+)\s*\}""", RegexOption.IGNORE_CASE)
        motorRegex.findAll(responseText).forEach { match ->
            val mName = match.groupValues[1]
            val mWatts = match.groupValues[2].toIntOrNull() ?: 0
            motorsList.add(MotorSpec(mName, mWatts))
        }

        if (brand.isNotBlank() || version.isNotBlank() || motorsList.isNotEmpty()) {
            return EvConfig(
                brand = brand,
                version = version,
                motors = motorsList,
                manufactoryYear = manufactoryYear,
                manufactoryCompany = manufactoryCompany,
                batteryTechnology = batteryTechnology,
                batteryVolts = batteryVolts,
                batteryAmpers = batteryAmpers,
                brakeQuantity = brakeQuantity,
                brakeTechnology = brakeTechnology,
                suspensionTechnology = suspensionTechnology,
                chargePower = chargePower,
                otherCharacteristics = otherCharacteristics,
                imageUrl = imageUrl,
                maxVoltage = maxVoltage,
                minVoltage = minVoltage
            )
        }

        return EvConfig()
    }
}
