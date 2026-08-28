package co.japl.android.ev_ride_connect.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GpsUtilsTest {

    @Test
    fun shouldCalculateZeroDistanceForSameCoordinates() {
        val distance = GpsUtils.calculateDistanceKm(4.6097, -74.0817, 4.6097, -74.0817)
        assertThat(distance).isEqualTo(0.0)
    }

    @Test
    fun shouldCalculateDistanceBetweenTwoCoordinates() {
        // Bogota to Medellin approximately ~240 km
        val distance = GpsUtils.calculateDistanceKm(4.6097, -74.0817, 6.2442, -75.5812)
        assertThat(distance).isBetween(230.0, 250.0)
    }

    @Test
    fun shouldCalculateSpeedKmHCorrectly() {
        // 10 km in 15 minutes (900000 ms) -> 40 km/h
        val speed = GpsUtils.calculateSpeedKmH(10.0, 900000L)
        assertThat(speed).isEqualTo(40.0)
    }

    @Test
    fun shouldReturnZeroSpeedForZeroTimeOrDistance() {
        assertThat(GpsUtils.calculateSpeedKmH(0.0, 900000L)).isEqualTo(0.0)
        assertThat(GpsUtils.calculateSpeedKmH(10.0, 0L)).isEqualTo(0.0)
    }

    @Test
    fun shouldCalculateAverageSpeedCorrectly() {
        // 20 km in 3600 seconds (1 hour) -> 20 km/h
        val avgSpeed = GpsUtils.calculateAverageSpeed(20.0, 3600L)
        assertThat(avgSpeed).isEqualTo(20.0)
    }

    @Test
    fun shouldReturnZeroAverageSpeedForZeroTimeOrDistance() {
        assertThat(GpsUtils.calculateAverageSpeed(0.0, 3600L)).isEqualTo(0.0)
        assertThat(GpsUtils.calculateAverageSpeed(20.0, 0L)).isEqualTo(0.0)
    }
}
