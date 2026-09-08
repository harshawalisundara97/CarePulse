package com.carepulse.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

/**
 * Gates backdrop blur app-wide. False on devices/preferences where blur should be skipped —
 * every consumer of [glassCard] MUST still render correctly with this false, via the opaque
 * fallback branch.
 */
val LocalGlassEnabled: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

/**
 * Applies the glass-on-grid card treatment: clip to [radius], blur the content behind this
 * composable (via [hazeState], registered against a `Modifier.haze(hazeState)` background layer
 * elsewhere in the tree — see [GlassScreen]) when [LocalGlassEnabled] is true, otherwise fall
 * back to an opaque background-tinted fill at the same radius/border so the layout stays legible
 * with zero blur. Always applies the [GlassFill]/[GlassFillDark] background and a 1dp
 * [GlassBorder]/[GlassBorderDark] border on top.
 */
@Composable
fun Modifier.glassCard(radius: Dp, hazeState: HazeState): Modifier {
    val dark = isSystemInDarkTheme()
    val fill = if (dark) GlassFillDark else GlassFill
    val border = if (dark) GlassBorderDark else GlassBorder
    val glassEnabled = LocalGlassEnabled.current
    val shape = RoundedCornerShape(radius)
    return this
        .clip(shape)
        .then(
            if (glassEnabled) {
                Modifier.hazeChild(state = hazeState, shape = shape)
            } else {
                Modifier
            }
        )
        .background(if (glassEnabled) fill else (if (dark) BackgroundDark else Background))
        .border(1.dp, border, shape)
}

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
    val dark = isSystemInDarkTheme()
    val bg = if (dark) BackgroundDark else Background
    val base = if (dark) RuleDark else Rule
    val ruleColor = base.copy(alpha = base.alpha * 0.70f)
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
                .background(AccentPrimary.copy(alpha = 0.22f))
        )
        Box(
            Modifier
                .size(230.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-70).dp, y = 110.dp)
                .background(AccentPrimary.copy(alpha = 0.10f))
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
 *                 .glassCard(radius = Radii.card, hazeState = hazeState)
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
        GlassGround(Modifier.haze(hazeState))
        content(hazeState)
    }
}
