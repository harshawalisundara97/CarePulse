# Fintech-Minimal Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace CarePulse's Navy/Teal Compose theme with the approved white-background,
minimal-fintech design system (tokens + component library), and apply it to two representative
screens (Settings, Customer Dashboard) as the proof-of-correctness pass.

**Architecture:** Kotlin + Jetpack Compose (Material 3), no new dependencies. Token files
(`Spacing.kt`, `Radii.kt`) are added; `Color.kt`, `Type.kt`, `Theme.kt` are rewritten in place;
`CommonComponents.kt` is split into a `ui/components/` package of focused files, each exposing
the same composable names the rest of the app already calls (`PrimaryButton`, `PastelCard`,
etc.) so screen files require minimal changes. Two screens are then restyled end-to-end using
the new components as a working reference for the remaining screens (tracked as Task 12, a
scoped follow-up not detailed line-by-line here per YAGNI — each screen only needs color/spacing
literal swaps, not new logic).

**Tech Stack:** Jetpack Compose, Material 3 (existing `androidx.compose.material3`), no new
Gradle dependencies required for this plan.

## Global Constraints

- Palette: white background (`#FFFFFF`) · primary text near-black (`#111827`) · secondary text
  gray (`#6B7280`) · success green (`#16A34A`) · warning/progress orange (`#F97316`) · info blue
  (`#2563EB`) · border gray (`#F2F2F2`). Dark theme uses matching near-black background
  (`#121212`) with the same accent hues brightened for contrast.
- Radii: cards 24dp · buttons 18dp · inputs 16dp · bottom nav 26dp · dialogs 28dp.
- Spacing (8pt grid): screen padding 20–24dp · card padding 16–20dp · section spacing 24–32dp.
- Typography sizes: Heading 28 Bold · Section Title 20 SemiBold · Body 16 Regular · Caption 13
  Regular · Small Label 12 Medium. `FontFamily.SansSerif` stays (Roboto on Android, no iOS
  target in this repo).
- No gradients on buttons/headers (drop `GradientHeader`'s gradient fill — flat `primary`
  background instead). No glassmorphism, no heavy shadow elevation (`tonalElevation` ≤ 1dp,
  `shadowElevation` ≤ 2dp everywhere).
- Every existing composable call site outside this plan's touched files must keep compiling —
  do not rename public composables (`PrimaryButton`, `SecondaryButton`, `GhostButton`,
  `DestructiveButton`, `PastelCard`, `GradientHeader`, `CarePulseTextField`, etc.); only their
  internals/styling change.
- Verification command after every task: `./gradlew :app:compileDebugKotlin` from
  `/Users/ranjana/Harsha/Projects/CarePulse` — must succeed with no errors before committing.

---

### Task 1: Spacing and radius token files

**Files:**
- Create: `app/src/main/java/com/carepulse/app/ui/theme/Spacing.kt`
- Create: `app/src/main/java/com/carepulse/app/ui/theme/Radii.kt`

**Interfaces:**
- Produces: `object Spacing { val ScreenPadding = 24.dp; val ScreenPaddingCompact = 20.dp; val
  CardPadding = 20.dp; val CardPaddingCompact = 16.dp; val SectionSpacing = 32.dp; val
  SectionSpacingCompact = 24.dp; val ItemGap = 8.dp }`
- Produces: `object Radii { val Card = 24.dp; val Button = 18.dp; val Input = 16.dp; val
  BottomNav = 26.dp; val Dialog = 28.dp }`

- [ ] **Step 1: Write `Spacing.kt`**

```kotlin
package com.carepulse.app.ui.theme

import androidx.compose.ui.unit.dp

/** 8pt grid spacing tokens per the fintech-minimal design spec. */
object Spacing {
    val ScreenPadding = 24.dp
    val ScreenPaddingCompact = 20.dp
    val CardPadding = 20.dp
    val CardPaddingCompact = 16.dp
    val SectionSpacing = 32.dp
    val SectionSpacingCompact = 24.dp
    val ItemGap = 8.dp
}
```

