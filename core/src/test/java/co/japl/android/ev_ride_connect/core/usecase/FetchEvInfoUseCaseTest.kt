package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig
import co.japl.android.ev_ride_connect.interfaces.ports.LlmClientPort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class FetchEvInfoUseCaseTest {

    private lateinit var useCase: FetchEvInfoUseCase

    @Before
    fun setUp() {
        useCase = FetchEvInfoUseCase(FakeLlmClientPort())
    }

    @Test
    fun shouldFetchEvInfoFromLlm() = runTest {
        val currentConfig = EvConfig(brand = "VSETT")
        val llmConfig = LlmConfig(apiKey = "key", modelName = "Gemini")

        val updatedConfig = useCase.execute("Get specs", llmConfig, currentConfig, null)

        assertThat(updatedConfig.brand).isEqualTo("VSETT")
    }

    private class FakeLlmClientPort : LlmClientPort {
        override suspend fun queryLlm(prompt: String, config: LlmConfig, promptTemplate: String?): String {
            return "{\"brand\": \"VSETT\", \"version\": \"C7 Plus\"}"
        }

        override suspend fun fetchAvailableModels(apiKey: String): List<String> = emptyList()
    }
}
