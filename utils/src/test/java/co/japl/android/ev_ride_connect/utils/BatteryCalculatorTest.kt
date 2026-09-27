package co.japl.android.ev_ride_connect.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BatteryCalculatorTest {

    @Test
    fun shouldReturnHundredWhenVoltageIs546For13S() {
        val percentage = BatteryCalculator.calculate13SPercentage(546)
        assertThat(percentage).isEqualTo(100)
    }

    @Test
    fun shouldReturnZeroWhenVoltageIs390For13S() {
        val percentage = BatteryCalculator.calculate13SPercentage(390)
        assertThat(percentage).isEqualTo(0)
    }

    @Test
    fun shouldReturnFiftyWhenVoltageIs468For13S() {
        val percentage = BatteryCalculator.calculate13SPercentage(468)
        assertThat(percentage).isEqualTo(50)
    }

    @Test
    fun shouldClampToHundredWhenVoltageExceeds546For13S() {
        val percentage = BatteryCalculator.calculate13SPercentage(600)
        assertThat(percentage).isEqualTo(100)
    }

    @Test
    fun shouldClampToZeroWhenVoltageIsBelow390For13S() {
        val percentage = BatteryCalculator.calculate13SPercentage(300)
        assertThat(percentage).isEqualTo(0)
    }

    @Test
    fun shouldCalculateDynamicPercentageCorrectly() {
        val percentageMid = BatteryCalculator.calculatePercentage(50.0, 40.0, 60.0)
        assertThat(percentageMid).isEqualTo(50)

        val percentageMin = BatteryCalculator.calculatePercentage(40.0, 40.0, 60.0)
        assertThat(percentageMin).isEqualTo(0)

        val percentageMax = BatteryCalculator.calculatePercentage(60.0, 40.0, 60.0)
        assertThat(percentageMax).isEqualTo(100)

        val percentageAbove = BatteryCalculator.calculatePercentage(65.0, 40.0, 60.0)
        assertThat(percentageAbove).isEqualTo(100)

        val percentageBelow = BatteryCalculator.calculatePercentage(35.0, 40.0, 60.0)
        assertThat(percentageBelow).isEqualTo(0)
    }

    @Test
    fun shouldHandleInvalidMinMaxGracefully() {
        val percentage = BatteryCalculator.calculatePercentage(50.0, 60.0, 40.0)
        assertThat(percentage).isEqualTo(0)
    }
}