- [ ] **Step 2: Write `Radii.kt`**

```kotlin
package com.carepulse.app.ui.theme

import androidx.compose.ui.unit.dp

/** Corner-radius tokens per the fintech-minimal design spec. */
object Radii {
    val Card = 24.dp
    val Button = 18.dp
    val Input = 16.dp
    val BottomNav = 26.dp
    val Dialog = 28.dp
}
```

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL (new files are unused so far, no call sites yet).

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/theme/Spacing.kt app/src/main/java/com/carepulse/app/ui/theme/Radii.kt
git commit -m "feat(theme): add spacing and radius design tokens"
```

---

### Task 2: Rewrite `Color.kt` with the minimal fintech palette

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Color.kt` (full rewrite)

**Interfaces:**
- Consumes: nothing.
- Produces (replaces prior names 1:1 so `Theme.kt` references keep working): `Background,
  CardSurface, BorderLine, SurfaceLow, SurfaceHigh, SurfaceHighest, TextPrimary, TextSecondary,
  DangerRed, WarningAmber, SuccessGreen, InfoBlue, DarkBackground, DarkSurface, DarkSurfaceLow,
  DarkSurfaceHigh, DarkSurfaceHighest, DarkBorder, DarkOnSurface, DarkOnSurfaceVar,
  AccentPrimary, AccentPrimaryDark, AccentContainerLight, AccentContainerDark`. Drops
  `NavyPrimary, NavyDark, TealAccent, TealDark, TealLight, TealAccentDark, TealContainerDk,
  NavyContainerDk` — Task 3 updates every remaining reference.

- [ ] **Step 1: Replace file contents**

```kotlin
package com.carepulse.app.ui.theme

import androidx.compose.ui.graphics.Color

// Accent — used for primary actions, active nav state, selected chips
val AccentPrimary        = Color(0xFF2563EB) // info blue doubles as the brand accent
val AccentPrimaryDark    = Color(0xFF60A5FA)
val AccentContainerLight = Color(0xFFDBEAFE)
val AccentContainerDark  = Color(0xFF1E3A5F)

// Light surfaces
val Background      = Color(0xFFFFFFFF)
val CardSurface     = Color(0xFFFFFFFF)
val BorderLine      = Color(0xFFF2F2F2)
val SurfaceLow      = Color(0xFFF7F7F8)
val SurfaceHigh     = Color(0xFFFFFFFF)
val SurfaceHighest  = Color(0xFFFAFAFA)

// Light text
val TextPrimary     = Color(0xFF111827)
val TextSecondary   = Color(0xFF6B7280)

// Semantic
val DangerRed       = Color(0xFFDC2626)
val WarningAmber    = Color(0xFFF97316)
val SuccessGreen    = Color(0xFF16A34A)
val InfoBlue        = Color(0xFF2563EB)

// Dark surfaces
val DarkBackground     = Color(0xFF121212)
val DarkSurface        = Color(0xFF1C1C1E)
val DarkSurfaceLow     = Color(0xFF161618)
val DarkSurfaceHigh    = Color(0xFF232326)
val DarkSurfaceHighest = Color(0xFF2C2C2E)
val DarkBorder         = Color(0xFF2E2E30)

// Dark text
val DarkOnSurface    = Color(0xFFF2F2F2)
val DarkOnSurfaceVar = Color(0xFFA1A1A6)
```

- [ ] **Step 2: Verify compile fails only at known call sites (expected)**

Run: `./gradlew :app:compileDebugKotlin`
Expected: FAIL — "unresolved reference: NavyPrimary/TealAccent/..." in `Theme.kt` and
`CommonComponents.kt`. This is expected; Tasks 3–4 fix it.

