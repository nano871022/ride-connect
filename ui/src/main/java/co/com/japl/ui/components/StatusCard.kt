package co.com.japl.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.japl.android.ev_ride_connect.ui.R

@Composable
fun StatusCard(
    title: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    statusMessage: String? = null,
    isSuccessStatus: Boolean = true,
    lastUpdatedText: String? = null,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    elevationDp: Dp = 4.dp
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevationDp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            lastUpdatedText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(text = stringResource(R.string.status_loading))
                }
            } else {
                actionButtonText?.let { btnText ->
                    onActionClick?.let { onClick ->
                        Button(
                            onClick = onClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = btnText)
                        }
                    }
                }
            }

            statusMessage?.let { msg ->
                val color: Color = if (isSuccessStatus) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
                Text(
                    text = msg,
                    color = color,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
