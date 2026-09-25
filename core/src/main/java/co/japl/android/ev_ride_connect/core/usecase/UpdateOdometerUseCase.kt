package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import javax.inject.Inject

class UpdateOdometerUseCase @Inject constructor(
    private val evDataPort: EvDataPort,
    private val getLatestEvDataUseCase: GetLatestEvDataUseCase
) {

    suspend fun execute(evCode: String, newKm: Long, currentBatteryLevel: Short): Long {
        val latest = getLatestEvDataUseCase.execute()
        val battery = currentBatteryLevel.takeIf { it > 0 } ?: latest?.batteryLevel ?: 0
        val data = EvData(
            evCode = evCode,
            km = newKm,
            batteryLevel = battery,
            createTmst = System.currentTimeMillis()
        )
        return evDataPort.saveEvData(data)
    }
}
