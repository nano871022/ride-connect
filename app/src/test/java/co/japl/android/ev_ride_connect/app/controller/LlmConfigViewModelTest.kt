package co.japl.android.ev_ride_connect.app.controller

import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
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

@OptIn(ExperimentalCoroutinesApi::class)
class LlmConfigViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeLlmConfigPort: FakeLlmConfigPort
    private lateinit var fakeLlmClientPort: FakeLlmClientPort
    private lateinit var viewModel: LlmConfigViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeLlmConfigPort = FakeLlmConfigPort()
        fakeLlmClientPort = FakeLlmClientPort()
        viewModel = LlmConfigViewModel(fakeLlmConfigPort, fakeLlmClientPort)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadConfigsOnInitialization() = runTest {
        testScheduler.runCurrent()

        val configs = viewModel.configs.value
        assertThat(configs).hasSize(2)
        assertThat(configs.map { it.modelName }).containsExactly("Gemini", "DeepSeek")

        val active = viewModel.activeConfigs.value
        assertThat(active).hasSize(1)
        assertThat(active.first().modelName).isEqualTo("Gemini")
    }

    @Test
    fun shouldUpdateSelectedModelAndApiKeyInput() = runTest {
        viewModel.onModelSelected("ChatGPT")
        viewModel.onApiKeyChanged("sk-123456789")

        assertThat(viewModel.selectedModel.value).isEqualTo("ChatGPT")
        assertThat(viewModel.apiKeyInput.value).isEqualTo("sk-123456789")
    }

    @Test
    fun shouldSaveConfigWhenApiKeyIsValid() = runTest {
        fakeLlmClientPort.shouldValidateSuccessfully = true

        viewModel.onModelSelected("Groq")
        viewModel.onApiKeyChanged("valid-groq-key")
        viewModel.saveConfig()

        testScheduler.runCurrent()

        assertThat(viewModel.apiKeyInput.value).isEmpty()
        assertThat(viewModel.errorMessage.value).isNull()
        assertThat(viewModel.configs.value).hasSize(3)
        assertThat(viewModel.configs.value.last().modelName).isEqualTo("Groq")
    }

    @Test
    fun shouldNotSaveConfigWhenApiKeyIsInvalid() = runTest {
        fakeLlmClientPort.shouldValidateSuccessfully = false

        viewModel.onModelSelected("Groq")
        viewModel.onApiKeyChanged("invalid-key")
        viewModel.saveConfig()

        testScheduler.runCurrent()

        assertThat(viewModel.errorMessage.value).isEqualTo("INVALID_API_KEY")
        assertThat(viewModel.configs.value).hasSize(2)
    }

    @Test
    fun shouldNotSaveConfigWhenApiKeyIsBlank() = runTest {
        viewModel.onApiKeyChanged("   ")
        viewModel.saveConfig()

        testScheduler.runCurrent()

        assertThat(viewModel.configs.value).hasSize(2)
    }

    @Test
    fun shouldToggleActiveStatus() = runTest {
        viewModel.toggleActiveStatus(1L, false)

        testScheduler.runCurrent()

        val updated = viewModel.configs.value.find { it.id == 1L }
        assertThat(updated?.isActive).isFalse()
        assertThat(viewModel.activeConfigs.value).isEmpty()
    }

    private class FakeLlmConfigPort : LlmConfigPort {
        val configs = mutableListOf(
            LlmConfig(1L, "Gemini", "key-gemini", "2025-01-01", "2025-01-01", true),
            LlmConfig(2L, "DeepSeek", "key-deepseek", "2025-01-01", "2025-01-01", false)
        )
        private var autoId = 3L

        override suspend fun getAllConfigs(): List<LlmConfig> {
            return configs.toList()
        }

        override suspend fun getActiveConfigs(): List<LlmConfig> {
            return configs.filter { it.isActive }
        }

        override suspend fun saveConfig(config: LlmConfig): Long {
            val id = if (config.id == 0L) autoId++ else config.id
            val newConfig = config.copy(id = id)
            configs.add(newConfig)
            return id
        }

        override suspend fun toggleActiveStatus(id: Long, isActive: Boolean): Boolean {
            val index = configs.indexOfFirst { it.id == id }
            if (index >= 0) {
                configs[index] = configs[index].copy(isActive = isActive)
                return true
            }
            return false
        }
    }

    private class FakeLlmClientPort : LlmClientPort {
        var shouldValidateSuccessfully = true

        override suspend fun validateApiKey(modelName: String, apiKey: String): Boolean {
            return shouldValidateSuccessfully
        }

        override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String {
            return "Response for $prompt"
        }
    }
}
