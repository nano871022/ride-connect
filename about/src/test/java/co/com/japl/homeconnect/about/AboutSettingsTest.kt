package co.com.japl.homeconnect.about

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AboutSettingsTest {

    @Test
    fun shouldReturnCorrectClassNameForAboutActivity() {
        val className = AboutSettings.ACTIVITY_CLASS_NAME
        assertThat(className).isEqualTo("co.com.japl.homeconnect.about.AboutActivity")
    }
}
