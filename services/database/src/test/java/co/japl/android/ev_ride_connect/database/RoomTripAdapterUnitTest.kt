package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.database.dao.TripDao
import co.japl.android.ev_ride_connect.database.entities.TripEntity
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class RoomTripAdapterUnitTest {

    private lateinit var fakeTripDao: FakeTripDao
    private lateinit var adapter: RoomTripAdapter
    private lateinit var backupHelper: GoogleDriveBackupHelper

    @Before
    fun setUp() {
        fakeTripDao = FakeTripDao()
        adapter = RoomTripAdapter(fakeTripDao)
        backupHelper = GoogleDriveBackupHelper()
    }

    @Test
    fun shouldSaveTripDataSuccessfully() = runTest {
        val distance = 2500
        val batteryConsumed = 18

        adapter.saveTripData(distance, batteryConsumed)

        val trips = fakeTripDao.getAllTrips()
        assertThat(trips).hasSize(1)
        assertThat(trips[0].distance).isEqualTo(distance)
        assertThat(trips[0].batteryConsumed).isEqualTo(batteryConsumed)
    }

    private class FakeTripDao : TripDao {
        private val trips = mutableListOf<TripEntity>()

        override suspend fun insertTrip(trip: TripEntity) {
            trips.add(trip)
        }

        override suspend fun getAllTrips(): List<TripEntity> {
            return trips.toList()
        }
    }
}
