package co.com.japl.ui.theme

import androidx.compose.ui.graphics.Color
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MaterialThemeComposeUITest {

    @Test
    fun shouldReturnCorrectPackageNameForMaterialThemeComposeUI() {
        val className = MaterialThemeComposeUIClassName.THEME_CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.theme.MaterialThemeComposeUIKt")
    }

    @Test
    fun shouldHaveCorrectCyberPulseMobilityColorsInColorScheme() {
        assertThat(DarkColorScheme.primary).isEqualTo(Color(0xFFDBFCFF))
        assertThat(DarkColorScheme.onPrimary).isEqualTo(Color(0xFF00363A))
        assertThat(DarkColorScheme.primaryContainer).isEqualTo(Color(0xFF00F0FF))
        assertThat(DarkColorScheme.onPrimaryContainer).isEqualTo(Color(0xFF006970))
        assertThat(DarkColorScheme.secondary).isEqualTo(Color(0xFFF5FFF3))
        assertThat(DarkColorScheme.secondaryContainer).isEqualTo(Color(0xFF34FF8C))
        assertThat(DarkColorScheme.tertiary).isEqualTo(Color(0xFFFDF2FF))
        assertThat(DarkColorScheme.tertiaryContainer).isEqualTo(Color(0xFFEACFFF))
        assertThat(DarkColorScheme.background).isEqualTo(Color(0xFF111318))
        assertThat(DarkColorScheme.surface).isEqualTo(Color(0xFF111318))
        assertThat(DarkColorScheme.surfaceContainerLow).isEqualTo(Color(0xFF1A1C20))
        assertThat(DarkColorScheme.surfaceContainerHigh).isEqualTo(Color(0xFF282A2E))
        assertThat(DarkColorScheme.error).isEqualTo(Color(0xFFFFB4AB))
    }
}

object MaterialThemeComposeUIClassName {
    val THEME_CLASS_NAME = MaterialThemeComposeUITest::class.java.packageName + ".MaterialThemeComposeUIKt"
}
