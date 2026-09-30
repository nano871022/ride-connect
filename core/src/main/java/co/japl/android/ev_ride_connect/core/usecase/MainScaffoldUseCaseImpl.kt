package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import co.japl.android.ev_ride_connect.interfaces.usecase.MainScaffoldUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainScaffoldUseCaseImpl @Inject constructor(
    private val sessionStatePort: SessionStatePort,
    private val observeActiveSessionUseCase: ObserveActiveSessionUseCase
) : MainScaffoldUseCase {
    override fun observeActiveSession(): Flow<ActiveSession?> {
        return observeActiveSessionUseCase.execute()
    }
}
