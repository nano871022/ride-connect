package co.japl.android.ev_ride_connect.interfaces.model

import co.japl.android.ev_ride_connect.interfaces.ports.LlmClientPort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LlmClientPortTest {

    @Test
    fun shouldValidateApiKeyAndGenerateResponseWithFakePort() = runTest {
        val fakePort = object : LlmClientPort {
            override suspend fun queryLlm(prompt: String, config: LlmConfig, promptTemplate: String?): String {
                return "Response for $prompt"
            }

            override suspend fun fetchAvailableModels(apiKey: String): List<String> {
                return listOf("v1", "v2")
            }

            override suspend fun validateApiKey(modelName: String, apiKey: String): Boolean {
                return apiKey.startsWith("valid-")
            }

            override suspend fun fetchAvailableModels(modelName: String, apiKey: String): List<String> {
                return listOf("$modelName-v1", "$modelName-v2")
            }

            override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String {
                return "Response for $prompt using $modelName"
            }
        }

        assertThat(fakePort.validateApiKey("Gemini", "valid-key-123")).isTrue()
        assertThat(fakePort.validateApiKey("Gemini", "invalid-key")).isFalse()

        val response = fakePort.generateResponse("Gemini", "valid-key-123", "Hello")
        assertThat(response).isEqualTo("Response for Hello using Gemini")

        val models = fakePort.fetchAvailableModels("Gemini", "valid-key-123")
        assertThat(models).containsExactly("Gemini-v1", "Gemini-v2")
    }
}
