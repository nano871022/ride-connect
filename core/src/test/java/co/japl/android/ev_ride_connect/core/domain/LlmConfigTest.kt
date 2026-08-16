package co.japl.android.ev_ride_connect.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class LlmConfigTest {

    private val podamFactory = PodamFactoryImpl()

    @Test
    fun shouldInstantiateLlmConfigWithPodam() {
        val config = podamFactory.manufacturePojo(LlmConfig::class.java)

        assertThat(config).isNotNull
        assertThat(config.modelName).isNotNull
        assertThat(config.apiKey).isNotNull
        assertThat(config.createdAt).isNotNull
        assertThat(config.updatedAt).isNotNull
    }

    @Test
    fun shouldCreateLlmConfigWithGivenValues() {
        val config = LlmConfig(
            id = 1L,
            modelName = "Gemini",
            apiKey = "test-api-key",
            createdAt = "2025-01-01 10:00",
            updatedAt = "2025-01-01 10:00",
            isActive = true
        )

        assertThat(config.id).isEqualTo(1L)
        assertThat(config.modelName).isEqualTo("Gemini")
        assertThat(config.apiKey).isEqualTo("test-api-key")
        assertThat(config.createdAt).isEqualTo("2025-01-01 10:00")
        assertThat(config.updatedAt).isEqualTo("2025-01-01 10:00")
        assertThat(config.isActive).isTrue()
    }
}
