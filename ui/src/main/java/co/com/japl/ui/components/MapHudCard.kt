package co.com.japl.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import co.japl.android.ev_ride_connect.ui.R

@Composable
fun MapHudCard(
    gpsIntervalSeconds: Long,
    onGpsIntervalSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    satellitesCount: Int = 14,
    isLiveTelemetry: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SimulatedMapCanvas(
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.80f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
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
                            text = stringResource(R.string.trip_gps_accuracy, satellitesCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
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

                val options = listOf(15L, 30L, 60L, 120L, 300L).map { sec ->
                    SegmentOption(sec, stringResource(R.string.trip_interval_seconds, sec.toInt()))
                }

                SegmentedChipGroup(
                    options = options,
                    selectedOption = gpsIntervalSeconds,
                    onOptionSelected = onGpsIntervalSelected
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Map Layers",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "My Location",
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatedMapCanvas(modifier: Modifier = Modifier) {
    val cyanColor = MaterialTheme.colorScheme.primaryContainer
    val emeraldColor = MaterialTheme.colorScheme.secondaryContainer

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(color = Color(0xFF111318))

        val gridStep = 40.dp.toPx()
        for (x in 0..(width / gridStep).toInt()) {
            drawLine(
                color = Color.White.copy(alpha = 0.03f),
                start = Offset(x * gridStep, 0f),
                end = Offset(x * gridStep, height),
                strokeWidth = 1f
            )
        }
        for (y in 0..(height / gridStep).toInt()) {
            drawLine(
                color = Color.White.copy(alpha = 0.03f),
                start = Offset(0f, y * gridStep),
                end = Offset(width, y * gridStep),
                strokeWidth = 1f
            )
        }

        val streetPath = Path().apply {
            moveTo(0f, height * 0.7f)
            quadraticBezierTo(width * 0.3f, height * 0.5f, width * 0.6f, height * 0.65f)
            lineTo(width, height * 0.4f)
        }
        drawPath(
            path = streetPath,
            color = Color.White.copy(alpha = 0.08f),
            style = Stroke(width = 12f)
        )

        val routePath = Path().apply {
            moveTo(width * 0.12f, height * 0.8f)
            cubicTo(
                width * 0.25f, height * 0.75f,
                width * 0.3f, height * 0.55f,
                width * 0.4f, height * 0.52f
            )
            cubicTo(
                width * 0.5f, height * 0.49f,
                width * 0.55f, height * 0.65f,
                width * 0.68f, height * 0.46f
            )
            cubicTo(
                width * 0.76f, height * 0.35f,
                width * 0.75f, height * 0.23f,
                width * 0.83f, height * 0.17f
            )
        }

        drawPath(
            path = routePath,
            color = cyanColor.copy(alpha = 0.35f),
            style = Stroke(width = 16f)
        )
        drawPath(
            path = routePath,
            color = cyanColor,
            style = Stroke(width = 6f)
        )
        drawPath(
            path = routePath,
            color = Color.White,
            style = Stroke(width = 2f)
        )

        val origin = Offset(width * 0.12f, height * 0.8f)
        drawCircle(color = emeraldColor, radius = 10f, center = origin)
        drawCircle(color = emeraldColor.copy(alpha = 0.4f), radius = 20f, center = origin)

        val currentLoc = Offset(width * 0.83f, height * 0.17f)
        drawCircle(color = cyanColor.copy(alpha = 0.3f), radius = 28f, center = currentLoc)
        drawCircle(color = cyanColor, radius = 14f, center = currentLoc)
        drawCircle(color = Color(0xFF0C0E12), radius = 8f, center = currentLoc)
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
