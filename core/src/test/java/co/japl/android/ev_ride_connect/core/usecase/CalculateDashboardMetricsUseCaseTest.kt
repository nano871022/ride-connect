package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.usecase.api.CalculateDashboardMetricsUseCase
import co.japl.android.ev_ride_connect.core.usecase.impl.CalculateDashboardMetricsUseCaseImpl
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.Trip
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class CalculateDashboardMetricsUseCaseTest {

    private lateinit var useCase: CalculateDashboardMetricsUseCase

    @Before
    fun setUp() {
        useCase = CalculateDashboardMetricsUseCaseImpl()
    }

    @Test
    fun shouldCalculateAllMetricsFromRealData() = runTest {
        val trips = listOf(
            Trip(id = 1, distance = 5.0, batteryConsumed = 20, createTmst = 1000L, maxSpeed = 20.0),
            Trip(id = 2, distance = 12.0, batteryConsumed = 35, createTmst = 2000L, maxSpeed = 25.0),
            Trip(id = 3, distance = 8.0, batteryConsumed = 15, createTmst = 3000L, maxSpeed = 18.0)
        )
        val latestEvData = EvData(id = 1, evCode = "EV01", km = 500, batteryLevel = 85, createTmst = 4000L)
        val evConfig = EvConfig(
            id = 1,
            batteryVolts = "52V",
            batteryAmpers = "20Ah",
            maxVoltage = 54.6,
            minVoltage = 39.0
        )

        val metrics = useCase.execute(trips = trips, latestEvData = latestEvData, evConfig = evConfig)

        // Consumo basado en el último viaje (15% batería / 8 km = 1.875 Wh/km aproximado)
        // El cálculo real depende de la implementación de CalculateConsumptionUseCase
        assertThat(metrics.consumptionWhPerKm).isGreaterThan(0.0)
        // Ciclos: 500 km / 50 = 10 ciclos
        assertThat(metrics.cyclesUsed).isEqualTo(10)
        // Porcentaje óptimo basado en 10 ciclos
        assertThat(metrics.optimalBatteryPercentage).isGreaterThan(0.0)
        assertThat(metrics.optimalBatteryPercentage).isLessThanOrEqualTo(100.0)
        // Voltaje estimado basado en 85% batería
        assertThat(metrics.estimatedVoltage).isNotEmpty()
        assertThat(metrics.estimatedVoltage).endsWith("V")
        // Fecha de última carga basada en createTmst = 4000L
        assertThat(metrics.lastMaxChargeDate).isNotEmpty()
        // Día con mayor carga: viaje con 12 km
        assertThat(metrics.lastHigherChargeDayInfo).contains("12.0")
    }

    @Test
    fun shouldReturnDefaultValuesWhenNoData() = runTest {
        val metrics = useCase.execute(trips = emptyList(), latestEvData = null, evConfig = null)

        assertThat(metrics.consumptionWhPerKm).isEqualTo(0.0)
        assertThat(metrics.cyclesUsed).isEqualTo(0)
        assertThat(metrics.optimalBatteryPercentage).isEqualTo(0.0)
        assertThat(metrics.estimatedVoltage).isEqualTo("N/D")
        assertThat(metrics.lastMaxChargeDate).isEqualTo("N/D")
        assertThat(metrics.lastHigherChargeDayInfo).isEqualTo("N/D")
    }

    @Test
    fun shouldCalculateConsumptionFromLastTrip() = runTest {
        val trips = listOf(
            Trip(id = 1, distance = 10.0, batteryConsumed = 20, createTmst = 1000L)
        )
        val latestEvData = EvData(id = 1, evCode = "EV01", km = 100, batteryLevel = 90, createTmst = 2000L)
        val evConfig = EvConfig(
            id = 1,
            batteryVolts = "52V",
            batteryAmpers = "20Ah",
            maxVoltage = 54.6,
            minVoltage = 39.0
        )

        val metrics = useCase.execute(trips = trips, latestEvData = latestEvData, evConfig = evConfig)

        assertThat(metrics.consumptionWhPerKm).isGreaterThan(0.0)
        assertThat(metrics.cyclesUsed).isEqualTo(2) // 100 km / 50 = 2 ciclos
    }
}
