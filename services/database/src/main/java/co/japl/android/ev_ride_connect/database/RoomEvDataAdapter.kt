package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import co.japl.android.ev_ride_connect.database.dao.EvDataDao
import co.japl.android.ev_ride_connect.database.entities.EvDataEntity

class RoomEvDataAdapter(
    private val evDataDao: EvDataDao
) : EvDataPort {

    override suspend fun getLatestEvData(): EvData? {
        return evDataDao.getLatestEvData()?.toDomain()
    }

    override suspend fun getAllEvData(): List<EvData> {
        return evDataDao.getAllEvData().map { it.toDomain() }
    }

    override suspend fun saveEvData(evData: EvData): Long {
        return evDataDao.insertEvData(evData.toEntity())
    }

    private fun EvDataEntity.toDomain() = EvData(
        evCode = evCode,
        km = km,
        batteryLevel = batteryLevel,
        createTmst = createTmst
    )

    private fun EvData.toEntity() = EvDataEntity(
        evCode = evCode,
        km = km,
        batteryLevel = batteryLevel,
        createTmst = createTmst
    )
}
