# Professional Color Rebrand Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace every pastel color in CarePulse with a professional Navy + Teal palette — no gradients, no pastels left anywhere.

**Architecture:** Color tokens live in `Color.kt`, wired into Material3 `ColorScheme` in `Theme.kt`. Shared components in `CommonComponents.kt` must be updated first, then all screen files updated to use the new tokens. No layout or structure changes — color only.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, `androidx.compose.ui.graphics.Color`

---

## New Token Reference (use in every task)

| Old name | New name | Hex |
|---|---|---|
| `PastelMintDeep` | `TealAccent` | `#0D9488` |
| `PastelMint` | `TealLight` | `#CCFBF1` |
| `SoftLavender` | `BorderLine` | `#E2E8F0` |
| `SoftPeach` | `BorderLine` | `#E2E8F0` |
| `CreamBackground` | `Background` | `#F5F7FA` |
| `CardSurface` | `CardSurface` | `#FFFFFF` (unchanged) |
| `InkPrimary` | `NavyPrimary` | `#1A2B4A` |
| `InkSecondary` | `TextSecondary` | `#64748B` |
| `DangerCoral` | `DangerRed` | `#EF4444` |
| `PulseRed` | `DangerRed` | `#EF4444` |
| `SuccessGreen` | `SuccessGreen` | `#10B981` (updated hex) |
| *(new)* | `NavyDark` | `#111E33` |
| *(new)* | `TealDark` | `#0B7A70` |
| *(new)* | `WarningAmber` | `#F59E0B` |

**Gradient rule:** Every `Brush.verticalGradient` / `Brush.horizontalGradient` / `Brush.linearGradient` must be replaced with a solid `Background` (`#F5F7FA`) or `CardSurface` (`#FFFFFF`) color.

---

## File Map

| File | Action |
|---|---|
| `ui/theme/Color.kt` | Full replacement |
| `ui/theme/Theme.kt` | Update `ColorScheme` mappings |
| `ui/components/CommonComponents.kt` | Replace all color refs, remove gradient imports |
| `ui/screens/auth/LoginScreen.kt` | Replace color imports + refs |
| `ui/screens/auth/CaregiverRegistrationScreen.kt` | Replace color imports + refs |
| `ui/screens/onboarding/RoleSelectionScreen.kt` | Replace colors + remove gradients |
| `ui/screens/customer/CustomerDashboardScreen.kt` | Replace colors + remove gradient |
| `ui/screens/customer/CaregiverDetailScreen.kt` | Replace color refs |
| `ui/screens/customer/BookingScreen.kt` | Replace color refs |
| `ui/screens/customer/CareRequestScreen.kt` | Replace color refs |
| `ui/screens/customer/PulseDashboardScreen.kt` | Replace color refs |
| `ui/screens/customer/VideoCallScreen.kt` | Replace color refs |
| `ui/screens/caregiver/CaregiverDashboardScreen.kt` | Replace color refs |
| `ui/screens/caregiver/ShiftSummaryScreen.kt` | Replace color refs |
| `ui/screens/caregiver/EditProfileScreen.kt` | Replace color refs |
| `ui/screens/agency/AgencyScreens.kt` | Replace color refs |
| `ui/screens/activity/ActivityScreen.kt` | Replace color refs |
| `ui/screens/messages/MessagesScreen.kt` | Replace color refs |
| `ui/screens/messages/ConversationScreen.kt` | Replace color refs |
| `ui/screens/settings/SettingsScreen.kt` | Replace color refs |

---

## Task 1: Replace Color.kt and Theme.kt

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/theme/Theme.kt`

- [ ] **Step 1: Replace Color.kt entirely**

Open `app/src/main/java/com/carepulse/app/ui/theme/Color.kt` and replace the entire file content with:

```kotlin
package com.carepulse.app.ui.theme

import androidx.compose.ui.graphics.Color

