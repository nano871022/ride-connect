package co.japl.android.ev_ride_connect.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class ScooterStateTest {

    private val podamFactory = PodamFactoryImpl()

    @Test
    fun shouldInstantiateScooterStateWithPodam() {
        val scooterState = podamFactory.manufacturePojo(ScooterState::class.java)

        assertThat(scooterState).isNotNull
        assertThat(scooterState.speedMode).isNotNull
        assertThat(scooterState.currentSpeed).isNotNull
        assertThat(scooterState.realtimeVoltage).isNotNull
        assertThat(scooterState.batteryPercentage).isNotNull
        assertThat(scooterState.totalOdometer).isNotNull
    }

    @Test
    fun shouldCreateScooterStateWithGivenValues() {
        val state = ScooterState(
            isLocked = true,
            speedMode = 2,
            currentSpeed = 25,
            realtimeVoltage = 520,
            batteryPercentage = 80,
            totalOdometer = 120,
            isLightOn = true
        )

        assertThat(state.isLocked).isTrue()
        assertThat(state.speedMode).isEqualTo(2)
        assertThat(state.currentSpeed).isEqualTo(25)
        assertThat(state.realtimeVoltage).isEqualTo(520)
        assertThat(state.batteryPercentage).isEqualTo(80)
        assertThat(state.totalOdometer).isEqualTo(120)
        assertThat(state.isLightOn).isTrue()
    }
}
