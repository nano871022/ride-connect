package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.usecase.ClearActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.EvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.FetchEvInfoUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetActiveLlmConfigsUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetEvConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveActiveSessionUseCase
import co.japl.android.ev_ride_connect.core.usecase.SaveEvConfigUseCase
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
class EvConfigViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeEvConfigPort: FakeEvConfigPort
    private lateinit var fakeLlmConfigPort: FakeLlmConfigPort
    private lateinit var fakeLlmClientPort: FakeLlmClientPort
    private lateinit var fakeSessionStatePort: FakeSessionStatePort
    private lateinit var viewModel: EvConfigViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeEvConfigPort = FakeEvConfigPort()
        fakeLlmConfigPort = FakeLlmConfigPort()
        fakeLlmClientPort = FakeLlmClientPort()
        fakeSessionStatePort = FakeSessionStatePort()

        val fetchEvInfoUseCase = FetchEvInfoUseCase(fakeLlmClientPort)
        val evConfigUseCase = EvConfigUseCase(
            fakeEvConfigPort,
            fakeLlmConfigPort,
            fakeLlmClientPort,
            fakeSessionStatePort,
            GetEvConfigUseCase(fakeEvConfigPort),
            SaveEvConfigUseCase(fakeEvConfigPort),
            GetActiveLlmConfigsUseCase(fakeLlmConfigPort),
            fetchEvInfoUseCase,
            GetActiveSessionUseCase(fakeSessionStatePort),
            SaveActiveSessionUseCase(fakeSessionStatePort),
            ClearActiveSessionUseCase(fakeSessionStatePort)
        )
        viewModel = EvConfigViewModel(evConfigUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadSavedConfigOnInit() = runTest {
        testScheduler.runCurrent()

        val config = viewModel.evConfig.value
        assertThat(config.brand).isEqualTo("VSETT")
        assertThat(config.version).isEqualTo("10+")
    }

    @Test
    fun shouldUpdateFieldsCorrectly() = runTest {
        viewModel.onBrandChanged("Segway")
        viewModel.onVersionChanged("Ninebot Max")
        viewModel.onManufactoryYearChanged("2024")

        val config = viewModel.evConfig.value
        assertThat(config.brand).isEqualTo("Segway")
        assertThat(config.version).isEqualTo("Ninebot Max")
        assertThat(config.manufactoryYear).isEqualTo("2024")
    }

    @Test
    fun shouldAddAndUpdateAndRemoveMotors() = runTest {
        viewModel.onAddMotor("Rear Motor", 1000)
        assertThat(viewModel.evConfig.value.motors).hasSize(1)
        assertThat(viewModel.evConfig.value.motors.first().name).isEqualTo("Rear Motor")

        viewModel.onUpdateMotor(0, "Dual Motor", 2000)
        assertThat(viewModel.evConfig.value.motors.first().name).isEqualTo("Dual Motor")

        viewModel.onRemoveMotor(0)
        assertThat(viewModel.evConfig.value.motors).isEmpty()
    }

    @Test
    fun shouldSaveConfigAndSetStatusMessage() = runTest {
        viewModel.onBrandChanged("Inokim")
        viewModel.saveEvConfig()

        testScheduler.runCurrent()

        assertThat(viewModel.statusMessage.value).isEqualTo("CONFIG_SAVED")
        assertThat(fakeEvConfigPort.savedConfig?.brand).isEqualTo("Inokim")
    }

    @Test
    fun shouldRequestEvInfoFromLlmSuccessfully() = runTest {
        fakeLlmConfigPort.activeConfigs.add(
            LlmConfig(id = 1L, modelName = "Gemini", selectedVersion = "gemini-1.5-flash", apiKey = "test-key", isActive = true)
        )
        viewModel.onRequestChanged("VSETT 10+ specs")

        viewModel.requestEvInfoFromLlm()

        testScheduler.runCurrent()

        assertThat(viewModel.statusMessage.value).isEqualTo("LLM_FETCH_SUCCESS")
        assertThat(viewModel.isLoadingLlm.value).isFalse()
        assertThat(viewModel.isSearchDialogVisible.value).isFalse()
    }

    @Test
    fun shouldShowErrorWhenRequestingLlmWithEmptyPrompt() = runTest {
        viewModel.onRequestChanged("   ")
        viewModel.requestEvInfoFromLlm()

        assertThat(viewModel.llmErrorMessage.value).isEqualTo("EMPTY_REQUEST_PROMPT")
    }

    @Test
    fun shouldShowErrorWhenNoActiveLlmConfigForRequest() = runTest {
        fakeLlmConfigPort.activeConfigs.clear()
        viewModel.onRequestChanged("VSETT 10+")

        viewModel.requestEvInfoFromLlm()

        testScheduler.runCurrent()

        assertThat(viewModel.llmErrorMessage.value).isEqualTo("NO_ACTIVE_LLM_CONFIG")
    }

    private class FakeEvConfigPort : EvConfigPort {
        var savedConfig: EvConfig? = EvConfig(
            id = 1L,
            brand = "VSETT",
            version = "10+",
            manufactoryYear = "2023"
        )

        override suspend fun getEvConfig(): EvConfig? = savedConfig

        override suspend fun saveEvConfig(config: EvConfig): Long {
            savedConfig = config
            return config.id.takeIf { it > 0 } ?: 1L
        }
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        val activeConfigs = mutableListOf<LlmConfig>()

        override suspend fun getAllConfigs(): List<LlmConfig> = activeConfigs

        override suspend fun getActiveConfigs(): List<LlmConfig> = activeConfigs

        override suspend fun saveConfig(config: LlmConfig): Long = 1L

        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean = true

        override suspend fun deleteConfig(id: Long): Boolean = true
    }

    private class FakeLlmClientPort : LlmClientPort {
        override suspend fun validateApiKey(modelName: String, apiKey: String): Boolean = true

        override suspend fun fetchAvailableModels(modelName: String, apiKey: String): List<String> = emptyList()

        override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String {
            return """
                {
                  "brand": "VSETT",
                  "version": "10+",
                  "manufactoryYear": "2023",
                  "manufactoryCompany": "VSETT",
                  "batteryTechnology": "Li-ion",
                  "batteryVolts": "60V",
                  "batteryAmpers": "25Ah",
                  "brakeQuantity": 2,
                  "brakeTechnology": "Hydraulic",
                  "suspensionTechnology": "Spring",
                  "chargePower": "60V 2A",
                  "otherCharacteristics": "Dual Motor",
                  "motors": [
                    {"name": "Front Motor", "watts": 1400},
                    {"name": "Rear Motor", "watts": 1400}
                  ]
                }
            """.trimIndent()
        }
    }

    private class FakeSessionStatePort : SessionStatePort {
        private var session: ActiveSession? = null

        override suspend fun saveActiveSession(session: ActiveSession) {
            this.session = session
        }

        override suspend fun getActiveSession(): ActiveSession? = session

        override fun observeActiveSession(): Flow<ActiveSession?> = MutableStateFlow(session)

        override suspend fun clearActiveSession() {
            session = null
        }
    }
}
