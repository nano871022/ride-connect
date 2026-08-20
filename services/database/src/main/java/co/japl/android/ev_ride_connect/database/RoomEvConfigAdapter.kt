package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.MotorSpec
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.database.dao.EvConfigDao
import co.japl.android.ev_ride_connect.database.entities.EvConfigEntity

class RoomEvConfigAdapter(
    private val evConfigDao: EvConfigDao
) : EvConfigPort {

    override suspend fun getEvConfig(): EvConfig? {
        val entity = evConfigDao.getLatestEvConfig() ?: return null
        return entity.toDomain()
    }

    override suspend fun saveEvConfig(config: EvConfig): Long {
        val entity = config.toEntity()
        return evConfigDao.insertEvConfig(entity)
    }

    private fun EvConfigEntity.toDomain(): EvConfig {
        return EvConfig(
            id = id,
            request = request,
            brand = brand,
            version = version,
            motors = parseMotorsJson(motorsJson),
            manufactoryYear = manufactoryYear,
            manufactoryCompany = manufactoryCompany,
            boughtDate = boughtDate,
            batteryTechnology = batteryTechnology,
            batteryVolts = batteryVolts,
            batteryAmpers = batteryAmpers,
            brakeQuantity = brakeQuantity,
            brakeTechnology = brakeTechnology,
            suspensionTechnology = suspensionTechnology,
            chargePower = chargePower,
            otherCharacteristics = otherCharacteristics,
            isLoaded = isLoaded
        )
    }

    private fun EvConfig.toEntity(): EvConfigEntity {
        return EvConfigEntity(
            id = id,
            request = request,
            brand = brand,
            version = version,
            motorsJson = serializeMotors(motors),
            manufactoryYear = manufactoryYear,
            manufactoryCompany = manufactoryCompany,
            boughtDate = boughtDate,
            batteryTechnology = batteryTechnology,
            batteryVolts = batteryVolts,
            batteryAmpers = batteryAmpers,
            brakeQuantity = brakeQuantity,
            brakeTechnology = brakeTechnology,
            suspensionTechnology = suspensionTechnology,
            chargePower = chargePower,
            otherCharacteristics = otherCharacteristics,
            isLoaded = isLoaded
        )
    }

    private fun parseMotorsJson(jsonStr: String): List<MotorSpec> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<MotorSpec>()
        try {
            val items = jsonStr.split(";")
            for (item in items) {
                if (item.isBlank()) continue
                val parts = item.split(":")
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val watts = parts[1].trim().toIntOrNull() ?: 0
                    list.add(MotorSpec(name = name, watts = watts))
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return list
    }

    private fun serializeMotors(motors: List<MotorSpec>): String {
        return motors.joinToString(";") { "${it.name}:${it.watts}" }
    }
}
