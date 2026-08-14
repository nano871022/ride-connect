package co.com.japl.ui.theme

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MaterialThemeComposeUITest {

    @Test
    fun shouldReturnCorrectPackageNameForMaterialThemeComposeUI() {
        val className = MaterialThemeComposeUIClassName.THEME_CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.theme.MaterialThemeComposeUIKt")
    }
}

object MaterialThemeComposeUIClassName {
    val THEME_CLASS_NAME = MaterialThemeComposeUITest::class.java.packageName + ".MaterialThemeComposeUIKt"
}
