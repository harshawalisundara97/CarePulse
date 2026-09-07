# Glass-on-Grid Redesign (Phase 1: Family Role) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace CarePulse's "fintech-minimal" theme (PR #3/#4) with the client-approved
"glass-on-grid" design system, and fully restyle every Family-role screen to match, using the
existing screens/routes/data — no navigation or data-layer changes.

**Architecture:** Kotlin + Jetpack Compose (Material 3) plus one new dependency, Haze
(`dev.chrisbanes.haze`), for backdrop blur. A new `ui/theme/Glass.kt` centralizes the glass
recipe (ruled-grid ground, off-screen accent blocks, blur-or-opaque-fallback card modifier)
behind a `LocalGlassEnabled` composition local, so every screen and the bottom nav consume the
same primitives rather than each re-implementing blur. Existing token files (`Color.kt`,
`Type.kt`, `Radii.kt`, `Spacing.kt`, `Motion.kt`, `Theme.kt`) are edited/extended in place —
their public names stay stable so already-untouched Caregiver/Agency screens keep compiling and
inherit the new palette automatically.

**Tech Stack:** Jetpack Compose, Material 3, `dev.chrisbanes.haze` (new), bundled Archivo font
resource (new).

**Spec:** `docs/superpowers/specs/2026-09-07-glass-on-grid-redesign-design.md`

## Global Constraints

- Colors (light / dark): `AccentPrimary` `#EC3013`/`#FF563C`, `AccentPressed` `#AE1800`/`#FF7A66`,
  `TextPrimary` `#201E1D`/`#F4F2F1`, `TextMuted` `#201E1D`@55%/`#F4F2F1`@55%, `Background`
  `#F3F2F2`/`#141312`, `GroundDeep` `#E6E3E1`/`#0D0C0C`, `GlassFill` white@62%/white@7%,
  `GlassFillSubtle` white@38%/white@4%, `GlassBorder` white@85%/white@13%, `Rule`
  `#201E1D`@14%/`#FFFFFF`@11%, `ShadowTint` `#2D2B2B`@13%/black@50%, `StatusAvailable` `#16A34A`
  (both), `StatusOnDuty` fill `#F97316` text `#C2410C` (both), `StatusDue` fill `#EC3013`@14%
  text `#AE1800` (both).
- Font: Archivo (400/600/700/800) everywhere — never Roboto/Inter for this redesign's screens.
- Radii: `chip=999.dp`, `input=16.dp`, `button=18.dp`, `statTile=20.dp`, `card=22.dp`,
  `cardLarge=24.dp`, `navBar=26.dp`, `sheet=30.dp` (top corners only), `avatarSmall=14.dp`,
  `avatarMedium=17.dp`, `avatarHero=24.dp`, `iconButton=13.dp`.
- Spacing: screen horizontal padding 20dp, card padding 16dp standard, vertical gap between cards
  12dp, gap inside a card 8–14dp, sheet padding 20dp sides/26dp bottom, nav bar inset 14dp
  sides/12dp bottom.
- Glass recipe: every glass-ground screen renders (in order) `Background` fill → 56×56dp ruled
  grid of 1dp `Rule` lines @70% opacity full-bleed → 280dp `AccentPrimary` square @22% opacity
  offset (−70dp, −70dp) from top-right → 230dp accent square @10% opacity offset 110dp up from
  bottom-left. Every glass card: clip to its radius, `GlassFill` background, 1dp `GlassBorder`
  border, blurred via Haze when `LocalGlassEnabled` else an opaque `Background`-tinted fallback at
  the same radius/border. Blur radius 20dp light/26dp dark; nav bar and sheets 26–30dp.
- No public composable already used by Caregiver/Agency screens may be renamed or have its
  parameter list changed unless this plan says so explicitly — those roles are Phase 2/3.
- No new routes, no route renames, no navigation restructuring — this is a pure visual/motion
  pass on existing screens (`navigation/CarePulseNavGraph.kt`'s `Routes` object is unchanged).
- No new data-layer/Firestore work — every screen keeps its existing data source.
- New/changed user-facing strings go into `res/values/strings.xml` +
  `res/values-si/strings.xml` + `res/values-ta/strings.xml` (existing localization pattern), not
  hardcoded.
- Status pills always carry a text label, never color-only. Every icon button has
  `contentDescription`. Touch targets ≥48dp.
- Verification command after every task: `./gradlew :app:compileDebugKotlin` from
  `/Users/ranjana/Harsha/Projects/CarePulse` — must succeed with no errors before committing.

---

### Task 0: Update CLAUDE.md's Frontend Design Language section

**Files:**
- Modify: `/Users/ranjana/Harsha/Projects/CarePulse/CLAUDE.md`

**Interfaces:** None (documentation only).

- [ ] **Step 1: Read the current file**

```bash
cat CLAUDE.md
```

Find the `# Frontend Design Language — Follow for All UI Work` section (added by the previous
fintech-minimal redesign) — it currently says "no glassmorphism", "White background", etc.

- [ ] **Step 2: Replace that entire section**

Replace the whole `# Frontend Design Language — Follow for All UI Work` section (from its heading
through its last bullet, right before the next top-level `#` heading or end of file) with:

```markdown
# Frontend Design Language — Follow for All UI Work

This is the required visual/design system for CarePulse's Android UI. Full spec:
`docs/superpowers/specs/2026-09-07-glass-on-grid-redesign-design.md`.

**Style:** Glass-on-grid. Translucent blurred surfaces (via Haze, `dev.chrisbanes.haze`) over a
ruled 56dp grid and off-screen accent blocks, on Modernist typographic bones (Archivo font,
flush-left uppercase labels, 2px rules, one red accent colour). Glass must always be gated behind
`LocalGlassEnabled` (`ui/theme/Glass.kt`) with an opaque `Background`-tinted fallback at the same
radius/border for low-end devices — layouts must stay legible with zero blur.

**Palette:** `AccentPrimary` red (`#EC3013` light / `#FF563C` dark) for primary actions, active
nav, and the accent field block. Near-black/near-white text (`TextPrimary`), 55%-alpha muted text
(`TextMuted`). `Background` `#F3F2F2` light / `#141312` dark. Full light + dark themes required.
Status pills use a 16%-alpha fill of their own hue with the full-strength hue as text, and always
carry a text label — never color-only.

**Spacing:** 4dp base grid. Screen horizontal padding 20dp. Card padding 16dp standard. Vertical
gap between cards 12dp. Sheet padding 20dp sides / 26dp bottom.

**Radii:** `chip=999dp` · `input=16dp` · `button=18dp` · `statTile=20dp` · `card=22dp` ·
`cardLarge=24dp` · `navBar=26dp` · `sheet=30dp` (top corners only).

**Typography:** Archivo (400/600/700/800) only — do not substitute Roboto or Inter for
glass-on-grid screens. Scale: Display 46sp/800, H1 31sp/800, H2 27sp/800, H3 23sp/800, H4
16sp/800, Body 13.5sp, Label 10–11sp/800 UPPERCASE.

**Navigation:** floating glass bottom nav, 66dp tall, radius 26dp, 3dp accent indicator sliding
along the top edge between 5 equal slots. Keep CarePulse's own tabs per role (Home/Pulse/
Messages/Activity/Settings for Family & Caregiver; Dashboard/Caregivers/Requests/Billing/Settings
for Agency) — do not rename routes.

