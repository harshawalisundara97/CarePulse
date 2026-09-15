package com.carepulse.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

/**
 * Gates backdrop blur app-wide. False on devices/preferences where blur should be skipped —
 * every consumer of [glassCard] MUST still render correctly with this false, via the opaque
 * fallback branch. Provided by [CarePulseTheme].
 */
val LocalGlassEnabled: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

/**
 * The effective dark/light flag the app is rendering with, resolved by [CarePulseTheme] from the
 * user's persisted theme preference (`ThemeMode.SYSTEM | LIGHT | DARK`) — NOT from
 * `isSystemInDarkTheme()`. Glass surfaces must key off this instead of the raw system setting, or
 * they render the wrong palette whenever the user's in-app choice disagrees with the OS setting
 * (e.g. app forced to LIGHT while the system is in dark mode).
 */
val LocalIsDarkTheme: ProvidableCompositionLocal<Boolean> = compositionLocalOf { false }

/**
 * Vertical space the floating glass bottom nav occupies at the bottom of a tab screen (bar
 * height + its bottom margin + the system navigation-bar inset), or 0.dp when no bottom nav is
 * showing. Provided by `CarePulseNavGraph`. Tab content draws edge-to-edge *behind* the bar so
 * the bar has something to blur; scroll containers add this as bottom content padding so their
 * last item can still scroll clear of it. See [tabContentWindowInsets] and [withoutBottom].
 */
val LocalBottomNavClearance: ProvidableCompositionLocal<Dp> = compositionLocalOf { 0.dp }

/**
 * Scaffold `contentWindowInsets` for a tab screen: the default insets with the bottom replaced
 * by [LocalBottomNavClearance], so the Scaffold's content padding (and its FAB position) clear
 * the floating nav without also adding the navigation-bar inset a second time. Falls back to the
 * Material defaults when no bottom nav is showing.
 */
@Composable
fun tabContentWindowInsets(): WindowInsets {
    val clearance = LocalBottomNavClearance.current
    return if (clearance == 0.dp) {
        ScaffoldDefaults.contentWindowInsets
    } else {
        ScaffoldDefaults.contentWindowInsets
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            .add(WindowInsets(bottom = clearance))
    }
}

/**
 * These padding values without their bottom edge. Apply this to a scroll container's outer
 * modifier and pass `calculateBottomPadding()` as its *content* padding instead, so items scroll
 * underneath the floating glass nav rather than being clipped above it.
 */
@Composable
fun PaddingValues.withoutBottom(): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding(),
        end = calculateEndPadding(layoutDirection),
    )
}

/** Standard Haze blur radius per the glass-on-grid spec: 20dp in light, 26dp in dark. */
private fun standardBlurRadius(dark: Boolean): Dp = if (dark) 26.dp else 20.dp

/** Elevated blur radius for surfaces that sit above cards — the bottom nav bar and sheets. */
private fun elevatedBlurRadius(dark: Boolean): Dp = if (dark) 30.dp else 26.dp

/**
 * Applies the glass-on-grid card treatment: clip to [radius], blur the content behind this
 * composable (via [hazeState], registered against a `Modifier.haze(hazeState)` background layer
 * elsewhere in the tree — see [GlassScreen]) when [LocalGlassEnabled] is true, otherwise fall
 * back to an opaque background-tinted fill at the same radius/border so the layout stays legible
 * with zero blur. Always applies the [GlassFill]/[GlassFillDark] background and a 1dp
 * [GlassBorder]/[GlassBorderDark] border on top.
 *
 * [blurRadius] defaults to the standard card blur (20dp light / 26dp dark). Nav-bar and sheet
 * callers — which the spec calls for at a larger 26-30dp radius — should pass
 * [elevatedGlassBlurRadius] explicitly, e.g. `glassCard(radius = ..., hazeState = ...,
 * blurRadius = elevatedGlassBlurRadius())`.
 */
