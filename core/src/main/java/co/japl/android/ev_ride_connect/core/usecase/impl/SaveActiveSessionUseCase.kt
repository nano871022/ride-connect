package co.japl.android.ev_ride_connect.core.usecase.impl

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import javax.inject.Inject

class SaveActiveSessionUseCase @Inject constructor(
    private val sessionStatePort: SessionStatePort
) {
    suspend fun execute(session: ActiveSession) = sessionStatePort.saveActiveSession(session)
}