// CarePulse Professional Palette — Navy + Teal
val NavyPrimary     = Color(0xFF1A2B4A)   // TopAppBar, nav bar, primary text
val NavyDark        = Color(0xFF111E33)   // Pressed / dark surfaces
val TealAccent      = Color(0xFF0D9488)   // Buttons, active chips, icons
val TealDark        = Color(0xFF0B7A70)   // Pressed teal
val TealLight       = Color(0xFFCCFBF1)   // Teal tint backgrounds

val Background      = Color(0xFFF5F7FA)   // Screen background
val CardSurface     = Color(0xFFFFFFFF)   // Card backgrounds
val BorderLine      = Color(0xFFE2E8F0)   // Borders, dividers

val TextPrimary     = Color(0xFF1A2B4A)   // Body text, card titles
val TextSecondary   = Color(0xFF64748B)   // Subtitles, metadata

val DangerRed       = Color(0xFFEF4444)   // Error, destructive
val WarningAmber    = Color(0xFFF59E0B)   // Pending, in-progress
val SuccessGreen    = Color(0xFF10B981)   // Completed, success
```

- [ ] **Step 2: Replace Theme.kt ColorScheme**

Open `app/src/main/java/com/carepulse/app/ui/theme/Theme.kt` and replace the `CarePulseColors` block:

```kotlin
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
```

- [ ] **Step 3: Commit**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/src/main/java/com/carepulse/app/ui/theme/Color.kt \
        app/src/main/java/com/carepulse/app/ui/theme/Theme.kt
git commit -m "style: replace pastel palette with Navy + Teal color tokens"
```

---

## Task 2: Update CommonComponents.kt

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/components/CommonComponents.kt`

- [ ] **Step 1: Replace all pastel imports**

Find the import block at the top of `CommonComponents.kt`. Replace all pastel-related imports with:

```kotlin
import com.carepulse.app.ui.theme.TealAccent
import com.carepulse.app.ui.theme.TealDark
import com.carepulse.app.ui.theme.TealLight
import com.carepulse.app.ui.theme.NavyPrimary
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.BorderLine
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.ui.theme.DangerRed
import com.carepulse.app.ui.theme.SuccessGreen
```

Remove these imports (they no longer exist):
```
import com.carepulse.app.ui.theme.PastelMint
import com.carepulse.app.ui.theme.PastelMintDeep
import com.carepulse.app.ui.theme.SoftLavender
import com.carepulse.app.ui.theme.SoftPeach
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.CreamBackground
```

Also remove any `import androidx.compose.ui.graphics.Brush` if present (no gradients).

- [ ] **Step 2: Update PrimaryButton colors**

Find the `PrimaryButton` composable. Replace the `ButtonDefaults.buttonColors` call:

```kotlin
colors = ButtonDefaults.buttonColors(
    containerColor = TealAccent,
    contentColor = Color.White,
    disabledContainerColor = TealAccent.copy(alpha = 0.4f)
),
```

- [ ] **Step 3: Update CarePulseTextField colors**

Find the `CarePulseTextField` composable. Replace the `TextFieldDefaults.colors` / `OutlinedTextFieldDefaults.colors` call:

```kotlin
colors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TealAccent,
    unfocusedBorderColor = BorderLine,
    focusedLabelColor = TealAccent,
    unfocusedLabelColor = TextSecondary,
    cursorColor = TealAccent,
)
```

- [ ] **Step 4: Update CarePulseHeader colors**

Find the `CarePulseHeader` composable (the one with `title` and optional `subtitle`):

```kotlin
Text(title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
// ...
Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
```

- [ ] **Step 5: Update PastelCard — background and border**

Find the `PastelCard` composable. Replace the rotating pastel colors list with solid white + border:

```kotlin
@Composable
fun PastelCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val cardModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .border(1.dp, BorderLine, shape)
        .background(CardSurface)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    Column(modifier = cardModifier, content = content)
}
```

Remove any `index % 3` pastel color rotation logic.

- [ ] **Step 6: Update PastelChip colors**

Find the `PastelChip` composable. Replace colors:

```kotlin
@Composable
fun PastelChip(
    label: String,
    selected: Boolean = false,
    color: Color = BorderLine,
    onClick: (() -> Unit)? = null
) {
    val bg = if (selected) TealAccent else color
    val textColor = if (selected) Color.White else TextPrimary
    // ... rest unchanged
}
```

- [ ] **Step 7: Update RatingRow colors**

Find the `RatingRow` composable (stars + count):

```kotlin
// star tint: WarningAmber (replace any PastelMint/InkPrimary usage)
// count text color: TextSecondary
```

Replace all `InkPrimary` references in RatingRow with `TextPrimary` and `InkSecondary` with `TextSecondary`.

- [ ] **Step 8: Update loading shimmer / any remaining references**

Search for any remaining `SoftLavender`, `PastelMint`, `InkPrimary`, `InkSecondary`, `CreamBackground` inside the file and replace:
- `SoftLavender.copy(alpha = ...)` → `BorderLine.copy(alpha = ...)`
- `InkPrimary` → `TextPrimary`
- `InkSecondary` → `TextSecondary`

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/components/CommonComponents.kt
git commit -m "style: update CommonComponents to Navy + Teal palette"
```

