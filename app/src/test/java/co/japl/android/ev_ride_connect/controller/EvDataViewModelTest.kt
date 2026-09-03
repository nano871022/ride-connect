package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
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

@OptIn(ExperimentalCoroutinesApi::class)
class EvDataViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var viewModel: EvDataViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeEvDataPort = FakeEvDataPort()
        fakeEvDataPort.savedList.add(EvData(evCode = "EV01", km = 100L, batteryLevel = 80))
        fakeEvDataPort.savedList.add(EvData(evCode = "EV01", km = 120L, batteryLevel = 70))
        viewModel = EvDataViewModel(fakeEvDataPort)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadEvDataHistoryOnInit() = runTest {
        viewModel.loadEvDataHistory()
        testScheduler.runCurrent()

        assertThat(viewModel.evDataList.value).hasSize(2)
        assertThat(viewModel.evDataList.value.first().km).isEqualTo(100L)
    }

    private class FakeEvDataPort : EvDataPort {
        val savedList = mutableListOf<EvData>()

        override suspend fun getLatestEvData(): EvData? {
            return savedList.lastOrNull()
        }

        override suspend fun getAllEvData(): List<EvData> {
            return savedList.toList()
        }

        override suspend fun saveEvData(evData: EvData): Long {
            savedList.add(evData)
            return savedList.size.toLong()
        }
    }
}
