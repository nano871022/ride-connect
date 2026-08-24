package co.japl.android.ev_ride_connect.app.controller

import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.domain.MotorSpec
import co.japl.android.ev_ride_connect.core.domain.ScooterState
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.core.usecase.FetchEvInfoUseCase
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
    private lateinit var fakeBleScooterPort: FakeBleScooterPort
    private lateinit var viewModel: EvConfigViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeEvConfigPort = FakeEvConfigPort()
        fakeLlmConfigPort = FakeLlmConfigPort()
        fakeLlmClientPort = FakeLlmClientPort()
        fakeBleScooterPort = FakeBleScooterPort()

        val fetchEvInfoUseCase = FetchEvInfoUseCase(fakeLlmClientPort)
        viewModel = EvConfigViewModel(
            fakeEvConfigPort,
            fakeLlmConfigPort,
            fetchEvInfoUseCase,
            fakeBleScooterPort
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldInitializeAndLoadSavedConfigAndActiveLlmConfigs() = runTest {
        testScheduler.runCurrent()

        assertThat(viewModel.activeLlmConfigs.value).hasSize(1)
        assertThat(viewModel.selectedLlmConfig.value?.modelName).isEqualTo("Gemini")
    }

    @Test
    fun shouldUpdateEvConfigFields() = runTest {
        viewModel.onRequestChanged("Vsett c7 plus")
        viewModel.onBrandChanged("VSETT")
        viewModel.onVersionChanged("C7 Plus")
        viewModel.onManufactoryYearChanged("2023")
        viewModel.onManufactoryCompanyChanged("eMove")
        viewModel.onBoughtDateChanged("2023-01-01")
        viewModel.onBatteryTechnologyChanged("Li-ion")
        viewModel.onBatteryVoltsChanged("60V")
        viewModel.onBatteryAmpersChanged("20.8Ah")
        viewModel.onBrakeQuantityChanged(2)
        viewModel.onBrakeTechnologyChanged("Hydraulic Disc")
        viewModel.onSuspensionTechnologyChanged("Spring")
        viewModel.onChargePowerChanged("67.2V 2A")
        viewModel.onOtherCharacteristicsChanged("Dual motor")

        val current = viewModel.evConfig.value
        assertThat(current.request).isEqualTo("Vsett c7 plus")
        assertThat(current.brand).isEqualTo("VSETT")
        assertThat(current.version).isEqualTo("C7 Plus")
        assertThat(current.manufactoryYear).isEqualTo("2023")
        assertThat(current.manufactoryCompany).isEqualTo("eMove")
        assertThat(current.boughtDate).isEqualTo("2023-01-01")
        assertThat(current.batteryTechnology).isEqualTo("Li-ion")
        assertThat(current.batteryVolts).isEqualTo("60V")
        assertThat(current.batteryAmpers).isEqualTo("20.8Ah")
        assertThat(current.brakeQuantity).isEqualTo(2)
        assertThat(current.brakeTechnology).isEqualTo("Hydraulic Disc")
        assertThat(current.suspensionTechnology).isEqualTo("Spring")
        assertThat(current.chargePower).isEqualTo("67.2V 2A")
        assertThat(current.otherCharacteristics).isEqualTo("Dual motor")
    }

    @Test
    fun shouldAddUpdateAndRemoveMotors() = runTest {
        viewModel.onAddMotor("Front Motor", 1000)
        viewModel.onAddMotor("Rear Motor", 1000)

        assertThat(viewModel.evConfig.value.motors).hasSize(2)

        viewModel.onUpdateMotor(0, "Front Motor Upgraded", 1200)
        assertThat(viewModel.evConfig.value.motors.first().name).isEqualTo("Front Motor Upgraded")
        assertThat(viewModel.evConfig.value.motors.first().watts).isEqualTo(1200)

        viewModel.onRemoveMotor(1)
        assertThat(viewModel.evConfig.value.motors).hasSize(1)
    }

    @Test
    fun shouldFetchEvInfoFromLlmSuccessfully() = runTest {
        testScheduler.runCurrent()

        viewModel.onRequestChanged("Vsett c7 plus by emove colombia seller")
        viewModel.requestEvInfoFromLlm()

        testScheduler.runCurrent()

        assertThat(viewModel.statusMessage.value).isEqualTo("LLM_FETCH_SUCCESS")
        assertThat(viewModel.evConfig.value.brand).isEqualTo("VSETT")
        assertThat(viewModel.evConfig.value.version).isEqualTo("C7 Plus")
        assertThat(viewModel.evConfig.value.motors).hasSize(2)
    }

    @Test
    fun shouldParseMarkdownWrappedLlmJsonResponse() = runTest {
        testScheduler.runCurrent()

        fakeLlmClientPort.customResponse = """
            ```json
            {
              "brand": "VSETT",
              "version": "C7 Plus",
              "motors": [
                {"name": "Rear Hub Motor", "watts": 350}
              ],
              "manufactoryYear": 2023,
              "manufactoryCompany": "Ningbo Vsett Intelligent Technology Co., Ltd.",
              "batteryTechnology": "Lithium-ion",
              "batteryVolts": 36,
              "batteryAmpers": 14.0,
              "brakeQuantity": 2,
              "brakeTechnology": "Hydraulic disc brakes",
              "suspensionTechnology": "Front suspension fork",
              "chargePower": "84W (42V 2A)",
              "otherCharacteristics": [
                "Dual battery system",
                "LCD display"
              ]
            }
            ```
        """.trimIndent()

        viewModel.onRequestChanged("buy vsett c7 plus")
        viewModel.requestEvInfoFromLlm()

        testScheduler.runCurrent()

        assertThat(viewModel.statusMessage.value).isEqualTo("LLM_FETCH_SUCCESS")
        val current = viewModel.evConfig.value
        assertThat(current.brand).isEqualTo("VSETT")
        assertThat(current.version).isEqualTo("C7 Plus")
        assertThat(current.manufactoryYear).isEqualTo("2023")
        assertThat(current.batteryVolts).isEqualTo("36")
        assertThat(current.batteryAmpers).isEqualTo("14.0")
        assertThat(current.otherCharacteristics).contains("Dual battery system", "LCD display")
        assertThat(current.motors).hasSize(1)
        assertThat(current.motors.first().watts).isEqualTo(350)
    }

    @Test
    fun shouldSetErrorWhenRequestPromptIsBlank() = runTest {
        viewModel.onRequestChanged("   ")
        viewModel.requestEvInfoFromLlm()

        testScheduler.runCurrent()

        assertThat(viewModel.llmErrorMessage.value).isEqualTo("EMPTY_REQUEST_PROMPT")
    }

    @Test
    fun shouldSaveEvConfig() = runTest {
        viewModel.onBrandChanged("VSETT")
        viewModel.saveEvConfig()

        testScheduler.runCurrent()

        assertThat(viewModel.statusMessage.value).isEqualTo("CONFIG_SAVED")
        assertThat(fakeEvConfigPort.savedConfig?.brand).isEqualTo("VSETT")
    }

    @Test
    fun shouldLoadEvAndConnectBle() = runTest {
        viewModel.onBrandChanged("VSETT C7 Plus")
        viewModel.loadEvAndConnectBle()

        testScheduler.runCurrent()

        assertThat(viewModel.statusMessage.value).isEqualTo("EV_LOADED_BLE_CONNECTED")
        assertThat(viewModel.evConfig.value.isLoaded).isTrue()
        assertThat(fakeBleScooterPort.lastSentDpId).isEqualTo(1)
    }

    private class FakeEvConfigPort : EvConfigPort {
        var savedConfig: EvConfig? = null

        override suspend fun getEvConfig(): EvConfig? {
            return savedConfig
        }

        override suspend fun saveEvConfig(config: EvConfig): Long {
            savedConfig = config
            return if (config.id == 0L) 1L else config.id
        }
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        val configs = mutableListOf(
            LlmConfig(1L, "Gemini", "valid-gemini-key", "2025-01-01", "2025-01-01", true)
        )

        override suspend fun getAllConfigs(): List<LlmConfig> = configs

        override suspend fun getActiveConfigs(): List<LlmConfig> = configs.filter { it.isActive }

        override suspend fun saveConfig(config: LlmConfig): Long {
            configs.add(config)
            return config.id
        }

        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean = true
    }

    private class FakeLlmClientPort : LlmClientPort {
        var customResponse: String? = null

        override suspend fun validateApiKey(modelName: String, apiKey: String): Boolean = true

        override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String {
            return customResponse ?: """
                {
                  "brand": "VSETT",
                  "version": "C7 Plus",
                  "motors": [{"name": "Front Motor", "watts": 1000}, {"name": "Rear Motor", "watts": 1000}],
                  "manufactoryYear": "2023",
                  "manufactoryCompany": "VSETT / eMove Colombia",
                  "batteryTechnology": "Li-ion 13S",
                  "batteryVolts": "60V",
                  "batteryAmpers": "20.8Ah",
                  "brakeQuantity": 2,
                  "brakeTechnology": "Hydraulic Disc Brake",
                  "suspensionTechnology": "Spring & Hydraulic Suspension",
                  "chargePower": "67.2V 2A",
                  "otherCharacteristics": "Dual motor electric scooter."
                }
            """.trimIndent()
        }
    }

    private class FakeBleScooterPort : BleScooterPort {
        var lastSentDpId: Int? = null
        var lastSentValue: Any? = null

        override fun observeScooterState(): Flow<ScooterState> {
            return MutableStateFlow(
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
        }

        override fun sendCommand(dpId: Int, value: Any) {
            lastSentDpId = dpId
            lastSentValue = value
        }
    }
}
