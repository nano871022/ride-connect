package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import javax.inject.Inject

class DeleteLlmConfigUseCase @Inject constructor(
    private val llmConfigPort: LlmConfigPort
) {
    suspend fun execute(id: Long) = llmConfigPort.deleteConfig(id)
}
