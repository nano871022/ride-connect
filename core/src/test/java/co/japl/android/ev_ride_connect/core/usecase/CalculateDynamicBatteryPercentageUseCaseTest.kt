package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.BatteryMode
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CalculateDynamicBatteryPercentageUseCaseTest {

    private val useCase = CalculateDynamicBatteryPercentageUseCase()

    @Test
    fun shouldReturnDirectPercentageWhenModeIsPercentage() {
        val result = useCase.execute(75.0, BatteryMode.PERCENTAGE, 39.0, 54.6)
        assertThat(result).isEqualTo(75.toShort())
    }

    @Test
    fun shouldClampPercentageWhenModeIsPercentage() {
        val resultAbove = useCase.execute(150.0, BatteryMode.PERCENTAGE, 39.0, 54.6)
        assertThat(resultAbove).isEqualTo(100.toShort())

        val resultBelow = useCase.execute(-10.0, BatteryMode.PERCENTAGE, 39.0, 54.6)
        assertThat(resultBelow).isEqualTo(0.toShort())
    }

    @Test
    fun shouldCalculatePercentageFromVoltageWhenModeIsVoltage() {
        val result = useCase.execute(46.8, BatteryMode.VOLTAGE, 39.0, 54.6)
        assertThat(result).isEqualTo(50.toShort())
    }

    @Test
    fun shouldCalculatePercentageFromEvConfig() {
        val config = EvConfig(
            batteryMode = BatteryMode.VOLTAGE,
            minVoltage = 40.0,
            maxVoltage = 60.0
        )
        val result = useCase.execute(50.0, config)
        assertThat(result).isEqualTo(50.toShort())
    }
}