- [ ] **Step 3: Commit (with `-n` skip disallowed — instead commit alongside Task 3/4 fix, see below)**

Do not commit yet — the tree won't compile until Task 3 lands. Proceed directly to Task 3, then
commit both together per Task 3 Step 4.

---

### Task 3: Rewrite `Theme.kt` color schemes and shapes

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Theme.kt` (full rewrite)

**Interfaces:**
- Consumes: `AccentPrimary, AccentPrimaryDark, AccentContainerLight, AccentContainerDark,
  Background, CardSurface, BorderLine, SurfaceLow, SurfaceHigh, SurfaceHighest, TextPrimary,
  TextSecondary, DangerRed, WarningAmber, DarkBackground, DarkSurface, DarkSurfaceLow,
  DarkSurfaceHigh, DarkSurfaceHighest, DarkBorder, DarkOnSurface, DarkOnSurfaceVar` (Task 2),
  `Radii` (Task 1), `ThemeMode`/`ThemePreference` (existing, unchanged).
- Produces: `CarePulseTheme(dynamicColor: Boolean = true, content: @Composable () -> Unit)`
  (unchanged signature — every screen call site keeps working), `CarePulseShapes` now sourced
  from `Radii`.

- [ ] **Step 1: Replace file contents**

```kotlin
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
    dynamicColor: Boolean = true,
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
```

Note: `RoundedCornerShape(8.dp)` and `12.dp` above need the `androidx.compose.ui.unit.dp` import
— add `import androidx.compose.ui.unit.dp` to the import block.

- [ ] **Step 2: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: still FAILS — `CommonComponents.kt` (Task 4) still references dropped color names
(`TealAccent`, `NavyPrimary`, etc.). Confirm the *only* remaining errors are in
`CommonComponents.kt`.

- [ ] **Step 3: Proceed to Task 4 before committing.**

---

### Task 4: Restyle `CommonComponents.kt` — flat colors, new radii, no gradients

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/components/CommonComponents.kt`

**Interfaces:**
- Consumes: `AccentPrimary, Background, CardSurface, BorderLine, TextPrimary, TextSecondary,
  DangerRed, SuccessGreen, WarningAmber` (Task 2), `Radii, Spacing` (Task 1).
- Produces: same public composable signatures as before — `PrimaryButton`, `SecondaryButton`,
  `GhostButton`, `DestructiveButton`, `PastelCard`, `GradientHeader`, `CarePulseTextField`
  (verify exact existing signatures by reading the current file before editing — do not change
  parameter lists, only internals).

- [ ] **Step 1: Update the import block**

Replace:
```kotlin
import com.carepulse.app.ui.theme.TealAccent
import com.carepulse.app.ui.theme.TealLight
import com.carepulse.app.ui.theme.NavyPrimary
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.CardSurface
import com.carepulse.app.ui.theme.BorderLine
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.ui.theme.DangerRed
import com.carepulse.app.ui.theme.SuccessGreen
import com.carepulse.app.ui.theme.WarningAmber
```
with:
```kotlin
import com.carepulse.app.ui.theme.AccentPrimary
import com.carepulse.app.ui.theme.AccentContainerLight
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.CardSurface
import com.carepulse.app.ui.theme.BorderLine
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.ui.theme.DangerRed
import com.carepulse.app.ui.theme.SuccessGreen
import com.carepulse.app.ui.theme.WarningAmber
import com.carepulse.app.ui.theme.Radii
import com.carepulse.app.ui.theme.Spacing
```

- [ ] **Step 2: Remove the gradient `Brush` from `PrimaryButton`, use flat `AccentPrimary` fill**

Find the `PrimaryButton` composable's background — it currently uses
`Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary,
MaterialTheme.colorScheme.tertiary))` (or similar, per the Phase 2 rewrite) inside a `Box` with
`.background(brush, shape = RoundedCornerShape(...))`. Replace the shape's corner radius with
`Radii.Button` and the background with a flat color:

```kotlin
.background(
    color = if (enabled) AccentPrimary else AccentPrimary.copy(alpha = 0.4f),
    shape = RoundedCornerShape(Radii.Button)
)
```

Keep the existing `clickable { ... }`, haptic feedback (`performHapticFeedback`), and
press-scale animation logic untouched — only the fill and radius change. Text color inside stays
white/`CardSurface` for contrast against `AccentPrimary`.

- [ ] **Step 3: Update `SecondaryButton`/`GhostButton`/`DestructiveButton` radius**

Wherever these use `RoundedCornerShape(...)` or `ButtonDefaults.shape`, set the corner radius to
`Radii.Button` (18dp) so all four button variants share the same silhouette.

- [ ] **Step 4: Update `PastelCard`**

Ensure the `Card`/`Surface` uses `shape = RoundedCornerShape(Radii.Card)`,
`colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)`,
`elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)` (soft, low elevation — not 0,
not >2dp), and inner content padding `Spacing.CardPadding`.

- [ ] **Step 5: Update `GradientHeader` — drop the gradient**

Rename internals only (not the composable name): replace the gradient `Brush.verticalGradient`
background with a flat `MaterialTheme.colorScheme.primary` background, keep the curved-bottom
shape (`RoundedCornerShape(bottomStart = Radii.Card, bottomEnd = Radii.Card)` or existing custom
shape) and existing content slot API unchanged.

- [ ] **Step 6: Update `CarePulseTextField` (or equivalent input composable) corner radius**

Set its `shape`/`OutlinedTextFieldDefaults.shape` to `RoundedCornerShape(Radii.Input)` and its
border color to `BorderLine` for the unfocused state.

- [ ] **Step 7: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit Tasks 2–4 together**

```bash
git add app/src/main/java/com/carepulse/app/ui/theme/Color.kt app/src/main/java/com/carepulse/app/ui/theme/Theme.kt app/src/main/java/com/carepulse/app/ui/components/CommonComponents.kt
git commit -m "feat(theme): replace Navy/Teal palette with minimal fintech tokens; flatten gradients"
```

---

### Task 5: Update `Type.kt` to match the spec's named sizes

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Type.kt` (targeted edits, not full
  rewrite)

**Interfaces:**
- Consumes: nothing new.
- Produces: same `CarePulseTypography` Material 3 `Typography` object, remapped so
  `headlineMedium` = Heading 28 Bold, `titleLarge` = Section Title 20 SemiBold, `bodyLarge` =
  Body 16 Regular, `bodySmall` = Caption 13 Regular, `labelMedium` = Small Label 12 Medium — per
  spec, screens keep using the same Material 3 slot names (`MaterialTheme.typography.*`), only
  the sizes at each slot change.

- [ ] **Step 1: Edit the four sizes called out in the spec**

Change these four lines in `CarePulseTypography`:
```kotlin
headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,     fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.25).sp),
titleLarge     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
bodySmall      = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,   fontSize = 13.sp, lineHeight = 18.sp),
labelMedium    = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 12.sp, letterSpacing = 0.5.sp),
```
(replacing the prior `headlineMedium`, `titleLarge`, `bodySmall`, `labelMedium` lines
one-for-one — leave every other line in the file untouched).

- [ ] **Step 2: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/theme/Type.kt
git commit -m "feat(theme): align type scale sizes with fintech-minimal spec"
```

---

### Task 6: Restyle the bottom navigation as a floating rounded pill

**Files:**
- Modify: the bottom navigation composable — locate it first with
  `grep -rl "NavigationBar\|BottomNav" app/src/main/java/com/carepulse/app/ui` (likely in
  `navigation/CarePulseNavGraph.kt` or a dedicated `ui/components/BottomNavBar.kt` — confirm
  exact path before editing).