---

## Task 3: Update Auth + Onboarding Screens

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/auth/LoginScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/auth/CaregiverRegistrationScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/onboarding/RoleSelectionScreen.kt`

- [ ] **Step 1: Fix LoginScreen.kt imports and color refs**

Remove old imports:
```
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMintDeep
```

Add new imports:
```kotlin
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.NavyPrimary
import com.carepulse.app.ui.theme.TealAccent
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
```

Then replace all usages in the file body:
- `CreamBackground` → `Background`
- `InkPrimary` → `TextPrimary`
- `InkSecondary` → `TextSecondary`
- `PastelMintDeep` → `TealAccent`

- [ ] **Step 2: Fix CaregiverRegistrationScreen.kt imports and color refs**

Apply the same import swap as Step 1. Replace all body usages:
- `CreamBackground` → `Background`
- `InkPrimary` → `TextPrimary`
- `InkSecondary` → `TextSecondary`
- `PastelMintDeep` → `TealAccent`
- `PastelMint` → `TealAccent`
- `SoftLavender` → `BorderLine`

- [ ] **Step 3: Fix RoleSelectionScreen.kt — remove gradients + update colors**

Remove old imports including `Brush`:
```
import androidx.compose.ui.graphics.Brush
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMint
import com.carepulse.app.ui.theme.SoftLavender
import com.carepulse.app.ui.theme.SoftPeach
```

Add:
```kotlin
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.BorderLine
import com.carepulse.app.ui.theme.NavyPrimary
import com.carepulse.app.ui.theme.TealAccent
import com.carepulse.app.ui.theme.TealLight
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
```

Replace gradient backgrounds with solid colors:
- `Brush.verticalGradient(listOf(CreamBackground, PastelMint.copy(alpha = 0.25f)))` → `Background`
- `Brush.linearGradient(listOf(PastelMint, SoftLavender))` → `TealAccent`
- `gradient = listOf(PastelMint, Color(0xFFC8F2DA))` → remove gradient, use solid `TealLight`

For any `.background(Brush.*)` call: change to `.background(Background)` or `.background(TealLight)`.

Replace all body color refs:
- `InkPrimary` → `TextPrimary`
- `InkSecondary` → `TextSecondary`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/auth/LoginScreen.kt \
        app/src/main/java/com/carepulse/app/ui/screens/auth/CaregiverRegistrationScreen.kt \
        app/src/main/java/com/carepulse/app/ui/screens/onboarding/RoleSelectionScreen.kt
git commit -m "style: update auth + onboarding screens to professional palette"
```

---

