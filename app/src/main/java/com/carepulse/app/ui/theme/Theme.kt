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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightBrandScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = AccentPrimary.copy(alpha = 0.14f),
    onPrimaryContainer = AccentPressed,
    secondary = TextPrimary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = GlassFillSubtle,
    onSecondaryContainer = TextPrimary,
    tertiary = StatusOnDuty,
    onTertiary = Color(0xFFFFFFFF),
    background = Background,
    onBackground = TextPrimary,
    surface = GlassFill,
    onSurface = TextPrimary,
    surfaceVariant = GlassFillSubtle,
    onSurfaceVariant = TextMuted,
    surfaceContainerLow = GroundDeep,
    surfaceContainerHigh = GlassFill,
    surfaceContainerHighest = Color(0xFFFFFFFF),
    outline = Rule,
    error = DangerRed,
    onError = Color(0xFFFFFFFF)
)

private val DarkBrandScheme = darkColorScheme(
    primary = AccentPrimaryDark,
    onPrimary = BackgroundDark,
    primaryContainer = AccentPrimaryDark.copy(alpha = 0.18f),
    onPrimaryContainer = AccentPressedDark,
    secondary = TextPrimaryDark,
    onSecondary = BackgroundDark,
    secondaryContainer = GlassFillSubtleDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = StatusOnDuty,
    onTertiary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = GlassFillDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = GlassFillSubtleDark,
    onSurfaceVariant = TextMutedDark,
    surfaceContainerLow = GroundDeepDark,
    surfaceContainerHigh = GlassFillDark,
    surfaceContainerHighest = Color(0xFF1C1A19),
    outline = RuleDark,
    error = DangerRed,
    onError = Color(0xFFFFFFFF)
)

private val CarePulseShapes = Shapes(
    extraSmall = RoundedCornerShape(Radii.IconButton),
    small = RoundedCornerShape(Radii.Input),
    medium = RoundedCornerShape(Radii.Button),
    large = RoundedCornerShape(Radii.Card),
    extraLarge = RoundedCornerShape(Radii.CardLarge)
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

    // Glass surfaces (Glass.kt) must key off the app's *resolved* dark/light flag, not the raw
    // system setting — isSystemInDarkTheme() disagrees with `useDark` whenever the user has
    // overridden the theme (ThemeMode.LIGHT/DARK) away from ThemeMode.SYSTEM. Exposing it here
    // keeps every glass consumer in sync with whatever ColorScheme MaterialTheme resolves below.
    CompositionLocalProvider(
        LocalIsDarkTheme provides useDark,
        LocalGlassEnabled provides true,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CarePulseTypography,
            shapes = CarePulseShapes,
            content = content
        )
    }
}
