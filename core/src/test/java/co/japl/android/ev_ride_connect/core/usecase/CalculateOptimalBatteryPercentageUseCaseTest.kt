package co.japl.android.ev_ride_connect.core.usecase

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class CalculateOptimalBatteryPercentageUseCaseTest {

    private lateinit var useCase: CalculateOptimalBatteryPercentageUseCase

    @Before
    fun setUp() {
        useCase = CalculateOptimalBatteryPercentageUseCase()
    }

    @Test
    fun execute_zeroCycles_returns100Percent() {
        val result = useCase.execute(usedCycles = 0, maxCycles = 500)
        assertThat(result).isEqualTo(100.0)
    }

    @Test
    fun execute_halfCycles_returnsExpectedOptimalPercentage() {
        val result = useCase.execute(usedCycles = 250, maxCycles = 500, maxDegradation = 20.0)
        assertThat(result).isEqualTo(90.0)
    }

    @Test
    fun execute_maxCycles_returns80Percent() {
        val result = useCase.execute(usedCycles = 500, maxCycles = 500, maxDegradation = 20.0)
        assertThat(result).isEqualTo(80.0)
    }

    @Test
    fun execute_exceedsMaxCycles_coercesTo80Percent() {
        val result = useCase.execute(usedCycles = 600, maxCycles = 500, maxDegradation = 20.0)
        assertThat(result).isEqualTo(80.0)
    }
}
