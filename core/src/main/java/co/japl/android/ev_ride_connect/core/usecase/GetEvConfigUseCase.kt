package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import javax.inject.Inject

class GetEvConfigUseCase @Inject constructor(
    private val evConfigPort: EvConfigPort
) {
    suspend fun execute(): EvConfig? = evConfigPort.getEvConfig()
}
