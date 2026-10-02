package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import javax.inject.Inject

class EndTripUseCase @Inject constructor(
    private val sessionStatePort: SessionStatePort
) {
    suspend fun execute() {
        val existing = sessionStatePort.getActiveSession()
        if (existing != null) {
            val updated = existing.copy(
                isRideActive = false,
                isPaused = false,
                startTimeMs = System.currentTimeMillis()
            )
            sessionStatePort.saveActiveSession(updated)

            sessionStatePort.clearActiveSession()
        }
    }
}
