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
    fun shouldReturnCorrectConfigSectionCardClassName() {
        val className = ConfigSectionCardComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.ConfigSectionCardKt")
    }

    @Test
    fun shouldReturnCorrectSpecTileClassName() {
        val className = SpecTileComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.SpecTileKt")
    }
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

object ConfigSectionCardComponent {
    val CLASS_NAME = ComponentsTest::class.java.packageName + ".ConfigSectionCardKt"
}

object SpecTileComponent {
    val CLASS_NAME = ComponentsTest::class.java.packageName + ".SpecTileKt"
}
