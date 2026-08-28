package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class TripDatabasePortTest {

    private val podamFactory = PodamFactoryImpl()

    @Test
    fun shouldSaveTripData() = runTest {
        val port = FakeTripDatabasePort()
        val expectedDistance = 1500
        val expectedBatteryConsumed = 25

        port.saveTripData(expectedDistance, expectedBatteryConsumed)

        assertThat(port.savedDistance).isEqualTo(expectedDistance)
        assertThat(port.savedBatteryConsumed).isEqualTo(expectedBatteryConsumed)
    }

    @Test
    fun shouldSaveTripAndGpsPoints() = runTest {
        val port = FakeTripDatabasePort()
        val trip = podamFactory.manufacturePojo(Trip::class.java)
        val gpsPoints = listOf(
            podamFactory.manufacturePojo(TripGps::class.java),
            podamFactory.manufacturePojo(TripGps::class.java)
        )

        val id = port.saveTrip(trip, gpsPoints)

        assertThat(id).isGreaterThan(0)
        assertThat(port.getAllTrips()).hasSize(1)
        assertThat(port.getTripById(id)).isNotNull
        assertThat(port.getGpsPointsByTripId(id)).hasSize(2)
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        var savedDistance: Int? = null
        var savedBatteryConsumed: Int? = null

        private val trips = mutableListOf<Trip>()
        private val gpsMap = mutableMapOf<Long, MutableList<TripGps>>()
        private var nextId = 1L

        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {
            savedDistance = distance
            savedBatteryConsumed = batteryConsumed
        }

        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
            val assignedId = if (trip.id == 0L) nextId++ else trip.id
            val savedTrip = trip.copy(id = assignedId)
            trips.add(savedTrip)
            gpsMap[assignedId] = gpsPoints.map { it.copy(tripId = assignedId) }.toMutableList()
            return assignedId
        }

        override suspend fun getAllTrips(): List<Trip> {
            return trips.sortedByDescending { it.createTmst }
        }

        override suspend fun getTripById(tripId: Long): Trip? {
            return trips.find { it.id == tripId }
        }

        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> {
            return gpsMap[tripId] ?: emptyList()
        }
    }
}