**Motion:** named durations/easings in `ui/theme/Motion.kt` — screen enter, list stagger, shared-
element profile hero, bottom-sheet slide-in, pulse rings, tab indicator slide, press feedback,
pull-to-refresh spin, sparkline draw. Honour reduce-motion: drop infinite pulse/stagger, keep
fades.

**Accessibility:** minimum touch target 48x48dp, WCAG AA contrast, full-opacity ink on accent/
glass grounds (never alpha-muted text over blur), every icon button has a `contentDescription`.

**Avoid:** hardcoded Roboto/Inter on redesigned screens, color-only status indicators, alpha-
muted text over blurred surfaces, un-gated blur with no fallback path.
```

- [ ] **Step 3: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: update design-language guidance for glass-on-grid redesign"
```

---

### Task 1: Add the Haze dependency

**Files:**
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Produces: `dev.chrisbanes.haze:haze` and `dev.chrisbanes.haze:haze-materials` available for
  import as `dev.chrisbanes.haze.*` in later tasks.

- [ ] **Step 1: Add the dependency**

In the `dependencies { ... }` block, after the `implementation("androidx.compose.material3:material3:1.3.0")`
line, add:

```kotlin
    // Backdrop blur for the glass-on-grid design system
    implementation("dev.chrisbanes.haze:haze:0.7.3")
    implementation("dev.chrisbanes.haze:haze-materials:0.7.3")
```

- [ ] **Step 2: Sync and verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL (dependency resolves; nothing uses it yet).

If the exact version `0.7.3` fails to resolve, check
`https://github.com/chrisbanes/haze/releases` is not reachable in this environment — in that
case run `./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep haze` to see
what Gradle resolved, and use the latest version already cached/resolvable instead; note the
actual version used in your report.

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle.kts
git commit -m "build: add Haze dependency for backdrop blur"
```

---

### Task 2: Bundle the Archivo font and rewrite Type.kt

**Files:**
- Create: `app/src/main/res/font/archivo_regular.ttf` (400), `archivo_semibold.ttf` (600),
  `archivo_bold.ttf` (700), `archivo_extrabold.ttf` (800)
- Create: `app/src/main/res/font/archivo.xml` (font family resource)
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Type.kt` (full rewrite)

**Interfaces:**
- Consumes: nothing new.
- Produces: `val Archivo: FontFamily` and a rewritten `val CarePulseTypography: Typography` —
  same top-level name every screen already references via `MaterialTheme.typography.*`, plus new
  named `TextStyle` vals for sizes Material 3's `Typography` doesn't have a slot for (`Display`,
  `NumericXl`, `NumericL`, `NumericM`, `Label`, `Chip`, `Nav`).

- [ ] **Step 1: Download the Archivo font files**

Run from the repo root:

```bash
mkdir -p app/src/main/res/font
curl -sL "https://github.com/google/fonts/raw/main/ofl/archivo/Archivo%5Bwdth%2Cwght%5D.ttf" -o /tmp/archivo-variable.ttf
```

The Archivo family ships as a single variable font on Google Fonts. Android's `FontFamily`
builder needs distinct static weights, so instead fetch the static weight files from the same
repo:

```bash
curl -sL "https://github.com/google/fonts/raw/main/ofl/archivo/static/Archivo-Regular.ttf" -o app/src/main/res/font/archivo_regular.ttf
curl -sL "https://github.com/google/fonts/raw/main/ofl/archivo/static/Archivo-SemiBold.ttf" -o app/src/main/res/font/archivo_semibold.ttf
curl -sL "https://github.com/google/fonts/raw/main/ofl/archivo/static/Archivo-Bold.ttf" -o app/src/main/res/font/archivo_bold.ttf
curl -sL "https://github.com/google/fonts/raw/main/ofl/archivo/static/Archivo-ExtraBold.ttf" -o app/src/main/res/font/archivo_extrabold.ttf
ls -la app/src/main/res/font/
```

Verify each downloaded file is a real font, not an HTML error page:

```bash
file app/src/main/res/font/archivo_regular.ttf app/src/main/res/font/archivo_semibold.ttf app/src/main/res/font/archivo_bold.ttf app/src/main/res/font/archivo_extrabold.ttf
```

Expected: each line reports a TrueType/OpenType font, not "HTML document" or "ASCII text". If any
file fails this check (network blocked, path moved on the google/fonts repo), report BLOCKED in
your final status — do not substitute a different font family without asking, since the spec is
explicit that Archivo is required and Roboto/Inter must not be substituted.

Android resource filenames must be lowercase with underscores only — the four filenames above
already satisfy that.

- [ ] **Step 2: Create the font family resource**

Write `app/src/main/res/font/archivo.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<font-family xmlns:app="http://schemas.android.com/apk/res-auto">
    <font
        app:font="@font/archivo_regular"
        app:fontStyle="normal"
        app:fontWeight="400" />
    <font
        app:font="@font/archivo_semibold"
        app:fontStyle="normal"
        app:fontWeight="600" />
    <font
        app:font="@font/archivo_bold"
        app:fontStyle="normal"
        app:fontWeight="700" />
    <font
        app:font="@font/archivo_extrabold"
        app:fontStyle="normal"
        app:fontWeight="800" />
</font-family>
```

- [ ] **Step 3: Rewrite Type.kt**

Replace the entire contents of `app/src/main/java/com/carepulse/app/ui/theme/Type.kt`:

