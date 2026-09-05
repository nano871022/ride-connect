package co.com.japl.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val DarkColorScheme = darkColorScheme(
    primary = CyberPulsePrimary,
    onPrimary = CyberPulseOnPrimary,
    primaryContainer = CyberPulsePrimaryContainer,
    onPrimaryContainer = CyberPulseOnPrimaryContainer,
    inversePrimary = CyberPulseInversePrimary,
    secondary = CyberPulseSecondary,
    onSecondary = CyberPulseOnSecondary,
    secondaryContainer = CyberPulseSecondaryContainer,
    onSecondaryContainer = CyberPulseOnSecondaryContainer,
    tertiary = CyberPulseTertiary,
    onTertiary = CyberPulseOnTertiary,
    tertiaryContainer = CyberPulseTertiaryContainer,
    onTertiaryContainer = CyberPulseOnTertiaryContainer,
    background = CyberPulseBackground,
    onBackground = CyberPulseOnBackground,
    surface = CyberPulseSurface,
    onSurface = CyberPulseOnSurface,
    surfaceVariant = CyberPulseSurfaceVariant,
    onSurfaceVariant = CyberPulseOnSurfaceVariant,
    surfaceTint = CyberPulseSurfaceTint,
    inverseSurface = CyberPulseInverseSurface,
    inverseOnSurface = CyberPulseInverseOnSurface,
    error = CyberPulseError,
    onError = CyberPulseOnError,
    errorContainer = CyberPulseErrorContainer,
    onErrorContainer = CyberPulseOnErrorContainer,
    outline = CyberPulseOutline,
    outlineVariant = CyberPulseOutlineVariant,
    scrim = CyberPulseSurfaceContainerLowest,
    surfaceDim = CyberPulseSurfaceDim,
    surfaceBright = CyberPulseSurfaceBright,
    surfaceContainerLowest = CyberPulseSurfaceContainerLowest,
    surfaceContainerLow = CyberPulseSurfaceContainerLow,
    surfaceContainer = CyberPulseSurfaceContainer,
    surfaceContainerHigh = CyberPulseSurfaceContainerHigh,
    surfaceContainerHighest = CyberPulseSurfaceContainerHighest
)

val LightColorScheme = lightColorScheme(
    primary = CyberPulsePrimary,
    onPrimary = CyberPulseOnPrimary,
    primaryContainer = CyberPulsePrimaryContainer,
    onPrimaryContainer = CyberPulseOnPrimaryContainer,
    inversePrimary = CyberPulseInversePrimary,
    secondary = CyberPulseSecondary,
    onSecondary = CyberPulseOnSecondary,
    secondaryContainer = CyberPulseSecondaryContainer,
    onSecondaryContainer = CyberPulseOnSecondaryContainer,
    tertiary = CyberPulseTertiary,
    onTertiary = CyberPulseOnTertiary,
    tertiaryContainer = CyberPulseTertiaryContainer,
    onTertiaryContainer = CyberPulseOnTertiaryContainer,
    background = CyberPulseBackground,
    onBackground = CyberPulseOnBackground,
    surface = CyberPulseSurface,
    onSurface = CyberPulseOnSurface,
    surfaceVariant = CyberPulseSurfaceVariant,
    onSurfaceVariant = CyberPulseOnSurfaceVariant,
    surfaceTint = CyberPulseSurfaceTint,
    inverseSurface = CyberPulseInverseSurface,
    inverseOnSurface = CyberPulseInverseOnSurface,
    error = CyberPulseError,
    onError = CyberPulseOnError,
    errorContainer = CyberPulseErrorContainer,
    onErrorContainer = CyberPulseOnErrorContainer,
    outline = CyberPulseOutline,
    outlineVariant = CyberPulseOutlineVariant,
    scrim = CyberPulseSurfaceContainerLowest,
    surfaceDim = CyberPulseSurfaceDim,
    surfaceBright = CyberPulseSurfaceBright,
    surfaceContainerLowest = CyberPulseSurfaceContainerLowest,
    surfaceContainerLow = CyberPulseSurfaceContainerLow,
    surfaceContainer = CyberPulseSurfaceContainer,
    surfaceContainerHigh = CyberPulseSurfaceContainerHigh,
    surfaceContainerHighest = CyberPulseSurfaceContainerHighest
)

@Composable
fun MaterialThemeComposeUI(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
