package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveSessionUseCase @Inject constructor(
    private val sessionStatePort: SessionStatePort
) {
    fun execute(): Flow<ActiveSession?> = sessionStatePort.observeActiveSession()
}
