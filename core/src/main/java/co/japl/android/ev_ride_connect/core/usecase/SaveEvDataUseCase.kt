package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import javax.inject.Inject

class SaveEvDataUseCase @Inject constructor(
    private val evDataPort: EvDataPort
) {
    suspend fun execute(evData: EvData) = evDataPort.saveEvData(evData)
}
