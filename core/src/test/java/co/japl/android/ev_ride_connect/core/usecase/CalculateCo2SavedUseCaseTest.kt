package co.japl.android.ev_ride_connect.core.usecase

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class CalculateCo2SavedUseCaseTest {

    private lateinit var useCase: CalculateCo2SavedUseCase

    @Before
    fun setUp() {
        useCase = CalculateCo2SavedUseCase()
    }

    @Test
    fun execute_tenKm_returns1200Grams() {
        val result = useCase.execute(distanceKm = 10.0, gramsPerKm = 120.0)
        assertThat(result).isEqualTo(1200.0)
    }

    @Test
    fun execute_zeroDistance_returnsZero() {
        val result = useCase.execute(distanceKm = 0.0)
        assertThat(result).isEqualTo(0.0)
    }
}
