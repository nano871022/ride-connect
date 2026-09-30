package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import javax.inject.Inject

class EvDataUseCase @Inject constructor(
    private val evDataPort: EvDataPort,
    private val tripDatabasePort: TripDatabasePort,
    private val getAllEvDataUseCase: GetAllEvDataUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase,
    private val getTripsByDateUseCase: GetTripsByDateUseCase
) {
    suspend fun getAllEvData(): List<EvData> {
        return getAllEvDataUseCase.execute()
    }

    suspend fun getAllTrips(): List<Trip> {
        return getAllTripsUseCase.execute()
    }

    suspend fun getTripsByDate(startDateMs: Long, endDateMs: Long): List<Trip> {
        return getTripsByDateUseCase.execute(startDateMs, endDateMs)
    }
}
