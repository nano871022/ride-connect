package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface MainScaffoldUseCase {
    fun observeActiveSession(): Flow<ActiveSession?>
}

class MainScaffoldUseCaseImpl @Inject constructor(
    private val sessionStatePort: SessionStatePort,
    private val observeActiveSessionUseCase: ObserveActiveSessionUseCase
) : MainScaffoldUseCase {
    override fun observeActiveSession(): Flow<ActiveSession?> {
        return observeActiveSessionUseCase.execute()
    }
}
