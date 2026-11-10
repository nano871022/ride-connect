package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.database.dao.TripDao
import co.japl.android.ev_ride_connect.database.entities.TripEntity
import co.japl.android.ev_ride_connect.database.entities.TripGpsEntity
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class RoomTripAdapterRf04Test {

    private lateinit var fakeTripDao: FakeTripDao
    private lateinit var adapter: RoomTripAdapter
    private val podamFactory = PodamFactoryImpl()

    @Before
    fun setUp() {
        fakeTripDao = FakeTripDao()
        adapter = RoomTripAdapter(fakeTripDao)
    }

    @Test
    fun shouldSaveAndRetrieveGpsPointTimestamp() = runTest {
        val timestamp = 1700000000000L
        val trip = Trip(id = 0, createTmst = timestamp)
        val gpsPoints = listOf(
            TripGps(
                id = 0, tripId = 0, orderIndex = 1,
                speed = 10.0, distance = 0.1, x = 4.6097, y = -74.0817,
                createTmst = timestamp, motionState = co.japl.android.ev_ride_connect.interfaces.model.MotionState.MOVING
            )
        )

        val tripId = adapter.saveTrip(trip, gpsPoints)

        val fetchedGpsPoints = adapter.getGpsPointsByTripId(tripId)
        assertThat(fetchedGpsPoints).hasSize(1)
        assertThat(fetchedGpsPoints[0].createTmst).isEqualTo(timestamp)
    }

    @Test
    fun shouldSaveAndRetrieveDistanceInKilometers() = runTest {
        val distanceKm = 2.5
        val trip = Trip(id = 0, distance = distanceKm, createTmst = 1000L)

        val tripId = adapter.saveTrip(trip, emptyList())

        val allTrips = adapter.getAllTrips()
        assertThat(allTrips).hasSize(1)
        assertThat(allTrips[0].distance).isEqualTo(distanceKm)
    }

    @Test
    fun shouldSaveAndRetrieveMaxSpeed() = runTest {
        val maxSpeed = 25.0
        val trip = Trip(id = 0, maxSpeed = maxSpeed, createTmst = 1000L)

        val tripId = adapter.saveTrip(trip, emptyList())

        val fetchedTrip = adapter.getTripById(tripId)
        assertThat(fetchedTrip).isNotNull
        assertThat(fetchedTrip?.maxSpeed).isEqualTo(maxSpeed)
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