```kotlin
package com.carepulse.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.carepulse.app.R

/** Archivo — required for the glass-on-grid design system. Do not substitute. */
val Archivo = FontFamily(
    Font(R.font.archivo_regular, FontWeight.Normal),
    Font(R.font.archivo_semibold, FontWeight.SemiBold),
    Font(R.font.archivo_bold, FontWeight.Bold),
    Font(R.font.archivo_extrabold, FontWeight.ExtraBold)
)

/** Sizes with no Material 3 Typography slot — used directly via these vals. */
val TypeDisplay = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 46.sp, letterSpacing = (-1.38).sp)
val TypeNumericXl = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, letterSpacing = (-1.32).sp)
val TypeNumericL = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, letterSpacing = (-0.64).sp)
val TypeNumericM = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, letterSpacing = (-0.46).sp)
val TypeLabel = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 0.6.sp)
val TypeChip = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
val TypeNav = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 9.5.sp, letterSpacing = 0.19.sp)

/** Glass-on-grid type scale, mapped onto Material 3's Typography slots. */
val CarePulseTypography = Typography(
    displayLarge   = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 46.sp, lineHeight = 52.sp, letterSpacing = (-1.38).sp),
    displayMedium  = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 38.sp, lineHeight = 44.sp, letterSpacing = (-1.14).sp),
    displaySmall   = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-1.02).sp),
    headlineLarge  = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 31.sp, lineHeight = 38.sp, letterSpacing = (-0.62).sp), // H1
    headlineMedium = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, lineHeight = 34.sp, letterSpacing = (-0.54).sp), // H2
    headlineSmall  = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, lineHeight = 29.sp, letterSpacing = (-0.46).sp), // H3
    titleLarge     = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 21.sp), // H4 / section header
    titleMedium    = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    titleSmall     = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, lineHeight = 18.sp),
    bodyLarge      = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 13.5.sp, lineHeight = 19.sp), // Body
    bodyMedium     = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 13.5.sp, lineHeight = 19.sp),
    bodySmall      = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp), // Body S
    labelLarge     = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 0.sp), // Chip
    labelMedium    = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 0.6.sp), // Label/overline
    labelSmall     = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 9.5.sp, letterSpacing = 0.19.sp) // Nav
)
```

Note: Material 3's `Typography` has 15 named slots and the glass-on-grid spec has more named
roles than that (Display, H1–H4, Numeric XL/L/M, Body, Body S, Label, Chip, Nav — 13 roles, but
Numeric XL/L/M need their own vals since Material 3 has no numeric-specific slot and this app
displays money/vitals figures that need `FontFeatureSettings("tnum")`-style tabular treatment).
That's why `TypeDisplay`/`TypeNumericXl`/`TypeNumericL`/`TypeNumericM`/`TypeLabel`/`TypeChip`/
`TypeNav` exist as standalone vals alongside the `Typography` mapping — screens use
`MaterialTheme.typography.headlineLarge` for H1 etc., but reach for `TypeNumericXl` directly for
the live-shift timer and `TypeNumericL`/`TypeNumericM` for money/vitals figures.

- [ ] **Step 4: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. If `R.font.archivo_regular` etc. are unresolved, re-check the font
filenames in Step 1 are lowercase snake_case with no extension in the XML `app:font` references.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/font/ app/src/main/java/com/carepulse/app/ui/theme/Type.kt
git commit -m "feat(theme): bundle Archivo font and rewrite type scale for glass-on-grid"
```

---

### Task 3: Rewrite Color.kt with the glass-on-grid palette

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Color.kt` (full rewrite)

**Interfaces:**
- Consumes: nothing.
- Produces (new/renamed top-level `Color` vals — Task 4 wires these into `Theme.kt`, so name
  them exactly as below): `AccentPrimary, AccentPrimaryDark, AccentPressed, AccentPressedDark,
  TextPrimary, TextPrimaryDark, TextMuted, TextMutedDark, Background, BackgroundDark, GroundDeep,
  GroundDeepDark, GlassFill, GlassFillDark, GlassFillSubtle, GlassFillSubtleDark, GlassBorder,
  GlassBorderDark, Rule, RuleDark, ShadowTint, ShadowTintDark, StatusAvailable, StatusOnDuty,
  StatusOnDutyText, StatusDueFill, StatusDueText, DangerRed, SuccessGreen`. Drops
  `AccentContainerLight, AccentContainerDark, CardSurface, SurfaceLow, SurfaceHigh,
  SurfaceHighest, TextSecondary, WarningAmber, InfoBlue, DarkBackground, DarkSurface,
  DarkSurfaceLow, DarkSurfaceHigh, DarkSurfaceHighest, DarkBorder, DarkOnSurface,
  DarkOnSurfaceVar` — Task 4 fixes every remaining reference to a dropped name across the whole
  repo (all roles), same pattern as the prior redesign's Task 2.

- [ ] **Step 1: Replace file contents**

```kotlin
package com.carepulse.app.ui.theme

import androidx.compose.ui.graphics.Color

// Accent — primary actions, active nav, accent field block, key figures
val AccentPrimary     = Color(0xFFEC3013)
val AccentPrimaryDark = Color(0xFFFF563C)
val AccentPressed     = Color(0xFFAE1800)
val AccentPressedDark = Color(0xFFFF7A66)

// Text
val TextPrimary     = Color(0xFF201E1D)
val TextPrimaryDark = Color(0xFFF4F2F1)
val TextMuted       = Color(0xFF201E1D).copy(alpha = 0.55f)
val TextMutedDark   = Color(0xFFF4F2F1).copy(alpha = 0.55f)

// Ground
val Background      = Color(0xFFF3F2F2)
val BackgroundDark   = Color(0xFF141312)
val GroundDeep       = Color(0xFFE6E3E1)
val GroundDeepDark   = Color(0xFF0D0C0C)

// Glass surfaces
val GlassFill        = Color(0xFFFFFFFF).copy(alpha = 0.62f)
val GlassFillDark     = Color(0xFFFFFFFF).copy(alpha = 0.07f)
val GlassFillSubtle   = Color(0xFFFFFFFF).copy(alpha = 0.38f)
val GlassFillSubtleDark = Color(0xFFFFFFFF).copy(alpha = 0.04f)
val GlassBorder       = Color(0xFFFFFFFF).copy(alpha = 0.85f)
val GlassBorderDark   = Color(0xFFFFFFFF).copy(alpha = 0.13f)

// Rules / grid lines
val Rule     = Color(0xFF201E1D).copy(alpha = 0.14f)
val RuleDark = Color(0xFFFFFFFF).copy(alpha = 0.11f)

// Elevation tint (never pure black)
val ShadowTint     = Color(0xFF2D2B2B).copy(alpha = 0.13f)
val ShadowTintDark = Color(0xFF000000).copy(alpha = 0.50f)

// Status — same hue in both themes
val StatusAvailable  = Color(0xFF16A34A)
val StatusOnDuty     = Color(0xFFF97316)
val StatusOnDutyText = Color(0xFFC2410C)
val StatusDueFill    = Color(0xFFEC3013).copy(alpha = 0.14f)
val StatusDueText    = Color(0xFFAE1800)

// General semantic (kept for existing error/success call sites across all roles)
val DangerRed    = Color(0xFFDC2626)
val SuccessGreen = Color(0xFF16A34A)
```

