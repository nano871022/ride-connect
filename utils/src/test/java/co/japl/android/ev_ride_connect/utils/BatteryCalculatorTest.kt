package co.japl.android.ev_ride_connect.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BatteryCalculatorTest {

    @Test
    fun shouldReturnHundredWhenVoltageIs546() {
        val percentage = BatteryCalculator.calculate13SPercentage(546)
        assertThat(percentage).isEqualTo(100)
    }

    @Test
    fun shouldReturnZeroWhenVoltageIs390() {
        val percentage = BatteryCalculator.calculate13SPercentage(390)
        assertThat(percentage).isEqualTo(0)
    }

    @Test
    fun shouldReturnFiftyWhenVoltageIs468() {
        // (468 - 390) / (546 - 390) * 100 = 78 / 156 * 100 = 50%
        val percentage = BatteryCalculator.calculate13SPercentage(468)
        assertThat(percentage).isEqualTo(50)
    }

    @Test
    fun shouldClampToHundredWhenVoltageExceeds546() {
        val percentage = BatteryCalculator.calculate13SPercentage(600)
        assertThat(percentage).isEqualTo(100)
    }

    @Test
    fun shouldClampToZeroWhenVoltageIsBelow390() {
        val percentage = BatteryCalculator.calculate13SPercentage(300)
        assertThat(percentage).isEqualTo(0)
    }
}
