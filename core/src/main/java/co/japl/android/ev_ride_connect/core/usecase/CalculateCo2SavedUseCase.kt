package co.japl.android.ev_ride_connect.core.usecase

import javax.inject.Inject

class CalculateCo2SavedUseCase @Inject constructor() {

    fun execute(distanceKm: Double, gramsPerKm: Double = 120.0): Double {
        if (distanceKm <= 0.0) return 0.0
        val gramsSaved = distanceKm * gramsPerKm
        return Math.round(gramsSaved * 10.0) / 10.0
    }
}
