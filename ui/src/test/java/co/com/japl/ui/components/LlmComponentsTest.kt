package co.com.japl.ui.components

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LlmComponentsTest {

    @Test
    fun shouldReturnCorrectProviderPillGroupClassName() {
        val className = ProviderPillGroupComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.ProviderPillGroupKt")
    }

    @Test
    fun shouldReturnCorrectModelConfigCardClassName() {
        val className = ModelConfigCardComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.ModelConfigCardKt")
    }
}

object ProviderPillGroupComponent {
    val CLASS_NAME = LlmComponentsTest::class.java.packageName + ".ProviderPillGroupKt"
}

object ModelConfigCardComponent {
    val CLASS_NAME = LlmComponentsTest::class.java.packageName + ".ModelConfigCardKt"
}