## Task 4: Update Customer Screens

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/CustomerDashboardScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/CaregiverDetailScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/BookingScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/CareRequestScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/PulseDashboardScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/VideoCallScreen.kt`

For each file:

- [ ] **Step 1: Fix imports in each file**

Remove any of these that appear:
```
import androidx.compose.ui.graphics.Brush
import com.carepulse.app.ui.theme.PastelMint
import com.carepulse.app.ui.theme.PastelMintDeep
import com.carepulse.app.ui.theme.SoftLavender
import com.carepulse.app.ui.theme.SoftPeach
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.DangerCoral
import com.carepulse.app.ui.theme.PulseRed
import com.carepulse.app.ui.theme.SuccessGreen
```

Add whichever new tokens are needed by that file:
```kotlin
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.BorderLine
import com.carepulse.app.ui.theme.CardSurface
import com.carepulse.app.ui.theme.NavyPrimary
import com.carepulse.app.ui.theme.TealAccent
import com.carepulse.app.ui.theme.TealLight
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.ui.theme.DangerRed
import com.carepulse.app.ui.theme.SuccessGreen
import com.carepulse.app.ui.theme.WarningAmber
```

- [ ] **Step 2: Replace body color references in each file**

Apply these replacements throughout each file's body:
- `PastelMintDeep` → `TealAccent`
- `PastelMint` → `TealLight`
- `SoftLavender` → `BorderLine`
- `SoftPeach` → `BorderLine`
- `CreamBackground` → `Background`
- `InkPrimary` → `TextPrimary`
- `InkSecondary` → `TextSecondary`
- `DangerCoral` → `DangerRed`
- `PulseRed` → `DangerRed`
- `SuccessGreen` → `SuccessGreen` (just update the hex via the token)

- [ ] **Step 3: Remove gradients in CustomerDashboardScreen.kt**

Find this line (around line 199):
```kotlin
.background(Brush.horizontalGradient(listOf(PastelMint, SoftLavender)))
```
Replace with:
```kotlin
.background(TealLight)
```

Remove the `Brush` import if it's no longer used.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/customer/
git commit -m "style: update all customer screens to professional palette"
```

---

## Task 5: Update Caregiver, Agency, Activity, Messages, Settings Screens

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/caregiver/CaregiverDashboardScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/caregiver/ShiftSummaryScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/caregiver/EditProfileScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/agency/AgencyScreens.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/activity/ActivityScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/messages/MessagesScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/messages/ConversationScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/settings/SettingsScreen.kt`

- [ ] **Step 1: Fix imports in each file**

For each file, remove the old pastel imports and add the new tokens (same swap as Task 4 Step 1).

- [ ] **Step 2: Replace body color references in each file**

Apply the same replacements as Task 4 Step 2 to every file listed above.

- [ ] **Step 3: Special fix — ConversationScreen.kt chat bubbles**

Find the `MessageBubble` composable (around line 135):
```kotlin
val bubbleColor = if (isMine) PastelMintDeep else Color.White
val textColor = if (isMine) Color.White else InkPrimary
```
Replace with:
```kotlin
val bubbleColor = if (isMine) TealAccent else CardSurface
val textColor = if (isMine) Color.White else TextPrimary
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/carepulse/app/ui/screens/caregiver/ \
        app/src/main/java/com/carepulse/app/ui/screens/agency/ \
        app/src/main/java/com/carepulse/app/ui/screens/activity/ \
        app/src/main/java/com/carepulse/app/ui/screens/messages/ \
        app/src/main/java/com/carepulse/app/ui/screens/settings/
git commit -m "style: update remaining screens to professional palette"
```

---

## Task 6: Verify no pastel references remain + Build + Install

**Files:** Read-only verification

- [ ] **Step 1: Check for any remaining pastel references**

```bash
grep -r "PastelMint\|SoftLavender\|SoftPeach\|CreamBackground\|InkPrimary\|InkSecondary\|DangerCoral\|PulseRed\|Brush.horizontalGradient\|Brush.verticalGradient\|Brush.linearGradient" \
  /Users/ranjana/Harsha/CarePulse/app/src/main/java --include="*.kt"
```

Expected output: **no lines** (empty). If any show up, fix them before proceeding.

- [ ] **Step 2: Build**

```bash
cd /Users/ranjana/Harsha/CarePulse
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Install on tablet**

```bash
./gradlew :app:installDebug
```

Expected: `Installed on 1 device.`

- [ ] **Step 4: Final commit + push**

```bash
git push origin feature/firebase-bottom-nav-polish
```
