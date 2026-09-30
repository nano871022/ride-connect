package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import javax.inject.Inject

class GetActiveSessionUseCase @Inject constructor(
    private val sessionStatePort: SessionStatePort
) {
    suspend fun execute(): ActiveSession? = sessionStatePort.getActiveSession()
}
