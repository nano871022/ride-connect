package co.japl.android.ev_ride_connect.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.com.japl.ui.components.DualMetricCard
import co.com.japl.ui.components.MapPoint
import co.com.japl.ui.components.TripMapView
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.TripViewModel
import co.japl.android.ev_ride_connect.core.domain.Trip
import co.japl.android.ev_ride_connect.core.domain.TripGps
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import co.japl.android.ev_ride_connect.utils.DateUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    viewModel: TripViewModel,
    navigator: AppNavigator? = null,
    modifier: Modifier = Modifier
) {
    val selectedTripId by (navigator?.selectedTripId ?: remember { kotlinx.coroutines.flow.MutableStateFlow(null) }).collectAsState()

    LaunchedEffect(selectedTripId) {
        val tripId = selectedTripId
        if (tripId != null) {
            viewModel.loadTripDetail(tripId)
        }
    }

    val selectedTripDetail by viewModel.selectedTripDetail.collectAsState()

    val tripDetailPair = selectedTripDetail
    val currentLocale = LocalConfiguration.current.locales[0]

    if (tripDetailPair == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.trip_empty_history),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        val (trip, gpsPoints) = tripDetailPair
        val mapPoints = gpsPoints.map {
            MapPoint(
                latitude = it.x,
                longitude = it.y,
                title = stringResource(R.string.trip_point_order, it.orderIndex),
                snippet = "${stringResource(R.string.trip_avg_speed_label)}: ${String.format(currentLocale, "%.1f km/h", it.speed)}"
            )
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                TripMapView(points = mapPoints, modifier = Modifier.fillMaxSize())
            }

            TripSummaryCard(trip = trip, gpsPointCnt = gpsPoints.size.toShort())

            Text(
                text = stringResource(R.string.trip_detail_points_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            gpsPoints.forEach { sample ->
                GpsSampleItem(sample = sample)
            }
        }
    }
}

@Composable
private fun TripSummaryCard(trip: Trip, gpsPointCnt: Short = 0) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row {
                Text(
                    text = DateUtils.formatTimestamp(trip.createTmst),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${ gpsPointCnt }",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = "${stringResource(R.string.trip_timer_label)}: ${DateUtils.formatDurationSeconds(trip.timeTrip)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            val currentLocale = LocalConfiguration.current.locales[0]
            DualMetricCard(
                primaryTitle = stringResource(R.string.trip_distance_label),
                primaryValue = String.format(currentLocale, "%.2f km", trip.distance),
                secondaryTitle = stringResource(R.string.trip_avg_speed_label),
                secondaryValue = String.format(currentLocale, "%.1f km/h", trip.averageSpeed)
            )
        }
    }
}

@Composable
private fun GpsSampleItem(sample: TripGps) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.trip_point_order, sample.orderIndex),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = DateUtils.formatTimestamp(sample.createTmst),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = stringResource(R.string.trip_point_coords, sample.x, sample.y),
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.trip_point_speed, sample.speed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.trip_point_distance, sample.distance),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
