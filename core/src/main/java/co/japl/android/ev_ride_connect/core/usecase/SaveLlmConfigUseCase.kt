package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import javax.inject.Inject

class SaveLlmConfigUseCase @Inject constructor(
    private val llmConfigPort: LlmConfigPort
) {
    suspend fun execute(config: LlmConfig): Long = llmConfigPort.saveConfig(config)
}
