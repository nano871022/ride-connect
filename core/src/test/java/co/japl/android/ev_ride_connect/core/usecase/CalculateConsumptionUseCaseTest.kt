package co.japl.android.ev_ride_connect.core.usecase

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class CalculateConsumptionUseCaseTest {

    private lateinit var useCase: CalculateConsumptionUseCase

    @Before
    fun setUp() {
        useCase = CalculateConsumptionUseCase()
    }

    @Test
    fun execute_validTrip_returnsWhPerKm() {
        val result = useCase.execute(
            batteryConsumedPercentage = 10,
            batteryVoltage = 52.0,
            batteryAmperes = 20.0,
            distanceKm = 10.0
        )
        assertThat(result).isEqualTo(10.4)
    }

    @Test
    fun execute_zeroDistance_returnsZero() {
        val result = useCase.execute(
            batteryConsumedPercentage = 10,
            batteryVoltage = 52.0,
            batteryAmperes = 20.0,
            distanceKm = 0.0
        )
        assertThat(result).isEqualTo(0.0)
    }
}
