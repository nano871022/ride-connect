package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import javax.inject.Inject

class EvConfigUseCase @Inject constructor(
    private val evConfigPort: EvConfigPort,
    private val llmConfigPort: LlmConfigPort,
    private val llmClientPort: LlmClientPort,
    private val sessionStatePort: SessionStatePort,
    private val getEvConfigUseCase: GetEvConfigUseCase,
    private val saveEvConfigUseCase: SaveEvConfigUseCase,
    private val getActiveLlmConfigsUseCase: GetActiveLlmConfigsUseCase,
    private val fetchEvInfoUseCase: FetchEvInfoUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val saveActiveSessionUseCase: SaveActiveSessionUseCase,
    private val clearActiveSessionUseCase: ClearActiveSessionUseCase
) {
    suspend fun getEvConfig(): EvConfig? {
        return getEvConfigUseCase.execute()
    }

    suspend fun saveEvConfig(config: EvConfig): Long {
        return saveEvConfigUseCase.execute(config)
    }

    suspend fun getActiveLlmConfigs(): List<LlmConfig> {
        return getActiveLlmConfigsUseCase.execute()
    }

    suspend fun fetchEvInfo(
        prompt: String,
        llmConfig: LlmConfig,
        currentConfig: EvConfig,
        promptTemplate: String? = null
    ): EvConfig {
        return fetchEvInfoUseCase.execute(prompt, llmConfig, currentConfig, promptTemplate)
    }

    suspend fun getActiveSession(): ActiveSession? {
        return getActiveSessionUseCase.execute()
    }

    suspend fun saveActiveSession(session: ActiveSession) {
        saveActiveSessionUseCase.execute(session)
    }

    suspend fun clearActiveSession() {
        clearActiveSessionUseCase.execute()
    }
}
