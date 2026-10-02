package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.interfaces.model.Trip
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import co.japl.android.ev_ride_connect.interfaces.usecase.TripDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

@OptIn(ExperimentalCoroutinesApi::class)
class TripDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val podamFactory = PodamFactoryImpl()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadTripDetailSuccessfully() = runTest {
        val expectedTrip = podamFactory.manufacturePojo(Trip::class.java)
        val expectedGpsPoint = podamFactory.manufacturePojo(TripGps::class.java)
        val fakeTripDetailUseCase = FakeTripDetailUseCase(Pair(expectedTrip, listOf(expectedGpsPoint)))

        val viewModel = TripDetailViewModel(fakeTripDetailUseCase)

        viewModel.loadTripDetail(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.selectedTripDetail.value
        assertThat(result).isNotNull
        assertThat(result?.first).isEqualTo(expectedTrip)
        assertThat(result?.second).hasSize(1)
        assertThat(result?.second?.get(0)).isEqualTo(expectedGpsPoint)
    }

    @Test
    fun shouldHandleErrorWhenLoadingTripDetail() = runTest {
        val fakeTripDetailUseCase = FakeFailingTripDetailUseCase()

        val viewModel = TripDetailViewModel(fakeTripDetailUseCase)

        viewModel.loadTripDetail(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.selectedTripDetail.value
        assertThat(result).isNull()
    }

    private class FakeTripDetailUseCase(
        private val tripDetails: Pair<Trip, List<TripGps>>?
    ) : TripDetailUseCase {
        override suspend fun getTripDetails(tripId: Long): Pair<Trip, List<TripGps>>? = tripDetails
        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = tripDetails?.second ?: emptyList()
        override suspend fun getTripById(tripId: Long): Trip? = tripDetails?.first
    }

    private class FakeFailingTripDetailUseCase : TripDetailUseCase {
        override suspend fun getTripDetails(tripId: Long): Pair<Trip, List<TripGps>>? {
            throw RuntimeException("Database error")
        }
        override suspend fun getGpsPointsByTripId(tripId: Long): List<TripGps> = emptyList()
        override suspend fun getTripById(tripId: Long): Trip? = null
    }
}