- [ ] **Step 2: Attempt compile (expected to fail — do not commit yet)**

Run: `./gradlew :app:compileDebugKotlin`
Expected: FAILS with many "unresolved reference" errors across `Theme.kt`,
`CommonComponents.kt`, and screen files that referenced the dropped names. This is expected —
Task 4 fixes every call site. Do not commit this task's file alone; proceed directly to Task 4.

---

### Task 4: Rewrite Theme.kt and fix every dropped-color-name call site repo-wide

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Theme.kt` (full rewrite)
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Radii.kt` (extend)
- Modify: every file across the whole repo (all three roles) that references a name Task 3
  dropped

**Interfaces:**
- Consumes: the new `Color.kt` vals from Task 3, `Radii` (extended below).
- Produces: `CarePulseTheme(dynamicColor: Boolean = false, content: @Composable () -> Unit)` —
  same signature as before, every existing call site keeps compiling.

- [ ] **Step 1: Extend Radii.kt**

Replace the contents of `app/src/main/java/com/carepulse/app/ui/theme/Radii.kt`:

```kotlin
package com.carepulse.app.ui.theme

import androidx.compose.ui.unit.dp

/** Corner-radius tokens per the glass-on-grid design spec. */
object Radii {
    val Chip = 999.dp
    val Input = 16.dp
    val Button = 18.dp
    val StatTile = 20.dp
    val Card = 22.dp
    val CardLarge = 24.dp
    val BottomNav = 26.dp
    val Dialog = 28.dp
    val Sheet = 30.dp
    val AvatarSmall = 14.dp
    val AvatarMedium = 17.dp
    val AvatarHero = 24.dp
    val IconButton = 13.dp
}
```

(`Card` moves from 24dp to 22dp; every other existing field keeps its old value; six new fields
are added. `Dialog` at 28dp is retained for plain `AlertDialog`s — the spec's 30dp `Sheet` radius
is specifically for bottom sheets' top corners.)

- [ ] **Step 2: Replace Theme.kt**

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CarePulseTypography,
        shapes = CarePulseShapes,
        content = content
    )
}
```

Add `import androidx.compose.ui.graphics.Color` to the import block (needed for the
`Color(0xFFFFFFFF)` literals above).

- [ ] **Step 3: Find every remaining reference to a dropped color name**

```bash
grep -rln "AccentContainerLight\|AccentContainerDark\|CardSurface\|SurfaceLow\|SurfaceHigh\|SurfaceHighest\|TextSecondary\|WarningAmber\|InfoBlue\|DarkBackground\|DarkSurface\|DarkBorder\|DarkOnSurface" app/src/main/java/com/carepulse/app/
```

For each file listed, open it and replace the dropped name with the nearest equivalent new
token, using this mapping:

| Dropped | Replace with |
|---|---|
| `CardSurface` | `Color(0xFFFFFFFF)` (Compose `Color`, keep the import) or `MaterialTheme.colorScheme.onPrimary` if it was content-on-accent |
| `SurfaceLow`, `SurfaceHigh`, `SurfaceHighest` | `MaterialTheme.colorScheme.surfaceContainerLow` / `surfaceContainerHigh` / `surfaceContainerHighest` respectively |
| `TextSecondary` | `TextMuted` (import from `ui.theme`) or `MaterialTheme.colorScheme.onSurfaceVariant` |
| `WarningAmber` | `StatusOnDuty` (import from `ui.theme`) |
| `InfoBlue` | `MaterialTheme.colorScheme.primary` (no dedicated info-blue token in the new palette; if the call site is genuinely informational and not a primary action, use `TextMuted` instead — use judgement per call site) |
| `DarkBackground`, `DarkSurface` | `BackgroundDark` / `GlassFillDark` respectively |
| `DarkBorder` | `RuleDark` |
| `DarkOnSurface` | `TextPrimaryDark` |
| `AccentContainerLight`, `AccentContainerDark` | `MaterialTheme.colorScheme.primaryContainer` |

Prefer `MaterialTheme.colorScheme.*` roles over importing the raw token directly wherever the
surrounding code already uses `MaterialTheme.colorScheme.*` for neighboring colors (consistency
with the file's existing style); import the raw token only where the file already imports raw
tokens for other colors.

- [ ] **Step 4: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. Repeat Step 3's grep — it must return no results.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat(theme): replace fintech-minimal palette with glass-on-grid tokens"
```

---

### Task 5: Extend Spacing.kt and Motion.kt

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Spacing.kt` (extend)
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Motion.kt` (extend)

**Interfaces:**
- Produces: new `Spacing` fields (`CardGap`, `InCardGap`, `SheetSides`, `SheetBottom`,
  `NavBarSides`, `NavBarBottom`) and new `Motion` fields (see below) — all additive, no existing
  field renamed or removed.

- [ ] **Step 1: Add fields to Spacing.kt**

Add these members inside the existing `object Spacing { ... }` block (do not remove any existing
field):

```kotlin
    val CardGap = 12.dp
    val InCardGap = 12.dp
    val SheetSides = 20.dp
    val SheetBottom = 26.dp
    val NavBarSides = 14.dp
    val NavBarBottom = 12.dp
```

- [ ] **Step 2: Add fields to Motion.kt**

Add these members inside the existing `object Motion { ... }` block (do not remove any existing
field; the existing `Standard`/`Emphasized`/`EmphasizedDecelerate` easing curves already match
the spec's "Standard" and "Emphasised" curves exactly, so reuse them rather than adding
duplicates):

