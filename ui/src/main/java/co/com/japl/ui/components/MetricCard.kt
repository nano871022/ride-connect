package co.com.japl.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MetricCard(
    value: String,
    title: String? = null,
    unit: String? = null,
    modifier: Modifier = Modifier,
    valueFontSize: TextUnit = 64.sp,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    elevationDp: Dp = 4.dp
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevationDp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text(
                text = value,
                fontSize = valueFontSize,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            unit?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun DualMetricCard(
    primaryTitle: String,
    primaryValue: String,
    secondaryTitle: String,
    secondaryValue: String,
    modifier: Modifier = Modifier,
    elevationDp: Dp = 2.dp
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevationDp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = primaryTitle,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = primaryValue,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = secondaryTitle,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = secondaryValue,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
