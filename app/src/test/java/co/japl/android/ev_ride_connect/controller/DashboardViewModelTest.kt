package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeEvDataPort: FakeEvDataPort
    private lateinit var fakeEvConfigPort: FakeEvConfigPort
    private lateinit var fakeLlmConfigPort: FakeLlmConfigPort
    private lateinit var fakeSessionStatePort: FakeSessionStatePort
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeEvDataPort = FakeEvDataPort()
        fakeEvConfigPort = FakeEvConfigPort()
        fakeLlmConfigPort = FakeLlmConfigPort()
        fakeSessionStatePort = FakeSessionStatePort()

        viewModel = DashboardViewModel(
            fakeEvDataPort,
            fakeEvConfigPort,
            fakeLlmConfigPort,
            fakeSessionStatePort
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadLatestEvDataAndCheckLlmConfigOnInit() = runTest {
        testScheduler.runCurrent()

        assertThat(viewModel.latestEvData.value?.evCode).isEqualTo("EV-001")
        assertThat(viewModel.showApiKeyPrompt.value).isFalse()
    }

    @Test
    fun shouldRehydrateResumedSessionWhenActive() = runTest {
        val active = ActiveSession(isRideActive = true, currentDistanceKm = 15.2)
        fakeSessionStatePort.sessionFlow.value = active

        testScheduler.runCurrent()

        assertThat(viewModel.resumedSession.value).isNotNull
        assertThat(viewModel.resumedSession.value?.currentDistanceKm).isEqualTo(15.2)
    }

    @Test
    fun shouldSaveEvDataWithFallbackEvCodeWhenNoConfig() = runTest {
        fakeEvConfigPort.savedConfig = null

        viewModel.saveEvData(100L, 85)
        testScheduler.runCurrent()

        val saved = fakeEvDataPort.savedEvData
        assertThat(saved).isNotNull
        assertThat(saved?.evCode).isEqualTo("EV01")
        assertThat(saved?.km).isEqualTo(100L)
        assertThat(saved?.batteryLevel).isEqualTo(85.toShort())
    }

    private class FakeEvDataPort : EvDataPort {
        var savedEvData: EvData? = null

        override suspend fun getLatestEvData(): EvData? {
            return savedEvData ?: EvData(
                evCode = "EV-001",
                km = 120L,
                batteryLevel = 90,
                createTmst = 1000L
            )
        }

        override suspend fun getAllEvData(): List<EvData> {
            return listOfNotNull(savedEvData)
        }

        override suspend fun saveEvData(evData: EvData): Long {
            savedEvData = evData
            return 1L
        }
    }

    private class FakeEvConfigPort : EvConfigPort {
        var savedConfig: EvConfig? = EvConfig(id = 5L, request = "VSETT C7")

        override suspend fun getEvConfig(): EvConfig? {
            return savedConfig
        }

        override suspend fun saveEvConfig(config: EvConfig): Long {
            savedConfig = config
            return config.id
        }
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        val configs = mutableListOf(
            LlmConfig(id = 1L, modelName = "Gemini", selectedVersion = "gemini-1.5-flash", apiKey = "valid-key", createdAt = "2025-01-01", updatedAt = "2025-01-01", isActive = true)
        )

        override suspend fun getAllConfigs(): List<LlmConfig> = configs

        override suspend fun getActiveConfigs(): List<LlmConfig> = configs.filter { it.isActive }

        override suspend fun saveConfig(config: LlmConfig): Long {
            configs.add(config)
            return config.id
        }

        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean = true

        override suspend fun deleteConfig(id: Long): Boolean = true
    }

    private class FakeSessionStatePort : SessionStatePort {
        val sessionFlow = MutableStateFlow<ActiveSession?>(null)

        override suspend fun saveActiveSession(session: ActiveSession) {
            sessionFlow.value = session
        }

        override suspend fun getActiveSession(): ActiveSession? = sessionFlow.value

        override fun observeActiveSession(): Flow<ActiveSession?> = sessionFlow

        override suspend fun clearActiveSession() {
            sessionFlow.value = null
        }
    }
}
