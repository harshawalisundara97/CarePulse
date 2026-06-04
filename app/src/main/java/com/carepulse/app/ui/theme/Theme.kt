package com.carepulse.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CarePulseColors = lightColorScheme(
    primary = TealAccent,
    onPrimary = CardSurface,
    primaryContainer = TealLight,
    onPrimaryContainer = NavyPrimary,
    secondary = NavyPrimary,
    onSecondary = CardSurface,
    secondaryContainer = NavyDark,
    onSecondaryContainer = CardSurface,
    tertiary = WarningAmber,
    onTertiary = CardSurface,
    background = Background,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = Background,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = CardSurface
)

private val CarePulseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun CarePulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CarePulseColors,
        typography = CarePulseTypography,
        shapes = CarePulseShapes,
        content = content
    )
}
