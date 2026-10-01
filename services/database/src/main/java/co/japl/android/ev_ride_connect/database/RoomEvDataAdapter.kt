package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import co.japl.android.ev_ride_connect.database.dao.EvDataDao
import co.japl.android.ev_ride_connect.database.entities.EvDataEntity

class RoomEvDataAdapter(
    private val evDataDao: EvDataDao
) : EvDataPort {

    override suspend fun getLatestEvData(): EvData? {
        val entity = evDataDao.getLatestEvData() ?: return null
        return entity.toDomain()
    }

    override suspend fun saveEvData(evData: EvData): Long {
        val entity = evData.toEntity()
        return evDataDao.insertEvData(entity)
    }

    override suspend fun getAllEvData(): List<EvData> {
        return evDataDao.getAllEvData().map { it.toDomain() }
    }

    override suspend fun updateOdometer(evCode: String, newKm: Long, currentBatteryPercentage: Short): Long {
        val entity = EvDataEntity(
            evCode = evCode,
            km = newKm,
            batteryLevel = currentBatteryPercentage,
            createTmst = System.currentTimeMillis()
        )
        return evDataDao.insertEvData(entity)
    }

    private fun EvDataEntity.toDomain(): EvData {
        return EvData(
            id = id,
            evCode = evCode,
            km = km,
            batteryLevel = batteryLevel,
            createTmst = createTmst
        )
    }

    private fun EvData.toEntity(): EvDataEntity {
        return EvDataEntity(
            id = id,
            evCode = evCode,
            km = km,
            batteryLevel = batteryLevel,
            createTmst = createTmst
        )
    }
}
