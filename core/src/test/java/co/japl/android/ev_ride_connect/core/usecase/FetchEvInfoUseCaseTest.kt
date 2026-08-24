package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class FetchEvInfoUseCaseTest {

    private lateinit var useCase: FetchEvInfoUseCase
    private lateinit var mockLlmClientPort: FakeLlmClientPort

    private class FakeLlmClientPort : LlmClientPort {
        var lastPrompt: String? = null
        var responseToReturn: String = ""

        override suspend fun validateApiKey(modelName: String, apiKey: String): Boolean = true

        override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String {
            lastPrompt = prompt
            return responseToReturn
        }
    }

    @Before
    fun setUp() {
        mockLlmClientPort = FakeLlmClientPort()
        useCase = FetchEvInfoUseCase(mockLlmClientPort)
    }

    @Test
    fun shouldFetchEvInfoAndMergeWithCurrentConfig() = runTest {
        mockLlmClientPort.responseToReturn = """
            {
              "brand": "VSETT",
              "version": "10+",
              "motors": [{"name": "Dual Motor", "watts": 1400}],
              "manufactoryYear": "2023",
              "manufactoryCompany": "VSETT",
              "batteryTechnology": "Li-ion",
              "batteryVolts": "60V",
              "batteryAmpers": "28Ah",
              "brakeQuantity": 2,
              "brakeTechnology": "Hydraulic",
              "suspensionTechnology": "Spring",
              "chargePower": "60V 2A",
              "otherCharacteristics": "Dual charging port"
            }
        """.trimIndent()

        val currentConfig = EvConfig(brand = "OldBrand")
        val llmConfig = LlmConfig(id = 1, modelName = "Gemini", apiKey = "test-key-123", isActive = true)

        val updatedConfig = useCase.execute("VSETT 10+", llmConfig, currentConfig)

        assertThat(mockLlmClientPort.lastPrompt).contains("VSETT 10+")
        assertThat(updatedConfig.brand).isEqualTo("VSETT")
        assertThat(updatedConfig.version).isEqualTo("10+")
        assertThat(updatedConfig.motors).hasSize(1)
        assertThat(updatedConfig.motors[0].watts).isEqualTo(1400)
    }
}
