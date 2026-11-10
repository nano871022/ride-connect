package co.japl.android.ev_ride_connect.core.usecase.impl

import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import javax.inject.Inject

class ToggleLlmConfigStatusUseCase @Inject constructor(
    private val llmConfigPort: LlmConfigPort
) {
    suspend fun execute(id: Long, isActive: Boolean) = llmConfigPort.toggleActiveStatus(id, isActive)
}