```kotlin
    // Glass-on-grid motion durations (ms), named per the design handoff's moment table
    const val ScreenEnter = 340
    const val ListStaggerStep = 60
    const val ListStaggerMax = 8
    const val ListItemDuration = 500
    const val ProfileHero = 460
    const val BottomSheet = 380
    const val FabToSheet = 380
    const val SheetScrim = 220
    const val LiveShiftPulse = 2200
    const val LiveShiftPulseOffset = 1100
    const val HeartBeat = 1800
    const val TabIndicator = 340
    const val PressFeedback = 140
    const val PullToRefreshTurn = 900
    const val SparklineDraw = 1300
    const val RevenueBars = 600
    const val ThemeSwitch = 280
```

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/theme/Spacing.kt app/src/main/java/com/carepulse/app/ui/theme/Motion.kt
git commit -m "feat(theme): add glass-on-grid spacing and motion tokens"
```

---

### Task 6: Create Glass.kt — the shared glass-card and ruled-ground primitives

**Files:**
- Create: `app/src/main/java/com/carepulse/app/ui/theme/Glass.kt`

**Interfaces:**
- Consumes: `Background, Rule, AccentPrimary, GlassFill, GlassBorder` (Task 3), `dev.chrisbanes.haze.*`
  (Task 1).
- Produces: `val LocalGlassEnabled: ProvidableCompositionLocal<Boolean>`,
  `fun Modifier.glassCard(radius: Dp, hazeState: HazeState): Modifier`,
  `@Composable fun GlassGround(modifier: Modifier = Modifier)`, `@Composable fun rememberHazeState(): HazeState`
  (thin wrapper re-export so screens don't need a separate Haze import for the common case). Every
  later screen task imports these three from `com.carepulse.app.ui.theme`.

- [ ] **Step 1: Write the file**

```kotlin
package com.carepulse.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.foundation.isSystemInDarkTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.haze

/** Gates backdrop blur app-wide. False on devices/preferences where blur should be skipped —
 * every consumer of [glassCard] MUST still render correctly with this false, via the opaque
 * fallback branch. */
val LocalGlassEnabled: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

/**
 * Applies the glass-on-grid card treatment: clip to [radius], blur the content behind this
 * composable (via [hazeState]) when [LocalGlassEnabled] is true, otherwise fall back to an
 * opaque background-tinted fill at the same radius/border so the layout stays legible with zero
 * blur. Always applies the [GlassFill]/[GlassFillDark] background and a 1dp [GlassBorder]/
 * [GlassBorderDark] border on top.
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
            if (glassEnabled) Modifier.hazeChild(state = hazeState) else Modifier
        )
        .background(if (glassEnabled) fill else (if (dark) BackgroundDark else Background))
        .border(1.dp, border, shape)
}

/**
 * Renders, behind screen content: the [Background] fill, a 56x56dp ruled grid of 1dp [Rule]
 * lines at 70% opacity full-bleed, a 280dp [AccentPrimary] square at 22% opacity anchored
 * off-screen top-right, and a 230dp accent square at 10% opacity off-screen bottom-left. Call
 * this once as the first child of a screen's root `Box`, with later content layered on top via
 * `Modifier.haze(hazeState)` on that same root `Box` so [glassCard] children can see through to
 * this ground.
 */
@Composable
fun GlassGround(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val bg = if (dark) BackgroundDark else Background
    val ruleColor = (if (dark) RuleDark else Rule).copy(alpha = (if (dark) RuleDark else Rule).alpha * 0.70f / (if (dark) RuleDark else Rule).alpha)
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
                .align(androidx.compose.ui.Alignment.TopEnd)
                .offset(x = 70.dp, y = (-70).dp)
                .background(AccentPrimary.copy(alpha = 0.22f), androidx.compose.foundation.shape.CircleShape.let { RoundedCornerShape(0.dp) })
        )
        Box(
            Modifier
                .size(230.dp)
                .align(androidx.compose.ui.Alignment.BottomStart)
                .offset(x = (-70).dp, y = (-110).dp)
                .background(AccentPrimary.copy(alpha = 0.10f))
        )
    }
}

/** Convenience re-export so screens only need one import line for the common case. */
@Composable
fun rememberHazeState(): HazeState = dev.chrisbanes.haze.rememberHazeState()
```

This file has two known rough edges the implementer must clean up while writing it (do not ship
them as-is — they're flagged here because the exact Haze 0.7.x API surface must be checked against
whatever version Task 1 actually resolved):

1. The `RoundedCornerShape(0.dp)`/`CircleShape.let{}` line for the top-right accent square is
   wrong — it should just be a plain square, i.e. `Modifier.background(AccentPrimary.copy(alpha = 0.22f))`
   with no shape at all (a `Box` background with no `clip` renders a rectangle). Fix this before
   compiling.
2. `import androidx.compose.ui.foundation.isSystemInDarkTheme` is the wrong package — the correct
   import is `androidx.compose.foundation.isSystemInDarkTheme`. Fix this before compiling.
3. Verify the actual Haze 0.7.x API: `hazeChild(state = ...)` and `Modifier.haze(state)` /
   `rememberHazeState()` names may differ slightly by exact patch version. Run
   `./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep haze` to confirm the
   resolved version, then check its actual public API (e.g. via
   `unzip -p ~/.gradle/caches/**/haze-<version>.jar` class listing, or the compiler's own
   "unresolved reference" / "no value passed for parameter" errors) and adjust `glassCard`/
   `GlassGround`/`rememberHazeState` to match — the exact parameter names matter more than what's
   written above, which is illustrative of the intended behavior, not a guaranteed-correct API
   surface for every Haze version.

- [ ] **Step 2: Fix the two flagged issues, verify the real Haze API, and compile**

Apply the fixes named above, then run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL. Iterate against real compiler errors until it is — this task's
success criterion is a compiling `Glass.kt` whose `glassCard`/`GlassGround` behave as described,
not a byte-for-byte match to the illustrative code above.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/theme/Glass.kt
git commit -m "feat(theme): add Glass.kt — glass-card modifier and ruled-ground background"
```

---

