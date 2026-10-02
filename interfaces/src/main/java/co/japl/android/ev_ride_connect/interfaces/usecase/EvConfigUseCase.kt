package co.japl.android.ev_ride_connect.interfaces.usecase

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig

interface EvConfigUseCase {
    suspend fun getEvConfig(): EvConfig?
    suspend fun saveEvConfig(config: EvConfig): Long
    suspend fun getActiveLlmConfigs(): List<LlmConfig>
    suspend fun fetchEvInfo(
        prompt: String,
        llmConfig: LlmConfig,
        currentConfig: EvConfig,
        promptTemplate: String? = null
    ): EvConfig
    suspend fun getActiveSession(): ActiveSession?
    suspend fun saveActiveSession(session: ActiveSession)
    suspend fun clearActiveSession()
}