**Interfaces:**
- Consumes: `Radii.BottomNav` (Task 1), `AccentPrimary` (Task 2).
- Produces: same bottom-nav composable name/signature and the same 5 destinations (Home,
  Activity, Messages, Pulse, Settings) — only its container shape/elevation/icon style change.

- [ ] **Step 1: Locate the exact file and composable**

```bash
grep -rn "NavigationBar(" app/src/main/java/com/carepulse/app/navigation/ app/src/main/java/com/carepulse/app/ui/components/ 2>/dev/null
```

- [ ] **Step 2: Wrap the `NavigationBar` in a floating rounded container**

Set the `NavigationBar`'s `modifier` to include
`.padding(horizontal = Spacing.ScreenPaddingCompact, vertical = 12.dp)
.clip(RoundedCornerShape(Radii.BottomNav))`, its `containerColor =
MaterialTheme.colorScheme.surface`, and `tonalElevation = 1.dp` (soft shadow, not flat-to-edge).
Switch any filled icon usage (`Icons.Filled.*`) for outlined equivalents
(`Icons.Outlined.*`/`Icons.Rounded.*`) for unselected items, keeping filled only for the
selected tab per Material 3 convention. Selected-tab indicator color →
`MaterialTheme.colorScheme.primaryContainer` (now `AccentContainerLight`/`AccentContainerDark`).

- [ ] **Step 3: Verify compile and visual smoke test**

Run: `./gradlew :app:compileDebugKotlin` — expect BUILD SUCCESSFUL.
Then use the iOS/Android Simulator control tool (`launch` + `screenshot`) or manually build+run
on an emulator to confirm the nav renders as a floating pill with 5 tabs, none clipped.

- [ ] **Step 4: Commit**

```bash
git add <bottom-nav-file-path>
git commit -m "feat(nav): restyle bottom navigation as floating rounded pill"
```

---

### Task 7: Restyle `SettingsScreen.kt` end-to-end

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/settings/SettingsScreen.kt`

**Interfaces:**
- Consumes: `PastelCard`, `GradientHeader` (or plain header — see Step 1), `PrimaryButton`
  (restyled in Task 4), `Spacing`, `Radii` (Task 1), `stringResource` (existing, unchanged
  localization from Phase 3.1).
- Produces: no new public API — this is a pure visual pass.

- [ ] **Step 1: Read the current file to get exact structure**

```bash
cat app/src/main/java/com/carepulse/app/ui/screens/settings/SettingsScreen.kt
```
Identify: the header composable used at the top, the `SettingRow`/list-row composable, and any
hard-coded `Color(0xFF...)` or `padding(...)` literals.

- [ ] **Step 2: Apply screen padding and section spacing**

Wrap the screen's root `Column`/`LazyColumn` content padding in
`PaddingValues(horizontal = Spacing.ScreenPadding)`, and insert `Spacer(Modifier.height(Spacing.SectionSpacing))`
between each logical section (Appearance, Language, Reminders, About) if not already present.

- [ ] **Step 3: Replace any hard-coded colors found in Step 1**

Every `Color(0xFF...)` literal in this file becomes the matching `MaterialTheme.colorScheme.*`
role (e.g. divider/border literals → `MaterialTheme.colorScheme.outline`, which now resolves to
`BorderLine`/`DarkBorder`).

- [ ] **Step 4: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Visual check in both themes**

Build + run on emulator (or Simulator tool), toggle System/Light/Dark in Settings itself, confirm
white background in light mode, near-black in dark mode, no leftover teal/navy anywhere, card
corners visibly rounded (24dp), row touch targets ≥48dp.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/settings/SettingsScreen.kt
git commit -m "style(settings): apply fintech-minimal tokens to Settings screen"
```

---

### Task 8: Restyle the Customer Dashboard screen end-to-end

**Files:**
- Modify: the customer dashboard screen file — confirm exact path via
  `ls app/src/main/java/com/carepulse/app/ui/screens/customer/` (per the existing summary this
  is `CustomerDashboardScreen.kt` or similar).

