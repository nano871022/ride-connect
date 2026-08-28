package co.japl.android.ev_ride_connect.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GpsUtils {

    private const val EARTH_RADIUS_KM = 6371.0

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == lat2 && lon1 == lon2) return 0.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    fun calculateSpeedKmH(distanceKm: Double, timeDifferenceMs: Long): Double {
        if (timeDifferenceMs <= 0 || distanceKm <= 0.0) return 0.0
        val timeInHours = timeDifferenceMs / 3_600_000.0
        return distanceKm / timeInHours
    }

    fun calculateAverageSpeed(totalDistanceKm: Double, totalTimeSeconds: Long): Double {
        if (totalTimeSeconds <= 0 || totalDistanceKm <= 0.0) return 0.0
        val timeInHours = totalTimeSeconds / 3600.0
        return totalDistanceKm / timeInHours
    }
}
