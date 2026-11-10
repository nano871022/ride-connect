package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.usecase.api.CalculateMaxSpeedUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateMaxSpeedUseCaseImpl
import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class CalculateMaxSpeedUseCaseTest {

    private lateinit var useCase: CalculateMaxSpeedUseCase

    @Before
    fun setUp() {
        useCase = CalculateMaxSpeedUseCaseImpl()
    }

    @Test
    fun shouldCalculateMaxSpeedFromGpsPoints() = runTest {
        val gpsPoints = listOf(
            TripGps(orderIndex = 1, speed = 0.0),
            TripGps(orderIndex = 2, speed = 12.5),
            TripGps(orderIndex = 3, speed = 25.0),
            TripGps(orderIndex = 4, speed = 18.0),
            TripGps(orderIndex = 5, speed = 22.0)
        )

        val maxSpeed = useCase.execute(gpsPoints)

        assertThat(maxSpeed).isEqualTo(25.0)
    }

    @Test
    fun shouldReturnZeroWhenNoGpsPoints() = runTest {
        val maxSpeed = useCase.execute(emptyList())

        assertThat(maxSpeed).isEqualTo(0.0)
    }

    @Test
    fun shouldReturnZeroWhenAllPointsStopped() = runTest {
        val gpsPoints = listOf(
            TripGps(orderIndex = 1, speed = 0.0),
            TripGps(orderIndex = 2, speed = 0.0)
        )

        val maxSpeed = useCase.execute(gpsPoints)

        assertThat(maxSpeed).isEqualTo(0.0)
    }
}
