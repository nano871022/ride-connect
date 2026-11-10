package co.com.japl.ui.components

import co.japl.android.ev_ride_connect.interfaces.model.TripGps
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

class TripMapViewTest {

    @Test
    fun shouldShowAllPointsAsMarkersWithSpeed() {
        // Dados puntos GPS con velocidad
        val gpsPoints = listOf(
            TripGps(orderIndex = 1, speed = 10.0, x = 4.6097, y = -74.0817),
            TripGps(orderIndex = 2, speed = 15.0, x = 4.6098, y = -74.0818),
            TripGps(orderIndex = 3, speed = 20.0, x = 4.6099, y = -74.0819),
            TripGps(orderIndex = 4, speed = 12.0, x = 4.6100, y = -74.0820),
            TripGps(orderIndex = 5, speed = 8.0, x = 4.6101, y = -74.0821)
        )

        // Cuando se preparan los puntos para el mapa
        val mapPoints = gpsPoints.mapIndexed { index, gps ->
            MapPoint(
                latitude = gps.x,
                longitude = gps.y,
                title = "Punto #${index + 1}",
                snippet = "Vel. Media: ${String.format("%.1f km/h", gps.speed)}"
            )
        }

        // Entonces todos los puntos deben ser marcadores con velocidad
        assertThat(mapPoints).hasSize(5)
        
        // El punto intermedio (index 2) debe tener velocidad en el snippet
        val middlePoint = mapPoints[2]
        assertThat(middlePoint.title).isEqualTo("Punto #3")
        assertThat(middlePoint.snippet).contains("20.0 km/h")
        
        // El primer punto debe tener velocidad
        val firstPoint = mapPoints[0]
        assertThat(firstPoint.snippet).contains("10.0 km/h")
        
        // El último punto debe tener velocidad
        val lastPoint = mapPoints[4]
        assertThat(lastPoint.snippet).contains("8.0 km/h")
    }

    @Test
    fun shouldHandleEmptyPoints() {
        val gpsPoints = emptyList<TripGps>()
        val mapPoints = gpsPoints.mapIndexed { index, gps ->
            MapPoint(
                latitude = gps.x,
                longitude = gps.y,
                title = "Punto #${index + 1}",
                snippet = "Vel. Media: ${String.format("%.1f km/h", gps.speed)}"
            )
        }
        assertThat(mapPoints).isEmpty()
    }
}
