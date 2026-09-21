package co.com.japl.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.com.japl.ui.theme.MaterialThemeComposeUI
import co.japl.android.ev_ride_connect.ui.R

@Composable
fun MapHudCard(
    gpsIntervalSeconds: Long,
    onGpsIntervalSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    satellitesCount: Int = -1,
    precisionMt: Double = 0.0,
    isLiveTelemetry: Boolean = false,
    points: List<MapPoint> = emptyList(),
    sampleTimestamps: List<Long> = emptyList(),
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(600.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(8.dp),
        ) {
            SatellitesCount(precisionMt, satellitesCount)

            LiveTelemetry(isLiveTelemetry)
            val options = listOf(15L, 30L, 60L, 120L, 300L).map { sec ->
                SegmentOption(
                    sec,
                    stringResource(R.string.trip_interval_seconds, sec.toInt())
                )
            }
            SegmentedChipGroup(
                options = options,
                selectedOption = gpsIntervalSeconds,
                onOptionSelected = onGpsIntervalSelected
            )

            SampleTelemetry(sampleTimestamps)

            Box(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            ) {
                TripMapView(
                    points = points,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SampleTelemetry(sampleTimestamps: List<Long>){
    if (sampleTimestamps.isNotEmpty()) {
        Text(
            text = stringResource(
                R.string.trip_sample_count_label,
                sampleTimestamps.size
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun LiveTelemetry(isLiveTelemetry: Boolean){
    if (isLiveTelemetry) {
        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                        alpha = 0.9f
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PulsingBeaconDot(
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            Text(
                text = stringResource(R.string.trip_live_telemetry),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SatellitesCount(precisionMt: Double, satellitesCount: Int){
    if(satellitesCount > 0) {
        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                        alpha = 0.9f
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.trip_gps_accuracy, precisionMt, satellitesCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PulsingBeaconDot(color: Color) {
    val scale = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = scale.value))
    )
}


@Preview
@Composable
internal fun HUBPreview(){
    MaterialThemeComposeUI() {
        MapHudCard(
            gpsIntervalSeconds = 20,
            onGpsIntervalSelected = { },
            modifier = Modifier,
            satellitesCount = -1,
            precisionMt = 0.0,
            isLiveTelemetry = false,
            points = emptyList(),
            sampleTimestamps = emptyList(),
        )
    }
}