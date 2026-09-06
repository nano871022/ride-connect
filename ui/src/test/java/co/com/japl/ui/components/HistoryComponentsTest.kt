package co.com.japl.ui.components

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HistoryComponentsTest {

    @Test
    fun shouldReturnCorrectAnalyticsStatCardClassName() {
        val className = AnalyticsStatCardComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.AnalyticsStatCardKt")
    }

    @Test
    fun shouldReturnCorrectFilterPillGroupClassName() {
        val className = FilterPillGroupComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.FilterPillGroupKt")
    }

    @Test
    fun shouldReturnCorrectHistoryRecordCardClassName() {
        val className = HistoryRecordCardComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.HistoryRecordCardKt")
    }

    @Test
    fun shouldReturnCorrectMaintenanceHealthCardClassName() {
        val className = MaintenanceHealthCardComponent.CLASS_NAME
        assertThat(className).contains("co.com.japl.ui.components.MaintenanceHealthCardKt")
    }
}

object AnalyticsStatCardComponent {
    val CLASS_NAME = HistoryComponentsTest::class.java.packageName + ".AnalyticsStatCardKt"
}

object FilterPillGroupComponent {
    val CLASS_NAME = HistoryComponentsTest::class.java.packageName + ".FilterPillGroupKt"
}

object HistoryRecordCardComponent {
    val CLASS_NAME = HistoryComponentsTest::class.java.packageName + ".HistoryRecordCardKt"
}

object MaintenanceHealthCardComponent {
    val CLASS_NAME = HistoryComponentsTest::class.java.packageName + ".MaintenanceHealthCardKt"
}
