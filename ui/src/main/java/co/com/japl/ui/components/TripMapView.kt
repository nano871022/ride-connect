package co.com.japl.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

data class MapPoint(
    val latitude: Double,
    val longitude: Double,
    val title: String? = null,
    val snippet: String? = null
)

@Composable
fun TripMapView(
    points: List<MapPoint>,
    modifier: Modifier = Modifier,
    polylineColor: Color = Color(0xFF00E5FF)
) {
    val defaultLocation = LatLng(4.6097, -74.0817)
    val initialPos = points.lastOrNull()?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 15f)
    }

    LaunchedEffect(points.size) {
        val lastPoint = points.lastOrNull()
        if (lastPoint != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(LatLng(lastPoint.latitude, lastPoint.longitude))
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        val latLngs = points.map { LatLng(it.latitude, it.longitude) }
        if (latLngs.size >= 2) {
            Polyline(
                points = latLngs,
                color = polylineColor,
                width = 8f
            )
        }

        points.forEachIndexed { index, point ->
            Marker(
                state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                title = point.title ?: "Punto #${index + 1}",
                snippet = point.snippet
            )
        }
    }
}
