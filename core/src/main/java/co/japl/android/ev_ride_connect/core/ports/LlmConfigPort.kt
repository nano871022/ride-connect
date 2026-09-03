package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.LlmConfig

interface LlmConfigPort {
    suspend fun getAllConfigs(): List<LlmConfig>
    suspend fun getActiveConfigs(): List<LlmConfig>
    suspend fun saveConfig(config: LlmConfig): Long
    suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean
    suspend fun deleteConfig(id: Long): Boolean
}
