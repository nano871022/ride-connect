package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.database.dao.TripDao
import co.japl.android.ev_ride_connect.database.entities.TripEntity
import co.japl.android.ev_ride_connect.database.entities.TripGpsEntity
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class RoomTripAdapterUnitTest {

    private lateinit var fakeTripDao: FakeTripDao
    private lateinit var adapter: RoomTripAdapter
    private val podamFactory = PodamFactoryImpl()

    @Before
    fun setUp() {
        fakeTripDao = FakeTripDao()
        adapter = RoomTripAdapter(fakeTripDao)
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

    @Test
    fun shouldSaveTripAndFetchTripHistoryAndDetail() = runTest {
        val trip = podamFactory.manufacturePojo(Trip::class.java).copy(id = 0)
        val gpsPoints = listOf(
            podamFactory.manufacturePojo(TripGps::class.java).copy(id = 0, tripId = 0),
            podamFactory.manufacturePojo(TripGps::class.java).copy(id = 0, tripId = 0)
        )

        val tripId = adapter.saveTrip(trip, gpsPoints)

        assertThat(tripId).isGreaterThan(0)

        val allTrips = adapter.getAllTrips()
        assertThat(allTrips).hasSize(1)
        assertThat(allTrips[0].id).isEqualTo(tripId)

        val fetchedTrip = adapter.getTripById(tripId)
        assertThat(fetchedTrip).isNotNull
        assertThat(fetchedTrip?.id).isEqualTo(tripId)

        val fetchedGpsPoints = adapter.getGpsPointsByTripId(tripId)
        assertThat(fetchedGpsPoints).hasSize(2)
        assertThat(fetchedGpsPoints[0].tripId).isEqualTo(tripId)
    }

    private class FakeTripDao : TripDao {
        private val trips = mutableListOf<TripEntity>()
        private val gpsList = mutableListOf<TripGpsEntity>()
        private var nextTripId = 1L
        private var nextGpsId = 1L

        override suspend fun insertTrip(trip: TripEntity): Long {
            val assignedId = if (trip.id == 0L) nextTripId++ else trip.id
            val entityToSave = trip.copy(id = assignedId)
            trips.add(entityToSave)
            return assignedId
        }

        override suspend fun insertTripGpsList(gpsPoints: List<TripGpsEntity>) {
            gpsPoints.forEach { gps ->
                val assignedId = if (gps.id == 0L) nextGpsId++ else gps.id
                gpsList.add(gps.copy(id = assignedId))
            }
        }

        override suspend fun getAllTrips(): List<TripEntity> {
            return trips.sortedByDescending { it.createTmst }
        }

        override suspend fun getTripById(tripId: Long): TripEntity? {
            return trips.find { it.id == tripId }
        }

        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGpsEntity> {
            return gpsList.filter { it.tripId == tripId }.sortedBy { it.orderIndex }
        }
    }
}
