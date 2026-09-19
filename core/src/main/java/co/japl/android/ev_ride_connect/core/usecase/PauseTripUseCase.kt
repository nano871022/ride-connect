package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import javax.inject.Inject

class PauseTripUseCase @Inject constructor(
    private val sessionStatePort: SessionStatePort
) {
    suspend fun execute() {
        val existing = sessionStatePort.getActiveSession() ?: ActiveSession()
        if (existing.isRideActive && !existing.isPaused) {
            val updated = existing.copy(
                isPaused = true,
                lastUpdatedTmst = System.currentTimeMillis()
            )
            sessionStatePort.saveActiveSession(updated)
        }
    }
}
