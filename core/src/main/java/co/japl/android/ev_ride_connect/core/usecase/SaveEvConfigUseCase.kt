package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import javax.inject.Inject

class SaveEvConfigUseCase @Inject constructor(
    private val evConfigPort: EvConfigPort
) {
    suspend fun execute(evConfig: EvConfig): Long = evConfigPort.saveEvConfig(evConfig)
}
