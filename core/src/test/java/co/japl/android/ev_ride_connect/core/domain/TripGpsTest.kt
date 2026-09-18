package co.japl.android.ev_ride_connect.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class TripGpsTest {

    private val podamFactory = PodamFactoryImpl()

    @Test
    fun shouldInstantiateTripGpsWithPodam() {
        val tripGps = podamFactory.manufacturePojo(TripGps::class.java)

        assertThat(tripGps).isNotNull
        assertThat(tripGps.motionState).isNotNull
    }

    @Test
    fun shouldDefaultMotionStateToStopped() {
        val tripGps = TripGps()

        assertThat(tripGps.motionState).isEqualTo(MotionState.STOPPED)
    }

    @Test
    fun shouldContainAllDefinedMotionStates() {
        val values = MotionState.values().map { it.name }

        assertThat(values).containsExactlyInAnyOrder("MOVING", "BRAKING", "ACCELERATING", "STOPPED")
    }
}
