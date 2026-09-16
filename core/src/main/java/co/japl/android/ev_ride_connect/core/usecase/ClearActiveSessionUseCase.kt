package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import javax.inject.Inject

class ClearActiveSessionUseCase @Inject constructor(
    private val sessionStatePort: SessionStatePort
) {
    suspend fun execute() = sessionStatePort.clearActiveSession()
}
