package co.japl.android.ev_ride_connect.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class TripTest {

    private val podamFactory = PodamFactoryImpl()

    @Test
    fun shouldInstantiateTripWithPodam() {
        val trip = podamFactory.manufacturePojo(Trip::class.java)

        assertThat(trip).isNotNull
        assertThat(trip.id).isNotNull
        assertThat(trip.timeTrip).isNotNull
        assertThat(trip.averageSpeed).isNotNull
        assertThat(trip.distance).isNotNull
        assertThat(trip.createTmst).isNotNull
    }

    @Test
    fun shouldInstantiateTripGpsWithPodam() {
        val tripGps = podamFactory.manufacturePojo(TripGps::class.java)

        assertThat(tripGps).isNotNull
        assertThat(tripGps.id).isNotNull
        assertThat(tripGps.tripId).isNotNull
        assertThat(tripGps.orderIndex).isNotNull
        assertThat(tripGps.speed).isNotNull
        assertThat(tripGps.distance).isNotNull
        assertThat(tripGps.x).isNotNull
        assertThat(tripGps.y).isNotNull
        assertThat(tripGps.createTmst).isNotNull
    }
}
