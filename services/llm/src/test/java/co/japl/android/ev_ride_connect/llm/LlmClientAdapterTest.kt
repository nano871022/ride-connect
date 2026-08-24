package co.japl.android.ev_ride_connect.llm

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test

class LlmClientAdapterTest {

    private lateinit var adapter: LlmClientAdapter

    @Before
    fun setUp() {
        adapter = LlmClientAdapter()
    }

    @Test
    fun shouldValidateApiKeySuccessfully() = runTest {
        val isValid = adapter.validateApiKey("Gemini", "valid-api-key-12345")
        assertThat(isValid).isTrue()
    }

    @Test
    fun shouldReturnFalseForBlankOrShortApiKey() = runTest {
        assertThat(adapter.validateApiKey("Gemini", "")).isFalse()
        assertThat(adapter.validateApiKey("Gemini", "   ")).isFalse()
        assertThat(adapter.validateApiKey("Gemini", "12345")).isFalse()
    }

    @Test
    fun shouldReturnFalseForInvalidKeyPattern() = runTest {
        assertThat(adapter.validateApiKey("Gemini", "invalid-key-here")).isFalse()
        assertThat(adapter.validateApiKey("DeepSeek", "error_key_test")).isFalse()
    }

    @Test
    fun shouldGenerateResponseForValidKey() = runTest {
        val response = adapter.generateResponse("DeepSeek", "valid-key-9999", "Explain physics")
        assertThat(response).contains("Response from DeepSeek: Explain physics")
    }

    @Test
    fun shouldThrowExceptionGeneratingResponseForInvalidKey() = runTest {
        assertThatThrownBy {
            kotlinx.coroutines.runBlocking {
                adapter.generateResponse("DeepSeek", "invalid", "Explain physics")
            }
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldThrowExceptionWhenGeminiApiKeyIsInvalid() = runTest {
        assertThatThrownBy {
            kotlinx.coroutines.runBlocking {
                adapter.generateResponse("Gemini", "valid-key-9999", "buy vsett c7 plus")
            }
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun shouldCallGeminiWhenApiKeyFromEnvIsPresent() = runTest {
        val envKey = System.getenv("GEMINI_API_KEY")
        if (!envKey.isNullOrBlank()) {
            val response = adapter.generateResponse("Gemini", envKey, "buy vsett c7 plus")
            assertThat(response).isNotEmpty()
        }
    }
}
