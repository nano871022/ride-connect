package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DashboardUseCase @Inject constructor(
    private val evDataPort: EvDataPort,
    private val evConfigPort: EvConfigPort,
    private val llmConfigPort: LlmConfigPort,
    private val sessionStatePort: SessionStatePort,
    private val tripDatabasePort: TripDatabasePort,
    private val getLatestEvDataUseCase: GetLatestEvDataUseCase,
    private val saveEvDataUseCase: SaveEvDataUseCase,
    private val getEvConfigUseCase: GetEvConfigUseCase,
    private val getActiveLlmConfigsUseCase: GetActiveLlmConfigsUseCase,
    private val observeActiveSessionUseCase: ObserveActiveSessionUseCase,
    private val calculateDynamicBatteryPercentageUseCase: CalculateDynamicBatteryPercentageUseCase,
    private val calculateOptimalBatteryPercentageUseCase: CalculateOptimalBatteryPercentageUseCase,
    private val calculateConsumptionUseCase: CalculateConsumptionUseCase,
    private val updateOdometerUseCase: UpdateOdometerUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase
) {
    suspend fun getLatestEvData(): EvData? {
        return getLatestEvDataUseCase.execute()
    }

    suspend fun saveEvData(evData: EvData): Long {
        return saveEvDataUseCase.execute(evData)
    }

    suspend fun getEvConfig(): EvConfig? {
        return getEvConfigUseCase.execute()
    }

    suspend fun getActiveLlmConfigs(): List<LlmConfig> {
        return getActiveLlmConfigsUseCase.execute()
    }

    fun observeActiveSession(): Flow<ActiveSession?> {
        return observeActiveSessionUseCase.execute()
    }

    fun calculateDynamicBatteryPercentage(batteryInputValue: Double, config: EvConfig?): Short {
        return calculateDynamicBatteryPercentageUseCase.execute(batteryInputValue, config)
    }

    fun calculateOptimalBatteryPercentage(cyclesUsed: Int): Double {
        return calculateOptimalBatteryPercentageUseCase.execute(cyclesUsed)
    }

    fun calculateConsumption(
        batteryConsumedPercentage: Int,
        batteryVoltage: Double,
        batteryAmperes: Double,
        distanceKm: Double
    ): Double {
        return calculateConsumptionUseCase.execute(
            batteryConsumedPercentage,
            batteryVoltage,
            batteryAmperes,
            distanceKm
        )
    }

    suspend fun updateOdometer(evCode: String, newKm: Long, currentBatteryLevel: Short): Long {
        return updateOdometerUseCase.execute(evCode, newKm, currentBatteryLevel)
    }

    suspend fun getAllTrips(): List<Trip> {
        return getAllTripsUseCase.execute()
    }
}