@Composable
fun Modifier.glassCard(
    radius: Dp,
    hazeState: HazeState,
    blurRadius: Dp = standardBlurRadius(LocalIsDarkTheme.current),
): Modifier {
    val dark = LocalIsDarkTheme.current
    val fill = if (dark) GlassFillDark else GlassFill
    val border = if (dark) GlassBorderDark else GlassBorder
    val glassEnabled = LocalGlassEnabled.current
    val shape = RoundedCornerShape(radius)
    return this
        .clip(shape)
        .then(
            if (glassEnabled) {
                Modifier.hazeChild(
                    state = hazeState,
                    shape = shape,
                    style = HazeStyle(blurRadius = blurRadius),
                )
            } else {
                Modifier
            }
        )
        .background(if (glassEnabled) fill else (if (dark) BackgroundDark else Background))
        .border(1.dp, border, shape)
}

/**
 * The elevated Haze blur radius (26dp light / 30dp dark) required for surfaces that sit above
 * ordinary cards — the floating bottom nav bar and bottom sheets — for use as
 * `glassCard`'s `blurRadius` argument.
 */
@Composable
fun elevatedGlassBlurRadius(): Dp = elevatedBlurRadius(LocalIsDarkTheme.current)

/**
 * Renders, behind screen content: the [Background] fill, a 56x56dp ruled grid of 1dp [Rule]
 * lines at 70% of their base alpha full-bleed, a 280dp [AccentPrimary] square at 22% opacity
 * anchored off-screen top-right, and a 230dp accent square at 10% opacity off-screen
 * bottom-left. Call this once as the first child of a screen's root `Box`, with later content
 * layered on top via `Modifier.haze(hazeState)` on that same root `Box` so [glassCard] children
 * can see through to this ground — or use [GlassScreen], which wires this up automatically.
 */
@Composable
fun GlassGround(modifier: Modifier = Modifier) {
    val dark = LocalIsDarkTheme.current
    val bg = if (dark) BackgroundDark else Background
    val base = if (dark) RuleDark else Rule
    val ruleColor = base.copy(alpha = base.alpha * 0.70f)
    val accent = if (dark) AccentPrimaryDark else AccentPrimary
    Box(modifier.fillMaxSize().background(bg)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 56.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(ruleColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(ruleColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                y += step
            }
        }
        Box(
            Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-70).dp)
                .background(accent.copy(alpha = 0.22f))
        )
        Box(
            Modifier
                .size(230.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-70).dp, y = (-110).dp)
                .background(accent.copy(alpha = 0.10f))
        )
    }
}

/**
 * Remembers a [HazeState] for the lifetime of the composition. Thin wrapper re-export so screens
 * only need one theme import for the common case, rather than a separate `dev.chrisbanes.haze`
 * import — Haze 0.7.x does not itself expose a `rememberHazeState()` function.
 */
@Composable
fun rememberHazeState(): HazeState = remember { HazeState() }

/**
 * Scaffold that encapsulates the full glass-on-grid wiring pattern for a screen: it remembers a
 * [HazeState], lays out the root [Box], renders [GlassGround] as the background/blur-source
 * layer (via `Modifier.haze(hazeState)`), and exposes that same [HazeState] to [content] so
 * children can call `Modifier.glassCard(radius, hazeState)` and see a blurred backdrop.
 *
 * Getting Haze's source (`Modifier.haze`) vs. child (`Modifier.hazeChild`) direction backwards
 * compiles cleanly but silently renders no blur — it is not compile-checkable — so screens
 * should prefer this scaffold over hand-rolling the pattern.
 *
 * Usage:
 * ```
 * GlassScreen { hazeState ->
 *     Column(Modifier.fillMaxSize()) {
 *         Card(
 *             Modifier
 *                 .fillMaxWidth()
 *                 .glassCard(radius = Radii.Card, hazeState = hazeState)
 *         ) {
 *             // card content
 *         }
 *     }
 * }
 * ```
 *
 * A screen with unusual layout needs (e.g. a non-Box root, or multiple independent haze
 * sources) can still hand-roll the pattern using [GlassGround], [rememberHazeState] and
 * `Modifier.haze` directly.
 */
@Composable
fun GlassScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(HazeState) -> Unit,
) {
    val hazeState = rememberHazeState()
    Box(modifier.fillMaxSize()) {
        GlassGround(Modifier.haze(hazeState, style = HazeStyle(blurRadius = standardBlurRadius(LocalIsDarkTheme.current))))
        content(hazeState)
    }
}
