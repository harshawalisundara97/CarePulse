# CarePulse — Professional Color Rebrand

**Date:** 2026-06-04  
**Status:** Approved  
**Scope:** Replace entire pastel palette with Navy + Teal professional color system

---

## Goal

Replace all pastel colors (mint, lavender, peach, cream) with a professional Navy + Teal
palette. No gradients. Result must look like a credible B2B healthcare SaaS.

---

## New Color System

| Token | Hex | Usage |
|-------|-----|-------|
| `NavyPrimary` | `#1A2B4A` | TopAppBar background, nav bar, text headers |
| `NavyDark` | `#111E33` | Pressed states, dark surfaces |
| `TealAccent` | `#0D9488` | Primary buttons, active chips, links, icons |
| `TealDark` | `#0B7A70` | Pressed/hover state for teal buttons |
| `Background` | `#F5F7FA` | Screen background (replaces CreamBackground) |
| `CardSurface` | `#FFFFFF` | Card backgrounds (unchanged) |
| `BorderLine` | `#E2E8F0` | Card borders, dividers (replaces soft borders) |
| `TextPrimary` | `#1A2B4A` | Body text, card titles (replaces InkPrimary) |
| `TextSecondary` | `#64748B` | Subtitles, metadata (replaces InkSecondary) |
| `DangerRed` | `#EF4444` | Error states, destructive actions |
| `WarningAmber` | `#F59E0B` | In-progress / pending status chips |
| `SuccessGreen` | `#10B981` | Completed / success states |

**Removed entirely:** `PastelMint`, `PastelMintDeep`, `SoftLavender`, `SoftPeach`,
`CreamBackground`, `InkPrimary`, `InkSecondary`, `DangerCoral`, `PulseRed`

---

## Files to Change

### 1. `ui/theme/Color.kt`
Full replacement of all color constants with the new token set above.

### 2. `ui/theme/Theme.kt`
Update Material3 `ColorScheme` to map new tokens:
- `primary` → `TealAccent`
- `onPrimary` → `Color.White`
- `secondary` → `NavyPrimary`
- `background` → `Background`
- `surface` → `CardSurface`
- `error` → `DangerRed`

### 3. `ui/components/CommonComponents.kt`
Replace all direct color references:
- `PastelMint` / `PastelMintDeep` → `TealAccent`
- `CreamBackground` → `Background`
- `InkPrimary` → `TextPrimary`
- `InkSecondary` → `TextSecondary`
- `SoftLavender` / `SoftPeach` → `BorderLine` or `CardSurface`
- `PulseRed` / `DangerCoral` → `DangerRed`
- `SuccessGreen` stays but updated hex to `#10B981`

### 4. All screen files (update direct color references)
- `LoginScreen.kt`
- `CaregiverRegistrationScreen.kt`
- `RoleSelectionScreen.kt`
- `CustomerDashboardScreen.kt`
- `CaregiverDetailScreen.kt`
- `BookingScreen.kt`
- `CareRequestScreen.kt`
- `PulseDashboardScreen.kt`
- `VideoCallScreen.kt`
- `CaregiverDashboardScreen.kt`
- `ShiftSummaryScreen.kt`
- `EditProfileScreen.kt`
- `AgencyScreens.kt`
- `ActivityScreen.kt`
- `MessagesScreen.kt`
- `ConversationScreen.kt`
- `SettingsScreen.kt`

---

## Rules

- **No gradients** anywhere. Solid colors only.
- **No pastel hex values** left in any file after this change.
- `PastelCard` composable in `CommonComponents.kt` must be renamed to `SurfaceCard`
  (or kept as `PastelCard` with the background changed to `CardSurface` + `BorderLine` border).
- TopAppBar background: `NavyPrimary` with white title text.
- Bottom nav bar background: `NavyPrimary`, active icon: `TealAccent`, inactive: white at 40% opacity.
- Primary button: `TealAccent` fill, white text, 8dp corner radius.
- Outlined button: `TealAccent` border + text, transparent fill.
- Status chips: `TealAccent`=active/assigned, `WarningAmber`=pending/in-progress, `DangerRed`=cancelled, `BorderLine`=gray/inactive.

---

## Out of Scope

- No layout changes
- No new screens
- No typography changes
- No icon changes
