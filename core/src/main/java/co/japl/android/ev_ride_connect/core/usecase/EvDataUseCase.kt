package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import javax.inject.Inject

interface EvDataUseCase {
    suspend fun getAllEvData(): List<EvData>
    suspend fun getAllTrips(): List<Trip>
    suspend fun getTripsByDate(startDateMs: Long, endDateMs: Long): List<Trip>
}

class EvDataUseCaseImpl @Inject constructor(
    private val evDataPort: EvDataPort,
    private val tripDatabasePort: TripDatabasePort,
    private val getAllEvDataUseCase: GetAllEvDataUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase,
    private val getTripsByDateUseCase: GetTripsByDateUseCase
) : EvDataUseCase {
    override suspend fun getAllEvData(): List<EvData> {
        return getAllEvDataUseCase.execute()
    }

    override suspend fun getAllTrips(): List<Trip> {
        return getAllTripsUseCase.execute()
    }

    override suspend fun getTripsByDate(startDateMs: Long, endDateMs: Long): List<Trip> {
        return getTripsByDateUseCase.execute(startDateMs, endDateMs)
    }
}
