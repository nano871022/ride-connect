package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveSessionUseCase @Inject constructor(
    private val sessionStatePort: SessionStatePort
) {
    fun execute(): Flow<ActiveSession?> = sessionStatePort.observeActiveSession()
}
