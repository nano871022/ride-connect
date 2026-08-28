package co.japl.android.ev_ride_connect.track

import co.japl.android.ev_ride_connect.core.domain.ScooterState
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScooterTrackingServiceTest {

    private lateinit var fakeBlePort: FakeBleScooterPort
    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var trackingTracker: ScooterTrackingTracker

    @Before
    fun setUp() {
        fakeBlePort = FakeBleScooterPort()
        fakeTripPort = FakeTripDatabasePort()
    }

    @Test
    fun shouldNotBeTrackingInitially() {
        trackingTracker = ScooterTrackingTracker(fakeBlePort, fakeTripPort)
        assertThat(trackingTracker.isTracking.value).isFalse()
    }

    @Test
    fun shouldStartTrackingWhenStartCalled() = runTest {
        trackingTracker = ScooterTrackingTracker(fakeBlePort, fakeTripPort, this)
        trackingTracker.startTracking()

        assertThat(trackingTracker.isTracking.value).isTrue()

        trackingTracker.stopTracking()
    }

    @Test
    fun shouldStopTrackingAndSaveTripWhenStopCalled() = runTest {
        trackingTracker = ScooterTrackingTracker(fakeBlePort, fakeTripPort, this)

        val initialState = ScooterState(
            isLocked = false,
            speedMode = 1,
            currentSpeed = 15,
            realtimeVoltage = 500,
            batteryPercentage = 80,
            totalOdometer = 100,
            isLightOn = false
        )
        fakeBlePort.emitState(initialState)

        trackingTracker.startTracking()
        testScheduler.runCurrent()

        val updatedState = ScooterState(
            isLocked = false,
            speedMode = 1,
            currentSpeed = 20,
            realtimeVoltage = 480,
            batteryPercentage = 65,
            totalOdometer = 115,
            isLightOn = false
        )
        fakeBlePort.emitState(updatedState)
        testScheduler.runCurrent()

        trackingTracker.stopTracking()

        assertThat(trackingTracker.isTracking.value).isFalse()
        assertThat(fakeTripPort.savedTrips).hasSize(1)
        val savedTrip = fakeTripPort.savedTrips.first()
        assertThat(savedTrip.first).isEqualTo(15) // distance: 115 - 100
        assertThat(savedTrip.second).isEqualTo(15) // batteryConsumed: 80 - 65
    }

    private class FakeBleScooterPort : BleScooterPort {
        private val _stateFlow = MutableStateFlow(
            ScooterState(
                isLocked = false,
                speedMode = 0,
                currentSpeed = 0,
                realtimeVoltage = 0,
                batteryPercentage = 0,
                totalOdometer = 0,
                isLightOn = false
            )
        )

        fun emitState(state: ScooterState) {
            _stateFlow.value = state
        }

        override fun observeScooterState() = _stateFlow.asStateFlow()

        override fun sendCommand(dpId: Int, value: Any) {}
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        val savedTrips = mutableListOf<Pair<Int, Int>>()

        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {
            savedTrips.add(distance to batteryConsumed)
        }

        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
            return 1L
        }

        override suspend fun getAllTrips(): List<Trip> {
            return emptyList()
        }

        override suspend fun getTripById(tripId: Long): Trip? {
            return null
        }

        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> {
            return emptyList()
        }
    }
}
