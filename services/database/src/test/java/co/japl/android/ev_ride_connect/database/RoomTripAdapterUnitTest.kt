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
            podamFactory.manufacturePojo(TripGps::class.java).copy(id = 0, tripId = 0, x = 4.6000, y = -74.0000),
            podamFactory.manufacturePojo(TripGps::class.java).copy(id = 0, tripId = 0, x = 4.6001, y = -74.0001)
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

    @Test
    fun shouldFilterOutConsecutiveDuplicateGpsPointsOnSaveTrip() = runTest {
        val trip = podamFactory.manufacturePojo(Trip::class.java).copy(id = 0)
        val gpsPoints = listOf(
            TripGps(orderIndex = 1, speed = 10.0, distance = 0.1, x = 4.6097, y = -74.0817),
            TripGps(orderIndex = 2, speed = 10.0, distance = 0.1, x = 4.6097, y = -74.0817),
            TripGps(orderIndex = 3, speed = 12.0, distance = 0.2, x = 4.6098, y = -74.0818)
        )

        val tripId = adapter.saveTrip(trip, gpsPoints)

        val fetchedGpsPoints = adapter.getGpsPointsByTripId(tripId)
        assertThat(fetchedGpsPoints).hasSize(2)
        assertThat(fetchedGpsPoints[0].x).isEqualTo(4.6097)
        assertThat(fetchedGpsPoints[1].x).isEqualTo(4.6098)
    }

    @Test
    fun shouldGetTripsByDateRange() = runTest {
        val trip1 = Trip(id = 0, createTmst = 1000L)
        val trip2 = Trip(id = 0, createTmst = 2000L)
        val trip3 = Trip(id = 0, createTmst = 3000L)

        adapter.saveTrip(trip1, emptyList())
        adapter.saveTrip(trip2, emptyList())
        adapter.saveTrip(trip3, emptyList())

        val result = adapter.getTripsByDate(1500L, 2500L)
        assertThat(result).hasSize(1)
        assertThat(result[0].createTmst).isEqualTo(2000L)
    }

    @Test
    fun shouldReturnRecapAggregationValues() = runTest {
        val trip1 = Trip(id = 0, distance = 10.0, batteryConsumed = 20, createTmst = 1000L)
        val trip2 = Trip(id = 0, distance = 15.5, batteryConsumed = 10, createTmst = 2000L)

        adapter.saveTrip(trip1, emptyList())
        adapter.saveTrip(trip2, emptyList())

        assertThat(adapter.getTotalTripsCount()).isEqualTo(2)
        assertThat(adapter.getTotalDistanceKm()).isEqualTo(25.5)
        assertThat(adapter.getChargeDetectionsCount(15)).isEqualTo(1)
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

        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<TripEntity> {
            return trips.filter {
                (it.createTmst in startTimestamp..endTimestamp) || (it.createTmst == 0L && it.timestamp in startTimestamp..endTimestamp)
            }.sortedByDescending { it.createTmst }
        }

        override suspend fun getTotalTripsCount(): Int = trips.size

        override suspend fun getTotalDistanceKm(): Double? = trips.sumOf { if (it.distanceKm > 0) it.distanceKm else it.distance / 1000.0 }

        override suspend fun getChargeDetectionsCount(threshold: Int): Int = trips.count { it.batteryConsumed >= threshold }
    }
}
