package co.com.japl.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UIConstantsTest {

    @Test
    fun shouldReturnCorrectClassNameForUIConstants() {
        val className = UIConstants.CLASS_NAME
        assertThat(className).isEqualTo(UIConstants::class.java.name)
    }

    @Test
    fun shouldHaveValidModuleName() {
        val moduleName = UIConstants.MODULE_NAME
        assertThat(moduleName).isNotBlank()
        assertThat(moduleName).contains("UI")
    }
}
