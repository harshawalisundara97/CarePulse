package com.carepulse.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightBrandScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = CardSurface,
    primaryContainer = AccentContainerLight,
    onPrimaryContainer = TextPrimary,
    secondary = TextPrimary,
    onSecondary = CardSurface,
    secondaryContainer = SurfaceLow,
    onSecondaryContainer = TextPrimary,
    tertiary = WarningAmber,
    onTertiary = CardSurface,
    background = Background,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLow,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLow = SurfaceLow,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
    outline = BorderLine,
    error = DangerRed,
    onError = CardSurface
)

private val DarkBrandScheme = darkColorScheme(
    primary = AccentPrimaryDark,
    onPrimary = DarkBackground,
    primaryContainer = AccentContainerDark,
    onPrimaryContainer = DarkOnSurface,
    secondary = DarkOnSurface,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceLow,
    onSecondaryContainer = DarkOnSurface,
    tertiary = WarningAmber,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceLow,
    onSurfaceVariant = DarkOnSurfaceVar,
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceHighest,
    outline = DarkBorder,
    error = DangerRed,
    onError = CardSurface
)

private val CarePulseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(Radii.Input),
    large = RoundedCornerShape(Radii.Button),
    extraLarge = RoundedCornerShape(Radii.Card)
)

@Composable
fun CarePulseTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themePreference = remember { ThemePreference(context) }
    val mode = themePreference.themeMode.collectAsState(initial = ThemeMode.SYSTEM).value

    val useDark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDark -> DarkBrandScheme
        else -> LightBrandScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CarePulseTypography,
        shapes = CarePulseShapes,
        content = content
    )
}
