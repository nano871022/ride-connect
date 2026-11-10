package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.usecase.api.CalculateConsumptionChartDataUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateConsumptionChartDataUseCaseImpl
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class CalculateConsumptionChartDataUseCaseTest {

    private lateinit var useCase: CalculateConsumptionChartDataUseCase

    @Before
    fun setUp() {
        useCase = CalculateConsumptionChartDataUseCaseImpl()
    }

    @Test
    fun shouldGenerateChartDataFromRealTrips() = runTest {
        val trips = listOf(
            Trip(id = 1, distance = 5.0, batteryConsumed = 20, createTmst = 1000L),
            Trip(id = 2, distance = 12.0, batteryConsumed = 35, createTmst = 2000L),
            Trip(id = 3, distance = 8.0, batteryConsumed = 15, createTmst = 3000L),
            Trip(id = 4, distance = 15.0, batteryConsumed = 40, createTmst = 4000L),
            Trip(id = 5, distance = 3.0, batteryConsumed = 10, createTmst = 5000L)
        )

        val chartData = useCase.execute(trips = trips)

        // Debe generar exactamente 7 puntos para el gráfico semanal
        assertThat(chartData).hasSize(7)
        // Todos los valores deben ser >= 0
        chartData.forEach { value ->
            assertThat(value).isGreaterThanOrEqualTo(0f)
        }
    }

    @Test
    fun shouldReturnZerosWhenNoTrips() = runTest {
        val chartData = useCase.execute(trips = emptyList())

        assertThat(chartData).hasSize(7)
        chartData.forEach { value ->
            assertThat(value).isEqualTo(0f)
        }
    }
}
