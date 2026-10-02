package co.japl.android.ev_ride_connect.interfaces.usecase

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import kotlinx.coroutines.flow.Flow

interface DashboardUseCase {
    suspend fun getLatestEvData(): EvData?
    suspend fun saveEvData(evData: EvData): Long
    suspend fun getEvConfig(): EvConfig?
    suspend fun getActiveLlmConfigs(): List<LlmConfig>
    fun observeActiveSession(): Flow<ActiveSession?>
    fun calculateDynamicBatteryPercentage(batteryInputValue: Double, config: EvConfig?): Short
    fun calculateOptimalBatteryPercentage(cyclesUsed: Int): Double
    fun calculateConsumption(
        batteryConsumedPercentage: Int,
        batteryVoltage: Double,
        batteryAmperes: Double,
        distanceKm: Double
    ): Double
    suspend fun updateOdometer(evCode: String, newKm: Long, currentBatteryLevel: Short): Long
    suspend fun getAllTrips(): List<Trip>
}
