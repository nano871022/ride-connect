package co.com.japl.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SegmentOption<T>(
    val value: T,
    val label: String
)

@Composable
fun <T> SegmentedButtonGroup(
    options: List<SegmentOption<T>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    spacingDp: Dp = 8.dp
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacingDp)
    ) {
        options.forEach { option ->
            val isSelected = option.value == selectedOption
            if (isSelected) {
                Button(
                    onClick = { onOptionSelected(option.value) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(option.label)
                }
            } else {
                OutlinedButton(
                    onClick = { onOptionSelected(option.value) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(option.label)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SegmentedChipGroup(
    options: List<SegmentOption<T>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    spacingDp: Dp = 8.dp
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacingDp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option.value == selectedOption,
                onClick = { onOptionSelected(option.value) },
                label = { Text(option.label) }
            )
        }
    }
}
