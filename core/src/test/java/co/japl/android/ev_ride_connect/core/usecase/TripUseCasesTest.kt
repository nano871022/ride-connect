package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class TripUseCasesTest {

    private val podamFactory = PodamFactoryImpl()
    private lateinit var fakeSessionStatePort: FakeSessionStatePort
    private lateinit var fakeTripDatabasePort: FakeTripDatabasePort

    private lateinit var pauseTripUseCase: PauseTripUseCase
    private lateinit var resumeTripUseCase: ResumeTripUseCase
    private lateinit var endTripUseCase: EndTripUseCase
    private lateinit var calculateTripSummaryUseCase: CalculateTripSummaryUseCase
    private lateinit var getTripsByDateUseCase: GetTripsByDateUseCase
    private lateinit var getTripDetailsUseCase: GetTripDetailsUseCase

    private class FakeSessionStatePort : SessionStatePort {
        private var session: ActiveSession? = null

        override suspend fun getActiveSession(): ActiveSession? = session

        override fun observeActiveSession(): Flow<ActiveSession?> = flowOf(session)

        override suspend fun saveActiveSession(session: ActiveSession) {
            this.session = session
        }

        override suspend fun clearActiveSession() {
            this.session = null
        }
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        val trips = mutableMapOf<Long, Trip>()
        val gpsPoints = mutableMapOf<Long, List<TripGps>>()

        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {}

        override suspend fun saveTrip(trip: Trip, gpsPoints: List<TripGps>): Long {
            val id = (trips.size + 1).toLong()
            val savedTrip = trip.copy(id = id)
            trips[id] = savedTrip
            this.gpsPoints[id] = gpsPoints
            return id
        }

        override suspend fun getAllTrips(): List<Trip> = trips.values.toList()

        override suspend fun getTripById(tripId: Long): Trip? = trips[tripId]

        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = gpsPoints[tripId] ?: emptyList()

        override suspend fun getTripsByDate(startTimestamp: Long, endTimestamp: Long): List<Trip> {
            return trips.values.filter { it.createTmst in startTimestamp..endTimestamp }
        }
    }

    @Before
    fun setUp() {
        fakeSessionStatePort = FakeSessionStatePort()
        fakeTripDatabasePort = FakeTripDatabasePort()

        pauseTripUseCase = PauseTripUseCase(fakeSessionStatePort)
        resumeTripUseCase = ResumeTripUseCase(fakeSessionStatePort)
        endTripUseCase = EndTripUseCase(fakeSessionStatePort)
        calculateTripSummaryUseCase = CalculateTripSummaryUseCase(fakeTripDatabasePort)
        getTripsByDateUseCase = GetTripsByDateUseCase(fakeTripDatabasePort)
        getTripDetailsUseCase = GetTripDetailsUseCase(fakeTripDatabasePort)
    }

    @Test
    fun shouldPauseTripWhenActive() = runTest {
        val activeSession = ActiveSession(isRideActive = true, isPaused = false)
        fakeSessionStatePort.saveActiveSession(activeSession)

        pauseTripUseCase.execute()

        val updated = fakeSessionStatePort.getActiveSession()
        assertThat(updated).isNotNull
        assertThat(updated!!.isPaused).isTrue()
        assertThat(updated.isRideActive).isTrue()
    }

    @Test
    fun shouldResumeTripWhenPaused() = runTest {
        val pausedSession = ActiveSession(isRideActive = true, isPaused = true)
        fakeSessionStatePort.saveActiveSession(pausedSession)

        resumeTripUseCase.execute()

        val updated = fakeSessionStatePort.getActiveSession()
        assertThat(updated).isNotNull
        assertThat(updated!!.isPaused).isFalse()
        assertThat(updated.isRideActive).isTrue()
    }

    @Test
    fun shouldEndTripAndClearSessionWhenNotProcessingLlm() = runTest {
        val session = ActiveSession(isRideActive = true, isPaused = false)
        fakeSessionStatePort.saveActiveSession(session)

        endTripUseCase.execute()

        val updated = fakeSessionStatePort.getActiveSession()
        assertThat(updated).isNull()
    }

    @Test
    fun shouldCalculateTripSummaryFromDatabase() = runTest {
        val trip = Trip(id = 1L, distance = 10.5, averageSpeed = 21.0, timeTrip = 1800)
        val points = listOf(
            podamFactory.manufacturePojo(TripGps::class.java),
            podamFactory.manufacturePojo(TripGps::class.java),
            podamFactory.manufacturePojo(TripGps::class.java)
        )
        fakeTripDatabasePort.saveTrip(trip, points)

        val summary = calculateTripSummaryUseCase.execute(1L, batteryConsumed = 15)

        assertThat(summary).isNotNull
        assertThat(summary!!.totalDistanceKm).isEqualTo(10.5)
        assertThat(summary.averageSpeedKmH).isEqualTo(21.0)
        assertThat(summary.totalGpsLocationsCount).isEqualTo(3)
        assertThat(summary.totalDurationSeconds).isEqualTo(1800L)
        assertThat(summary.batteryConsumedPercentage).isEqualTo(15)
    }

    @Test
    fun shouldCalculateTripSummaryFromRawData() {
        val summary = calculateTripSummaryUseCase.calculateFromRawData(
            distanceKm = 10.0,
            durationSeconds = 1800,
            gpsPointsCount = 5,
            batteryConsumed = 20
        )

        assertThat(summary.totalDistanceKm).isEqualTo(10.0)
        assertThat(summary.averageSpeedKmH).isEqualTo(20.0)
        assertThat(summary.totalGpsLocationsCount).isEqualTo(5)
        assertThat(summary.totalDurationSeconds).isEqualTo(1800L)
        assertThat(summary.batteryConsumedPercentage).isEqualTo(20)
    }

    @Test
    fun shouldGetTripsByDate() = runTest {
        val trip1 = Trip(createTmst = 1000L)
        val trip2 = Trip(createTmst = 2000L)
        val trip3 = Trip(createTmst = 3000L)

        fakeTripDatabasePort.saveTrip(trip1, emptyList())
        fakeTripDatabasePort.saveTrip(trip2, emptyList())
        fakeTripDatabasePort.saveTrip(trip3, emptyList())

        val result = getTripsByDateUseCase.execute(1500L, 2500L)

        assertThat(result).hasSize(1)
        assertThat(result[0].createTmst).isEqualTo(2000L)
    }

    @Test
    fun shouldGetTripDetailsWithGpsPoints() = runTest {
        val trip = Trip(id = 1L, distance = 5.0)
        val points = listOf(
            TripGps(orderIndex = 1, x = 4.0, y = -74.0),
            TripGps(orderIndex = 2, x = 4.1, y = -74.1)
        )
        val savedId = fakeTripDatabasePort.saveTrip(trip, points)

        val details = getTripDetailsUseCase.execute(savedId)

        assertThat(details).isNotNull
        assertThat(details!!.first.id).isEqualTo(savedId)
        assertThat(details.second).hasSize(2)
        assertThat(details.second[0].x).isEqualTo(4.0)
    }
}
