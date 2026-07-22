# CarePulse — Fintech-Minimal Redesign

## Context
CarePulse (native Android, Kotlin + Jetpack Compose, Material 3) currently uses a Navy/Teal
gradient theme from Phase 2/3 work. The user wants a full visual replacement inspired by a
premium fintech-app reference: white background, minimal palette, large rounded cards, soft
low-elevation shadows, spacious 8pt-grid layouts, and a floating rounded bottom nav — used as
design *language*, not copied literally.

**Scope decision:** Replace the existing theme entirely (not a parallel variant). Implement
everything in one coordinated pass: tokens → components → screens.

**Platform note:** This repo is native Android only (no iOS/KMP target). The reference spec's
iOS-specific items (SF Pro Display, iPhone SE/Plus/Pro Max, iPad) are not buildable here.
Android gets the full treatment (Material 3, Compose, dynamic color, adaptive layout,
Roboto/Google Sans). iOS parity is out of scope unless a separate iOS project exists.

**Bottom nav:** Keeps CarePulse's existing labels/structure — Home, Activity, Messages, Pulse,
Settings — restyled as a floating rounded pill nav with outlined icons. Not renamed to the
reference's generic Home/Explore/Analytics/Notifications/Profile.

## Design tokens (`ui/theme/`)

- **Color.kt** (rewrite): white background, black/dark-gray primary text, gray secondary text,
  green (success), orange (warning/progress), blue (info), border gray `#F2F2F2`. Full light +
  dark palettes. Drop Navy/Teal brand colors and button gradients.
- **Spacing.kt** (new): 8pt system — screen padding 20–24dp, card padding 16–20dp, section
  spacing 24–32dp.
- **Radii.kt** (new): cards 24dp, buttons 18dp, inputs 16dp, bottom nav 26dp, dialogs 28dp.
- **Type.kt** (rewrite): Android type scale using Roboto/Google Sans — Heading 28 Bold, Section
  Title 20 SemiBold, Body 16 Regular, Caption 13 Regular, Small Label 12 Medium. Support Dynamic
  Type (Compose font-scale respects system settings by default; verify no clipping at 200%).
- **Theme.kt**: keep `ThemeMode` (System/Light/Dark) + dynamic-color-on-S+ pattern from Phase 2;
  swap the brand `ColorScheme`s for the new minimal palettes. Shadows: soft, low elevation only
  (no glass/neumorphism/heavy gradients).
- **Motion.kt**: keep existing duration/easing tokens; used for fade/scale/slide only — no new
  flashy animation patterns.

## Component library (`ui/components/`)

Split `CommonComponents.kt` into focused files under `ui/components/`:
- `buttons/`: Primary, Secondary, Outlined, Text, Icon, FAB (flat/tonal, no gradients; min touch
  target 48x48dp).
- `cards/`: base Card, StatCard, DashboardCard, ProgressCard — soft shadow, 24dp radius, generous
  padding.
- `inputs/`: SearchBar, TextField, Dropdown.
- `selection/`: Chip, Badge, Switch, Checkbox, RadioButton.
- `navigation/`: Tabs, floating rounded BottomNav (26dp radius).
- `overlays/`: BottomSheet, Dialog (28dp radius), Snackbar, Toast.
- `progress/`: CircularProgress, LinearProgress.
- `lists/`: SettingsRow, generic list row.
- `states/`: EmptyState, ErrorState, LoadingSkeleton.
- `charts/`: rounded bar chart, rounded line chart, circular progress ring — minimal axis
  styling, reuse existing chart data where present.

All components: outlined rounded icons, WCAG AA contrast in both themes, 48x48dp minimum touch
targets.

## Screens

Every existing screen restyled to the new tokens/components — no new screens added. Representative
set: Login/Auth, Customer/Caregiver/Agency Dashboards, Messages/Chat, Pulse/Vitals, Reminders,
Settings, Language Picker, home-screen Vitals widget. Existing navigation graph and business logic
unchanged — this is a visual/component-layer pass.

## Animations

Reuse `Motion.kt` tokens for: fade, scale, slide, card-elevation-on-press, ripple, FAB expand,
progress-bar fill. No new flashy/complex animations — matches existing Phase 2/3 shared-element
and gesture work already planned (not duplicated here).

## Out of scope
- iOS build target (doesn't exist in this repo).
- New screens/features beyond the current app surface.
- Backend/Firestore/data-layer changes.
- Tablet-specific layouts beyond what Compose's adaptive defaults already provide (flagged as a
  possible follow-up, not committed here).

## Verification
1. Every screen renders correctly in both light and dark theme, no leftover Navy/Teal color
   literals (grep `Color(0xFF` across `ui/screens/` should only match the new minimal palette).
2. Bottom nav renders as a floating rounded pill, all 5 existing destinations reachable.
3. Buttons/cards/inputs match the radii/spacing/shadow spec on at least one full screen (Settings
   or Dashboard) reviewed visually before rolling out to the rest.
4. TalkBack announces all interactive elements; touch targets ≥48x48dp.
5. Font scale 200% — no clipped text on Dashboard, Settings, Reminders.
