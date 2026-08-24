package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.database.dao.LlmConfigDao
import co.japl.android.ev_ride_connect.database.entities.LlmConfigEntity
import co.japl.android.ev_ride_connect.utils.DateUtils
import java.util.Date

class RoomLlmConfigAdapter(
    private val llmConfigDao: LlmConfigDao
) : LlmConfigPort {

    override suspend fun getAllConfigs(): List<LlmConfig> {
        return llmConfigDao.getAllConfigs().map { it.toDomain() }
    }

    override suspend fun getActiveConfigs(): List<LlmConfig> {
        return llmConfigDao.getActiveConfigs().map { it.toDomain() }
    }

    override suspend fun saveConfig(config: LlmConfig): Long {
        val nowFormatted = DateUtils.formatTimestamp(System.currentTimeMillis())
        val createdAt = if (config.createdAt.isBlank()) nowFormatted else config.createdAt
        val updatedAt = nowFormatted

        val entity = LlmConfigEntity(
            id = config.id,
            modelName = config.modelName,
            selectedVersion = config.selectedVersion,
            apiKey = config.apiKey,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isActive = config.isActive
        )
        return llmConfigDao.insertConfig(entity)
    }

    override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean {
        val nowFormatted = DateUtils.formatTimestamp(System.currentTimeMillis())
        val updatedRows = llmConfigDao.updateActiveStatus(id, isActive, nowFormatted)
        return updatedRows > 0
    }

    private fun LlmConfigEntity.toDomain(): LlmConfig {
        return LlmConfig(
            id = id,
            modelName = modelName,
            selectedVersion = selectedVersion,
            apiKey = apiKey,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isActive = isActive
        )
    }
}
