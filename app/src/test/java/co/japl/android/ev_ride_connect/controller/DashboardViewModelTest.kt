package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
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
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val podamFactory = PodamFactoryImpl()
    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeEvConfigPort: FakeEvConfigPort
    private lateinit var fakeLlmConfigPort: FakeLlmConfigPort
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeEvDataPort = FakeEvDataPort()
        fakeEvConfigPort = FakeEvConfigPort()
        fakeLlmConfigPort = FakeLlmConfigPort()
        viewModel = DashboardViewModel(fakeEvDataPort, fakeEvConfigPort, fakeLlmConfigPort)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadLatestEvDataOnInit() = runTest {
        val initialEvData = EvData(evCode = "EV01", km = 150L, batteryLevel = 80)
        fakeEvDataPort.savedList.add(initialEvData)

        viewModel.loadLatestEvData()
        testScheduler.runCurrent()

        assertThat(viewModel.latestEvData.value).isEqualTo(initialEvData)
    }

    @Test
    fun shouldSaveEvDataAndRefreshLatest() = runTest {
        val config = EvConfig(id = 5L, request = "Vsett C7")
        fakeEvConfigPort.config = config

        viewModel.saveEvData(200L, 90)
        testScheduler.runCurrent()

        assertThat(fakeEvDataPort.savedList).hasSize(1)
        val saved = fakeEvDataPort.savedList.first()
        assertThat(saved.evCode).isEqualTo("5")
        assertThat(saved.km).isEqualTo(200L)
        assertThat(saved.batteryLevel).isEqualTo(90.toShort())
        assertThat(viewModel.latestEvData.value?.km).isEqualTo(200L)
    }

    @Test
    fun shouldShowApiKeyPromptWhenNoActiveConfigsExist() = runTest {
        testScheduler.runCurrent()

        assertThat(viewModel.showApiKeyPrompt.value).isTrue()
    }

    @Test
    fun shouldNotShowApiKeyPromptWhenActiveConfigWithKeyExists() = runTest {
        fakeLlmConfigPort.activeConfigs.add(LlmConfig(id = 1L, apiKey = "valid-key", isActive = true))

        viewModel.checkActiveLlmConfigs()
        testScheduler.runCurrent()

        assertThat(viewModel.showApiKeyPrompt.value).isFalse()
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

    private class FakeEvConfigPort : EvConfigPort {
        var config: EvConfig? = null

        override suspend fun getEvConfig(): EvConfig? = config

        override suspend fun saveEvConfig(config: EvConfig): Long {
            this.config = config
            return 1L
        }
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        val activeConfigs = mutableListOf<LlmConfig>()

        override suspend fun getAllConfigs(): List<LlmConfig> = activeConfigs

        override suspend fun getActiveConfigs(): List<LlmConfig> = activeConfigs.filter { it.isActive }

        override suspend fun saveConfig(config: LlmConfig): Long = 1L

        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean = true

        override suspend fun deleteConfig(id: Long): Boolean = true
    }
}