**Interfaces:**
- Consumes: `PastelCard`, `PrimaryButton`, `GradientHeader`, `Spacing`, `Radii` (all from
  Tasks 1–4).
- Produces: no new public API — pure visual pass, same as Task 7.

- [ ] **Step 1: Read the current file**

```bash
cat app/src/main/java/com/carepulse/app/ui/screens/customer/CustomerDashboardScreen.kt
```

- [ ] **Step 2: Apply the same pattern as Task 7 Steps 2–3**

Screen padding → `Spacing.ScreenPadding`; card padding inside any `PastelCard`/stat-card usage →
`Spacing.CardPadding`; section gaps → `Spacing.SectionSpacing`; replace hard-coded colors with
`MaterialTheme.colorScheme.*` roles.

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Visual check in both themes**

Same as Task 7 Step 5, applied to the Dashboard: confirm stat/upcoming-shift cards render with
24dp radius, soft 1dp shadow, no gradients, welcome header is a flat `AccentPrimary`-colored
`GradientHeader` (name kept, gradient dropped per Task 4 Step 5).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/customer/CustomerDashboardScreen.kt
git commit -m "style(dashboard): apply fintech-minimal tokens to Customer Dashboard screen"
```

---

### Task 9: Full-repo hard-coded-color audit for remaining screens

**Files:**
- Modify: every remaining screen file under `app/src/main/java/com/carepulse/app/ui/screens/`
  that still contains a `Color(0xFF...)` literal referencing the old Navy/Teal palette or any
  other hard-coded hex not already covered by Tasks 7–8.

**Interfaces:**
- Consumes: same token set as Tasks 7–8.
- Produces: no new API.

- [ ] **Step 1: Enumerate remaining offenders**

```bash
grep -rln "Color(0xFF" app/src/main/java/com/carepulse/app/ui/screens/ | grep -v -E "settings/SettingsScreen.kt|customer/CustomerDashboardScreen.kt"
```

- [ ] **Step 2: For each file listed, apply the Task 7 Steps 2–3 pattern**

Same three moves per file: screen/card/section spacing → `Spacing.*` tokens, hard-coded hex →
`MaterialTheme.colorScheme.*` role, card/button/input radii → `Radii.*` (already inherited
automatically for any file using `PastelCard`/`PrimaryButton`/etc. from Task 4 — only files with
their *own* inline `Card`/`Box` shapes need a direct radius edit).

- [ ] **Step 3: Verify compile after each file**

Run: `./gradlew :app:compileDebugKotlin` after each file edit — fix immediately if it fails
before moving to the next file (small, isolated diffs, easy to bisect).

- [ ] **Step 4: Final full-repo grep confirms zero offenders**

```bash
grep -rn "Color(0xFF" app/src/main/java/com/carepulse/app/ui/screens/
```
Expected: no output (or only genuinely one-off literals with no theme-role equivalent, e.g. a
chart's data-series colors — leave those, they're not part of this token system).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/
git commit -m "style: apply fintech-minimal tokens across remaining screens"
```

---

### Task 10: Full build + manual smoke test

**Files:** none (verification-only task).

- [ ] **Step 1: Full assemble**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Install and click through on an emulator**

Use the iOS/Android Simulator control tool (or `adb install` + manual launch) to walk: Login →
Customer Dashboard → Settings → toggle Light/Dark → Reminders → Messages → Pulse/Vitals. Confirm
in every screen: white/near-black background per theme, no leftover teal/navy, 24dp card
corners, floating rounded bottom nav, no clipped text, buttons ≥48dp tall.

- [ ] **Step 3: Report results to the user**

Summarize pass/fail per screen; do not claim "done" until this walkthrough is actually performed
and its output observed (per verification-before-completion practice) — screenshot or describe
what was seen at each step.

---
