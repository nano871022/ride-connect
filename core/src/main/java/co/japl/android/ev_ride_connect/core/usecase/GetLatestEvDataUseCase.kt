package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import javax.inject.Inject

class GetLatestEvDataUseCase @Inject constructor(
    private val evDataPort: EvDataPort
) {
    suspend fun execute(): EvData? = evDataPort.getLatestEvData()
}