### Task 7: Rebuild the bottom navigation as a floating glass bar with a sliding top indicator

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/navigation/CarePulseNavGraph.kt`

**Interfaces:**
- Consumes: `Modifier.glassCard`, `rememberHazeState` (Task 6), `Radii.BottomNav` (Task 4),
  `Spacing.NavBarSides`/`NavBarBottom` (Task 5), `Motion.TabIndicator` (Task 5).
- Produces: same `BottomBar` private composable signature — `tabRoutes`, `Routes`, `TabItem`,
  `tabsFor` all stay exactly as they are; only `BottomBar`'s internals change.

- [ ] **Step 1: Read the current `BottomBar` composable**

```bash
grep -n "private fun BottomBar" -A 40 app/src/main/java/com/carepulse/app/navigation/CarePulseNavGraph.kt
```

- [ ] **Step 2: Replace `BottomBar`'s body**

Replace the entire `private fun BottomBar(...)` function with:

```kotlin
@Composable
private fun BottomBar(
    navController: NavHostController,
    currentRoute: String?,
    tabs: List<TabItem>
) {
    val hazeState = rememberHazeState()
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Box(
        Modifier
            .padding(horizontal = Spacing.NavBarSides, vertical = Spacing.NavBarBottom)
            .height(66.dp)
            .fillMaxWidth()
            .glassCard(radius = Radii.BottomNav, hazeState = hazeState)
    ) {
        val slotFraction = 1f / tabs.size
        val indicatorOffset by androidx.compose.animation.core.animateFloatAsState(
            targetValue = selectedIndex * slotFraction,
            animationSpec = androidx.compose.animation.core.tween(Motion.TabIndicator, easing = Motion.Standard),
            label = "tabIndicator"
        )

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val indicatorWidth = maxWidth * slotFraction
            Box(
                Modifier
                    .offset(x = maxWidth * indicatorOffset)
                    .width(indicatorWidth)
                    .height(3.dp)
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    val iconColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(Routes.Home) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            if (selected) tab.icon else tab.outlinedIcon,
                            contentDescription = tab.label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(tab.label.uppercase(), style = TypeNav, color = iconColor)
                    }
                }
            }
        }
    }
}
```

Add these imports if not already present in the file: `androidx.compose.foundation.layout.BoxWithConstraints`,
`androidx.compose.foundation.layout.height`, `androidx.compose.foundation.layout.width`,
`androidx.compose.foundation.layout.Arrangement`, `androidx.compose.foundation.interaction.MutableInteractionSource`,
`androidx.compose.foundation.clickable`, `androidx.compose.runtime.remember`,
`androidx.compose.ui.Alignment`, `androidx.compose.foundation.background`,
`com.carepulse.app.ui.theme.glassCard`, `com.carepulse.app.ui.theme.rememberHazeState`,
`com.carepulse.app.ui.theme.TypeNav`, `com.carepulse.app.ui.theme.Motion`.

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Visual smoke check**

Build and install (`./gradlew :app:installDebug` on a connected device/emulator if available) and
confirm the nav renders as a floating 66dp glass pill with a 3dp indicator under the active tab
that slides when you switch tabs, for all three roles' tab sets — this file's `tabsFor()` is
unchanged so all three still render.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/carepulse/app/navigation/CarePulseNavGraph.kt
git commit -m "feat(nav): rebuild bottom navigation as a floating glass bar with sliding indicator"
```

---

### Task 8: Redesign RoleSelectionScreen.kt and LoginScreen.kt

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/onboarding/RoleSelectionScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/auth/LoginScreen.kt`

**Interfaces:**
- Consumes: `GlassGround`, `Modifier.glassCard`, `rememberHazeState` (Task 6), full token set
  from Tasks 3–5.
- Produces: no signature changes — both screens keep their existing parameters
  (`RoleSelectionScreen(onRoleSelected: (UserRole) -> Unit)`,
  `LoginScreen(role: String, vm: CarePulseViewModel, onAuthSuccess: (Boolean) -> Unit, onBack: () -> Unit)`
  — verify exact current signatures by reading the files first, since this plan does not
  reproduce them and they must not change).

- [ ] **Step 1: Read both files fully**

```bash
cat app/src/main/java/com/carepulse/app/ui/screens/onboarding/RoleSelectionScreen.kt
cat app/src/main/java/com/carepulse/app/ui/screens/auth/LoginScreen.kt
```

- [ ] **Step 2: Apply the glass-on-grid treatment to RoleSelectionScreen**

Wrap the screen's root in a `Box` containing `GlassGround()` as the first child, then the actual
content on top wrapped in `Modifier.haze(hazeState)` (so cards elsewhere on the same screen, if
any, can blur it) — for this screen specifically there are no glass cards over the ground (per
the handoff, role-select is mostly type + a role-select control on the plain ground), so a full
`glassCard` treatment may not apply; use judgement: if the current screen already has a
`Card`/`Surface`-style container around the role options, convert it to `Modifier.glassCard(Radii.CardLarge, hazeState)`;
if it's a flat list of buttons directly on the background, leave it flat and just swap the ground
and typography. Apply `MaterialTheme.typography.headlineLarge` (H1) to the screen title, `Archivo`
body styles elsewhere, `AccentPrimary` to the primary action. Keep the existing Sinhala subhead
string (per the spec's localisation note, this string already exists in `strings.xml` — do not
hardcode a new one).

- [ ] **Step 3: Apply the glass-on-grid treatment to LoginScreen**

Read the current login flow first — if it is NOT actually a 4-digit OTP flow (the handoff
describes a prototype OTP flow but this repo's real login may be email/password or Google
sign-in, since that was implemented in a prior session), do NOT invent an OTP UI that doesn't
match the real auth flow. Instead: apply the visual treatment only — `GlassGround()` background,
glass-card treatment on the login form container (`Modifier.glassCard(Radii.CardLarge, hazeState)`),
`AccentPrimary` on the primary CTA, H1 title style — while keeping whatever real auth fields/
buttons already exist (email/password/Google, per this repo's actual implementation). Report in
your final status whether the real flow matched the handoff's OTP description or not, and what
you did instead.

- [ ] **Step 4: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/onboarding/RoleSelectionScreen.kt app/src/main/java/com/carepulse/app/ui/screens/auth/LoginScreen.kt
git commit -m "style: apply glass-on-grid tokens to role selection and login screens"
```

---

