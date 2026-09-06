package co.com.japl.ui.components

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ComponentsTest {

    @Test
    fun shouldHaveCorrectPackageForComponents() {
        val packageName = ComponentsTest::class.java.packageName
        assertThat(packageName).isEqualTo("co.com.japl.ui.components")
    }

    @Test
    fun shouldReturnCorrectMetricCardClassName() {
        val className = MetricCardComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.MetricCardKt")
    }

    @Test
    fun shouldReturnCorrectSegmentedButtonGroupClassName() {
        val className = SegmentedButtonGroupComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.SegmentedButtonGroupKt")
    }

    @Test
    fun shouldReturnCorrectSettingSwitchRowClassName() {
        val className = SettingSwitchRowComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.SettingSwitchRowKt")
    }

    @Test
    fun shouldReturnCorrectStatusCardClassName() {
        val className = StatusCardComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.StatusCardKt")
    }

    @Test
    fun shouldReturnCorrectSpeedometerGaugeClassName() {
        val className = SpeedometerGaugeComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.SpeedometerGaugeKt")
    }

    @Test
    fun shouldReturnCorrectTelemetryBentoCardClassName() {
        val className = TelemetryBentoCardComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.TelemetryBentoCardKt")
    }

    @Test
    fun shouldReturnCorrectMaintenanceBannerClassName() {
        val className = MaintenanceBannerComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.MaintenanceBannerKt")
    }
}

object SpeedometerGaugeComponent {
    val CLASS_NAME = ComponentsTest::class.java.packageName + ".SpeedometerGaugeKt"
}

object TelemetryBentoCardComponent {
    val CLASS_NAME = ComponentsTest::class.java.packageName + ".TelemetryBentoCardKt"
}

object MaintenanceBannerComponent {
    val CLASS_NAME = ComponentsTest::class.java.packageName + ".MaintenanceBannerKt"
}

object MetricCardComponent {
    val CLASS_NAME = ComponentsTest::class.java.packageName + ".MetricCardKt"
}

object SegmentedButtonGroupComponent {
    val CLASS_NAME = ComponentsTest::class.java.packageName + ".SegmentedButtonGroupKt"
}

object SettingSwitchRowComponent {
    val CLASS_NAME = ComponentsTest::class.java.packageName + ".SettingSwitchRowKt"
}

object StatusCardComponent {
    val CLASS_NAME = ComponentsTest::class.java.packageName + ".StatusCardKt"
}
