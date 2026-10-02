package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.SessionStatePort
import co.japl.android.ev_ride_connect.interfaces.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.interfaces.usecase.DashboardUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DashboardUseCaseImpl @Inject constructor(
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
) : DashboardUseCase {
    override suspend fun getLatestEvData(): EvData? {
        return getLatestEvDataUseCase.execute()
    }

    override suspend fun saveEvData(evData: EvData): Long {
        return saveEvDataUseCase.execute(evData)
    }

    override suspend fun getEvConfig(): EvConfig? {
        return getEvConfigUseCase.execute()
    }

    override suspend fun getActiveLlmConfigs(): List<LlmConfig> {
        return getActiveLlmConfigsUseCase.execute()
    }

    override fun observeActiveSession(): Flow<ActiveSession?> {
        return observeActiveSessionUseCase.execute()
    }

    override fun calculateDynamicBatteryPercentage(batteryInputValue: Double, config: EvConfig?): Short {
        return calculateDynamicBatteryPercentageUseCase.execute(batteryInputValue, config)
    }

    override fun calculateOptimalBatteryPercentage(cyclesUsed: Int): Double {
        return calculateOptimalBatteryPercentageUseCase.execute(cyclesUsed)
    }

    override fun calculateConsumption(
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

    override suspend fun updateOdometer(evCode: String, newKm: Long, currentBatteryLevel: Short): Long {
        return updateOdometerUseCase.execute(evCode, newKm, currentBatteryLevel)
    }

    override suspend fun getAllTrips(): List<Trip> {
        return getAllTripsUseCase.execute()
    }
}