### Task 9: Redesign CustomerDashboardScreen.kt (Family home)

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/CustomerDashboardScreen.kt`

**Interfaces:**
- Consumes: `GlassGround`, `Modifier.glassCard`, `rememberHazeState` (Task 6), token set from
  Tasks 3–5.
- Produces: no signature changes — read the file first to confirm the exact current signature
  (per the prior redesign session it was
  `CustomerDashboardScreen(vm, onOpenCaregiver, onOpenPulse, onRequestCare, onSignOut)`; verify
  this is still accurate before editing, since intervening commits may have changed it).

- [ ] **Step 1: Read the current file fully**

```bash
cat app/src/main/java/com/carepulse/app/ui/screens/customer/CustomerDashboardScreen.kt
```

- [ ] **Step 2: Apply glass-on-grid treatment**

- Root: `Box` with `GlassGround()` behind, content `Modifier.haze(hazeState)` on top, screen
  horizontal padding `Spacing.ScreenPaddingCompact` (20dp — matches spec's "screen horizontal
  padding 20dp" exactly).
- Every card-like container (the caregiver card, the "need" chips row's container if any, stat
  tiles) becomes `Modifier.glassCard(Radii.Card, hazeState)` with `Spacing.CardPaddingCompact`
  (16dp) inner padding; vertical gap between cards `Spacing.CardGap` (12dp).
- Per the handoff's per-screen note: pull-to-refresh on this screen should trigger a spin +
  "Syncing latest vitals…" pill — if this repo does not yet have `PullToRefreshBox` wired here,
  do not add new pull-to-refresh *logic* in this task (that's a feature addition, out of this
  redesign's data/interaction scope per the spec's "pure visual/motion redesign" framing) —
  only restyle whatever refresh affordance already exists. If a `PullToRefreshBox` already exists
  from prior work, restyle its indicator to use `AccentPrimary` and leave its behavior untouched.
- Caregiver card's two CTAs (Message, Live pulse) use the primary/secondary button styles wired
  to `AccentPrimary` per the existing button components in `ui/components/CommonComponents.kt`
  (do not modify that file in this task — Phase 1 does not touch `CommonComponents.kt`; if a
  button there needs a new visual variant for this screen, use `Modifier.glassCard` directly on a
  clickable `Box` instead of adding to the shared component file).
- Section headers use `MaterialTheme.typography.titleLarge` (H4/section-header role), body text
  `MaterialTheme.typography.bodyLarge` (13.5sp Body), money/vitals figures use `TypeNumericM` or
  `TypeNumericL` per the handoff (import from `com.carepulse.app.ui.theme`).

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/customer/CustomerDashboardScreen.kt
git commit -m "style: apply glass-on-grid tokens to Family home (CustomerDashboardScreen)"
```

---

### Task 10: Redesign CaregiverDetailScreen.kt (search + caregiver profile)

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/CaregiverDetailScreen.kt`

**Interfaces:**
- Consumes: same as Task 9.
- Produces: no signature changes — read the file first to confirm current signature.

- [ ] **Step 1: Read the current file fully**

```bash
cat app/src/main/java/com/carepulse/app/ui/screens/customer/CaregiverDetailScreen.kt
```

- [ ] **Step 2: Apply glass-on-grid treatment**

Per the handoff: this repo's `CaregiverDetailScreen` may combine what the handoff treats as two
separate screens (search results + profile detail) — read the actual file structure first and
adapt accordingly, do not force a two-screen split that doesn't exist in this codebase.

- If there's a filter-chip row, style each chip as `Modifier.glassCard(Radii.Chip, hazeState)`
  when unselected, filled `AccentPrimary` background with white text when selected — `Radii.Chip`
  is 999dp (fully rounded).
- Profile header block (if present): accent-red (`AccentPrimary`) background,
  `RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)` (28dp bottom corners per spec),
  hero avatar `Radii.AvatarHero` (24dp) radius.
- Any stats row uses a `2.dp` rule at `Color.White.copy(alpha = 0.3f)` between stats, per spec,
  when laid directly on the accent-red header.
- One verification/trust line only — do not add multiple trust badges (spec: "trust is
  deliberately minimal per the client").
- 7-day availability strip (if present): style as a horizontal row of `Radii.Chip`-shaped day
  cells, one selected with `AccentPrimary` fill.
- Everything else (cards, lists) follows the same `Modifier.glassCard(Radii.Card, hazeState)` /
  `Spacing.CardGap` pattern as Task 9.

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/customer/CaregiverDetailScreen.kt
git commit -m "style: apply glass-on-grid tokens to caregiver search/profile screen"
```

---

### Task 11: Redesign BookingScreen.kt

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/BookingScreen.kt`

**Interfaces:**
- Consumes: same as Task 9.
- Produces: no signature changes.

- [ ] **Step 1: Read the current file fully**

```bash
cat app/src/main/java/com/carepulse/app/ui/screens/customer/BookingScreen.kt
```

- [ ] **Step 2: Verify the total is actually computed, not hardcoded — fix if not**

Per the spec, the booking total MUST be `rate × hours` where a 6-hour shift = 6, a 12-hour shift
= 12, and a Night 12h shift = 12 (i.e. hours is just the numeric shift length in all three
cases), formatted as `LKR 22,200` with thousands separators. Search the file for where the total
is computed or displayed:

```bash
grep -n "total\|LKR\|rate" app/src/main/java/com/carepulse/app/ui/screens/customer/BookingScreen.kt
```

If the total is already computed from `rate * hours` (this was likely already correct from prior
work, since this is existing app logic, not new for this redesign), leave the computation
untouched — only restyle. If you find a hardcoded total, that is a pre-existing bug outside this
redesign's stated scope (visual/motion only) — do NOT silently fix it as part of this styling
task; instead note it explicitly in your final report as a "found but not fixed per scope"
item, so a human can decide whether to spin it into a separate fix.

- [ ] **Step 3: Apply glass-on-grid treatment**

- The booking flow's container (bottom sheet or full screen, whichever this repo actually uses)
  gets `Radii.Sheet` (30dp, top corners only) if it's a bottom sheet, or `GlassGround()` +
  `Modifier.glassCard(Radii.CardLarge, hazeState)` per-section if it's a full screen — read the
  file to determine which and apply accordingly.
- Sheet padding (if a sheet): `Spacing.SheetSides` (20dp) horizontal, `Spacing.SheetBottom`
  (26dp) bottom.
- Step 3 (confirmed) green tick: if an existing icon/checkmark marks confirmation, wrap it in a
  scale-in animation (`androidx.compose.animation.core.animateFloatAsState` from 0f to 1f,
  `Motion.BottomSheet` duration, `Motion.Emphasized` easing) if it doesn't already animate.
- Money figures use `TypeNumericL` (31-34sp per spec) or `TypeNumericM` depending on visual
  hierarchy in the existing layout.

- [ ] **Step 4: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/customer/BookingScreen.kt
git commit -m "style: apply glass-on-grid tokens to booking screen"
```

---

### Task 12: Redesign CareRequestScreen.kt

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/CareRequestScreen.kt`

**Interfaces:**
- Consumes: same as Task 9.
- Produces: no signature changes.

- [ ] **Step 1: Read the current file fully**

```bash
cat app/src/main/java/com/carepulse/app/ui/screens/customer/CareRequestScreen.kt
```

- [ ] **Step 2: Apply glass-on-grid treatment**

`GlassGround()` background, form container(s) as `Modifier.glassCard(Radii.CardLarge, hazeState)`,
segmented selectors (gender: Female/Male/No preference; location: Hospital/Home) styled as a
`Row` of `Radii.Chip`-shaped segments with `AccentPrimary` fill on the selected segment — if the
existing implementation already uses a different selector style (e.g. radio buttons), restyle
that existing control rather than replacing it with a new chip-based one, unless the current
control cannot visually support the segmented look without a larger rewrite (use judgement, note
your choice in the report). Submit button uses `AccentPrimary`; toast on submit uses the existing
toast mechanism restyled per Task-13's toast spec (see next task) if this screen already shows
one, otherwise leave the return-to-home behavior as-is (adding a new toast where none exists is a
feature addition, not in scope here — restyle what's already there).

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/customer/CareRequestScreen.kt
git commit -m "style: apply glass-on-grid tokens to care request screen"
```

