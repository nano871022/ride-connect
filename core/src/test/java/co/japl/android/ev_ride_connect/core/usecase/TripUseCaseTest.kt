package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.MotionState
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.MotionDetectorPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TripUseCaseTest {

    private lateinit var fakeTripPort: FakeTripDatabasePort
    private lateinit var fakeSessionPort: FakeSessionStatePort
    private lateinit var fakeEvConfigPort: FakeEvConfigPort
    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeMotionPort: FakeMotionDetectorPort
    private lateinit var useCase: TripUseCase

    @Before
    fun setUp() {
        fakeTripPort = FakeTripDatabasePort()
        fakeSessionPort = FakeSessionStatePort()
        fakeEvConfigPort = FakeEvConfigPort()
        fakeEvDataPort = FakeEvDataPort()
        fakeMotionPort = FakeMotionDetectorPort()

        val getLatestEvDataUseCase = GetLatestEvDataUseCase(fakeEvDataPort)
        val getEvConfigUseCase = GetEvConfigUseCase(fakeEvConfigPort)

        useCase = TripUseCaseImpl(
            fakeTripPort,
            fakeSessionPort,
            fakeEvConfigPort,
            fakeEvDataPort,
            fakeMotionPort,
            SaveTripUseCase(fakeTripPort),
            EndTripUseCase(fakeSessionPort),
            PauseTripUseCase(fakeSessionPort),
            ResumeTripUseCase(fakeSessionPort),
            CalculateTripSummaryUseCase(fakeTripPort),
            CalculateCo2SavedUseCase(),
            CalculateConsumptionUseCase(),
            CalculateDynamicBatteryPercentageUseCase(),
            GetTripDetailsUseCase(fakeTripPort),
            GetTripsByDateUseCase(fakeTripPort),
            getEvConfigUseCase,
            getLatestEvDataUseCase,
            SaveEvDataUseCase(fakeEvDataPort),
            GetActiveSessionUseCase(fakeSessionPort),
            ObserveActiveSessionUseCase(fakeSessionPort),
            SaveActiveSessionUseCase(fakeSessionPort)
        )
    }

    @Test
    fun shouldSaveTripAndEndTrip() = runTest {
        val trip = Trip(id = 1L, distance = 5.0)
        val id = useCase.saveTrip(trip, emptyList())

        assertThat(id).isEqualTo(1L)

        useCase.endTrip()
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>) = trip.id.takeIf { it > 0 } ?: 1L
        override suspend fun getAllTrips() = emptyList<Trip>()
        override suspend fun getTripById(tripId: Long) = null
        override suspend fun getGpsPointsByTripId(tripId: Long) = emptyList<TripGps>()
        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long) = emptyList<Trip>()
        override suspend fun getTotalTripsCount() = 0
        override suspend fun getTotalDistanceKm() = 0.0
        override suspend fun getChargeDetectionsCount(threshold: Int) = 0
        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}
    }

    private class FakeSessionStatePort : SessionStatePort {
        var activeSession: ActiveSession? = null

        override suspend fun saveActiveSession(session: ActiveSession) {
            activeSession = session
        }

        override suspend fun getActiveSession() = activeSession

        override fun observeActiveSession() = MutableStateFlow(activeSession)

        override suspend fun clearActiveSession() {
            activeSession = null
        }
    }

    private class FakeEvConfigPort : EvConfigPort {
        override suspend fun getEvConfig() = EvConfig(id = 1L)
        override suspend fun saveEvConfig(config: EvConfig) = 1L
    }

    private class FakeEvDataPort : EvDataPort {
        override suspend fun getLatestEvData() = EvData(evCode = "EV01", km = 100L, batteryLevel = 90)
        override suspend fun getAllEvData() = emptyList<EvData>()
        override suspend fun saveEvData(evData: EvData) = 1L
    }

    private class FakeMotionDetectorPort : MotionDetectorPort {
        private val _motionState = MutableStateFlow(MotionState.STOPPED)
        override val motionState: StateFlow<MotionState> = _motionState

        override fun start() {}
        override fun stop() {}
        override fun processSensorData(
            accX: Float, accY: Float, accZ: Float,
            gyroX: Float, gyroY: Float, gyroZ: Float,
            currentTimestamp: Long
        ) {}
        override fun updateState(newState: MotionState, currentTimestamp: Long) {
            _motionState.value = newState
        }
    }
}