---

### Task 13: Redesign PulseDashboardScreen.kt (Family pulse)

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/PulseDashboardScreen.kt`

**Interfaces:**
- Consumes: same as Task 9.
- Produces: no signature changes.

- [ ] **Step 1: Read the current file fully**

```bash
cat app/src/main/java/com/carepulse/app/ui/screens/customer/PulseDashboardScreen.kt
```

- [ ] **Step 2: Apply glass-on-grid treatment**

- `GlassGround()` background, glass-card stat tiles (`Modifier.glassCard(Radii.StatTile, hazeState)`,
  20dp radius, per spec's "statTile" role — nested stat tiles specifically use `GlassFillSubtle`
  instead of `GlassFill` for their background, since the spec calls out `GlassFillSubtle` as "for
  nested stat tiles").
- If this screen already has a heart-rate sparkline (`Canvas`/`drawPath`), animate its draw with
  a dash-offset animation from 420 to 0 over `Motion.SparklineDraw` (1300ms) using
  `Motion.Emphasized` easing, via `androidx.compose.ui.graphics.PathEffect.dashPathEffect` with
  an animated phase, or an equivalent progressive-reveal technique appropriate to however the
  path is currently drawn — read the existing drawing code first and adapt the animation to it
  rather than replacing the whole chart implementation (per spec: "Rebuild with Canvas/drawPath
  from real data" refers to the *original prototype's* hand-drawn illustrative path, not this
  repo's already-real chart — if this repo's sparkline already draws from real vitals data, only
  add the draw-in animation, do not touch the data source).
- Numeric vitals figures use `TypeNumericL`/`TypeNumericM` per the handoff.

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/customer/PulseDashboardScreen.kt
git commit -m "style: apply glass-on-grid tokens to Family pulse screen"
```

---

### Task 14: Redesign MessagesScreen.kt and ConversationScreen.kt

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/messages/MessagesScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/messages/ConversationScreen.kt`

**Interfaces:**
- Consumes: same as Task 9.
- Produces: no signature changes.

- [ ] **Step 1: Read both files fully**

```bash
cat app/src/main/java/com/carepulse/app/ui/screens/messages/MessagesScreen.kt
cat app/src/main/java/com/carepulse/app/ui/screens/messages/ConversationScreen.kt
```

- [ ] **Step 2: Apply glass-on-grid treatment**

- `MessagesScreen`: `GlassGround()` background, each thread row as a `Modifier.glassCard(Radii.Card, hazeState)`
  or, if rows are meant to look like a flat list rather than individual cards (check the current
  layout — a thread list is often one glass card containing all rows, not one card per row), wrap
  the whole list in one `Modifier.glassCard(Radii.CardLarge, hazeState)` with `Rule`-colored
  dividers between rows (`HorizontalDivider(color = MaterialTheme.colorScheme.outline)`).
- `ConversationScreen`: chat bubbles keep their existing shape logic but use `AccentPrimary` for
  the current user's sent-message bubbles and `GlassFillSubtle`/`MaterialTheme.colorScheme.surfaceVariant`
  for received bubbles; input field container as `Modifier.glassCard(Radii.Input, hazeState)`.

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/messages/MessagesScreen.kt app/src/main/java/com/carepulse/app/ui/screens/messages/ConversationScreen.kt
git commit -m "style: apply glass-on-grid tokens to messages and chat screens"
```

---

### Task 15: Redesign ActivityScreen.kt

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/activity/ActivityScreen.kt`

**Interfaces:**
- Consumes: same as Task 9.
- Produces: no signature changes.

- [ ] **Step 1: Read the current file fully**

```bash
cat app/src/main/java/com/carepulse/app/ui/screens/activity/ActivityScreen.kt
```

- [ ] **Step 2: Apply glass-on-grid treatment**

`GlassGround()` background, list rows/cards per the same `Modifier.glassCard(Radii.Card, hazeState)`
pattern as prior screens. If this screen shows invoice/payment status (per the handoff's
"Activity + payments" mapping), status pills use `StatusAvailable`/`StatusDueFill`+`StatusDueText`
per spec — a "Paid" pill uses `StatusAvailable` fill-at-16%-alpha/full-strength-text pattern (see
Global Constraints), a "Due" pill uses `StatusDueFill`/`StatusDueText` directly (already
pre-computed at the spec's 14% alpha). Every status pill keeps its text label.

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/activity/ActivityScreen.kt
git commit -m "style: apply glass-on-grid tokens to activity screen"
```

---

### Task 16: Full build + repo-wide consistency check

**Files:** none (verification-only task).

- [ ] **Step 1: Full assemble**

```bash
./gradlew :app:assembleDebug
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Confirm no Caregiver/Agency screen is visually broken**

```bash
grep -rln "AccentContainerLight\|AccentContainerDark\|CardSurface\|SurfaceLow\|SurfaceHigh\|SurfaceHighest\|TextSecondary\|WarningAmber\|InfoBlue\|DarkBackground\|DarkSurface\|DarkBorder\|DarkOnSurface" app/src/main/java/com/carepulse/app/
```
Expected: no output (Task 4 already fixed every reference repo-wide; this re-confirms no later
task reintroduced one).

- [ ] **Step 3: Install and click through on a connected device/emulator if available**

```bash
adb devices -l
```
If a device is attached, `./gradlew :app:installDebug` and walk: role select → login → Family
home → search/caregiver profile → booking → care request → pulse → messages → chat → activity →
settings, toggling light/dark in Settings. Confirm: glass cards render with visible blur (or the
opaque fallback if `LocalGlassEnabled` is toggled off), the ruled grid + accent squares show
behind content, Archivo renders (visually distinct from Roboto's default look), the bottom nav's
indicator slides between tabs, and Caregiver/Agency-role home screens (not redesigned this phase)
still render without missing colors or crashes.

- [ ] **Step 4: Report results**

Summarize pass/fail per screen; do not claim "done" until this walkthrough is actually performed
and its output observed, or explicitly note it could not be performed (no device available) —
per verification-before-completion practice.

---
