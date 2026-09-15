# CarePulse — Glass-on-Grid Redesign (Phase 1: Family role)

## Context
CarePulse (native Android, Kotlin + Jetpack Compose, Material 3) currently ships the
"fintech-minimal" theme merged in PR #3 (white background, flat colors, no glass). A client-
approved high-fidelity design handoff (`design_handoff_carepulse_mobile/`, provided as a zip)
specifies a full replacement: a "glass-on-grid" direction — translucent blurred cards over a
visible ruled ground and off-screen accent blocks, on Modernist typographic bones (Archivo font,
flush-left uppercase labels, 2px rules, one red accent colour).

**Scope decision:** Full replacement of the fintech-minimal theme, not a parallel variant.
Phase 1 (this spec) covers the shared token/infra layer (applies app-wide immediately) plus a
full end-to-end redesign of every **Family-role** screen. Caregiver-role and Agency-role screens
automatically inherit the new tokens (so nothing looks visually broken/mixed) but are not
individually redesigned until Phase 2/3, mirroring the "foundation + one proof role" approach
used for the previous redesign.

**Data scope:** Pure visual/motion redesign. Each screen keeps whatever data source it already
uses (Firestore repo, ViewModel, etc.) — no new data-layer work, no wiring of previously-mock
data to real sources as part of this pass.

**CLAUDE.md conflict:** The current CLAUDE.md instructs "no glassmorphism." Since this redesign
is deliberately glass-based at the client's explicit request, CLAUDE.md's Frontend Design
Language section must be replaced before implementation starts (Task 0 of the implementation
plan), using the wording the handoff doc itself suggests.

## New dependencies & assets
- `dev.chrisbanes.haze` — backdrop blur for Compose (no first-party equivalent exists). Supports
  API 24+, matching this project's `minSdk = 24`.
- **Archivo** variable font (Google Fonts, OFL license), weights 400/600/700/800, bundled as an
  Android font resource (`res/font/`) and wired into `Type.kt` via `FontFamily`. Do not substitute
  Inter or Roboto — the handoff is explicit that Archivo is required.
- No new data/network dependencies.

## Design tokens (`ui/theme/`)

### Color.kt (full rewrite)
Exact values from the handoff, both light and dark:

| Token | Light | Dark | Use |
|---|---|---|---|
| `AccentPrimary` | `#EC3013` | `#FF563C` | Primary actions, active nav, accent field block |
| `AccentPressed` | `#AE1800` | `#FF7A66` | Pressed/link-hover |
| `TextPrimary` | `#201E1D` | `#F4F2F1` | Body and headings |
| `TextMuted` | `#201E1D` @55% | `#F4F2F1` @55% | Secondary text, labels |
| `Background` | `#F3F2F2` | `#141312` | Screen background |
| `GroundDeep` | `#E6E3E1` | `#0D0C0C` | Behind-glass ground |
| `GlassFill` | white @62% | white @7% | Standard glass card |
| `GlassFillSubtle` | white @38% | white @4% | Nested stat tiles |
| `GlassBorder` | white @85% | white @13% | 1dp glass hairline |
| `Rule` | `#201E1D` @14% | `#FFFFFF` @11% | Dividers, grid lines, unselected chip border |
| `ShadowTint` | `#2D2B2B` @13% | black @50% | Elevation colour |
| `StatusAvailable` | `#16A34A` | same | "Available", "Paid", success tick |
| `StatusOnDuty` | `#F97316` (text `#C2410C`) | same | "On duty" |
| `StatusDue` | `#EC3013` @14% (text `#AE1800`) | same | "Due" invoice pills |

Status pills: 16%-alpha fill of their own hue, full-strength hue as text — never color-only,
every pill also carries a text label.

### Type.kt (full rewrite)
Family: Archivo (400/600/700/800). Scale: Display 46sp/800/−0.03em, H1 31sp/800/−0.02em, H2
27sp/800/−0.02em, H3 23sp/800/−0.02em, H4 16sp/800/0, Numeric XL 44sp/800 tabular/−0.03em,
Numeric L 31–34sp/800/−0.02em, Numeric M 21–26sp/800/−0.02em, Body 13.5sp/400–600/0, Body S
12sp/400/0, Label 10–11sp/800 UPPERCASE/0.06em, Chip 12sp/800/0, Nav 9.5sp/800/0.02em. Minimum
body 13sp; nav labels never below 9.5sp.

### Radii.kt
`chip = 999.dp`, `input = 16.dp`, `button = 18.dp`, `statTile = 20.dp`, `card = 22.dp`,
`cardLarge = 24.dp`, `navBar = 26.dp`, `sheet = 30.dp` (top corners only), `avatarSmall = 13–14.dp`,
`avatarMedium = 16–17.dp`, `avatarHero = 24.dp`, `iconButton = 13.dp`.

### Spacing.kt
4dp base grid. Screen horizontal padding 20dp. Card padding 14–17dp (16 standard). Vertical gap
between cards 11–13dp. Gap inside a card 8–14dp. Sheet padding 20dp sides / 26dp bottom. Nav bar
inset 14dp sides / 12dp bottom.

### Motion.kt
Easing: Emphasised = `CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)`, Standard =
`CubicBezierEasing(0.2f, 0f, 0f, 1f)`. Named duration/spec constants for every moment in the
handoff's motion table (screen enter, list stagger, profile hero shared-element, bottom sheet,
FAB→sheet morph, sheet scrim, live-shift pulse rings, heart-glyph beat, tab indicator, press
feedback, pull-to-refresh spin, sparkline draw, revenue bars, theme switch cross-fade) — see
handoff `README.md` "Motion" table for exact durations, reproduced verbatim into code comments
at each usage site rather than re-typed here.

### Glass.kt (new)
- `val LocalGlassEnabled = compositionLocalOf { true }` — gates blur; low-end/opt-out devices
  fall back to an opaque `Background`-tinted fill at the same radius and border, so layouts stay
  legible with zero blur.
- `Modifier.glassCard(radius: Dp, hazeState: HazeState)`: clips to `RoundedCornerShape(radius)`,
  applies `hazeChild` when `LocalGlassEnabled.current`, else a flat `Background`-tinted
  `background()`; always applies `GlassFill` background + `1.dp` `GlassBorder` border. Blur radius
  20dp light / 26dp dark; nav bar and sheets 26–30dp; saturation boost ~1.4–1.6× where Haze
  supports it.
- `GlassGround()` composable: renders, behind screen content, in order — `Background` fill, a
  56×56dp ruled grid of 1dp `Rule` lines at 70% opacity full-bleed, a 280dp `AccentPrimary` square
  at 22% opacity anchored off-screen top-right (offset −70dp, −70dp), and a 230dp accent square at
  10% opacity off-screen bottom-left (110dp up from the bottom). Every screen that shows the
  ruled/glass ground renders this once behind its content.

## Navigation
Bottom nav rebuilt as a floating glass bar: 66dp tall, inset 14dp sides / 12dp bottom, radius
26dp (`Radii.navBar`), 3dp accent indicator on the top edge sliding via `animateDpAsState` between
five equal 1/5-width slots (340ms standard easing). Icon 22dp over a 9.5sp label; active item
`AccentPrimary` with 2.5dp stroke vs 2dp inactive. Existing `Routes`/5-tab-per-role structure is
unchanged — this is a pure container/visual rebuild, not a navigation refactor. Nav and
`GlassGround` are hidden on full-height routes: splash, role select, login, search, caregiver
profile, care request, chat, live shift, shift report, agency assign (matches the existing
per-route Scaffold pattern, extended with a "full-height route" flag).

## Family-role screens (Phase 1 redesign targets)
Per the handoff's screen-to-repo-file mapping, each of the following gets full visual + motion
treatment — glass cards via `Modifier.glassCard()`, `GlassGround()` background, Archivo
typography, and the specific per-screen interaction notes below:

- **Splash / role select / login** — `ui/screens/onboarding/RoleSelectionScreen.kt`,
  `ui/screens/auth/LoginScreen.kt`. Splash: tap-anywhere-advances, pulsing accent ring + beating
  heart glyph. Login: 4-digit OTP cells, entered cells get accent border.
- **Family home** — `ui/screens/customer/CustomerDashboardScreen.kt`. Pull-to-refresh (icon spins
  360°, accent "Syncing latest vitals…" pill fades in, resolves ~1.5s). Need-chips route to
  search. Caregiver card: Message → chat, Live pulse → pulse tab.
- **Search + caregiver profile** — `ui/screens/customer/CaregiverDetailScreen.kt`. Six toggleable
  filter chips in a horizontal scroller ("Available now" pre-selected), result count line,
  shared-element avatar transition search→profile. Profile: accent-red header block with 28dp
  bottom corners, hero avatar, three stats on a 2dp white-30%-alpha rule, one quiet verification
  line, 7-day availability strip with one day selected.
- **Booking sheet** — `ui/screens/customer/BookingScreen.kt`. 3 steps: date+shift-length → review
  → confirmed. Total is **computed**, not hardcoded: `rate × hours` where 6h shift = 6, 12h = 12,
  Night 12h = 12; format `LKR 22,200` with thousands separators. Confirmation sentence names the
  booked caregiver's first name. Step 3: green tick scales in.
- **Care request** — `ui/screens/customer/CareRequestScreen.kt`. Segmented selectors: caregiver
  gender (Female/Male/No preference), location (Hospital/Home). Submit returns home with a toast.
- **Family pulse** — `ui/screens/customer/PulseDashboardScreen.kt`. Live vitals data (existing
  source, not re-wired). Heart-rate sparkline: path dash-offset draws in 420→0 over 1300ms.
- **Messages + chat** — `ui/screens/messages/MessagesScreen.kt`, `ConversationScreen.kt`.
- **Activity** — `ui/screens/activity/ActivityScreen.kt`.

Out of scope for Phase 1 (tokens apply automatically, no dedicated screen work): Caregiver-role
screens (`CaregiverDashboardScreen.kt`, `VitalsLogScreen.kt` live-shift/vitals-log,
`ShiftSummaryScreen.kt`), Agency-role screens (`AgencyScreens.kt` dashboard/assign/roster/
billing), `SettingsScreen.kt` (dark-mode toggle + language row + demo role switcher — token-only
pass here, dedicated redesign later).

## Toasts
Dark `#201E1D` pill, radius 18dp, 20dp side insets, positioned 92dp from bottom (above the nav
bar), rises in over 340ms, auto-dismisses after 2.6s.

## Accessibility
Touch targets 48dp minimum; nav items 72dp pitch × 66dp; icon buttons 38dp visual with 48dp
target. Body text ≥13sp; contrast ≥4.5:1 body, ≥3:1 headline-scale — full-opacity ink on accent
and glass grounds, never alpha-muted text over blur. Every icon button needs
`contentDescription`. Status never color-only. Reduce-motion: drop infinite pulse rings and
stagger, keep fades.

## Localisation note
Sinhala/Tamil/English strings already exist from the prior Phase 3 localization work (not part
of this handoff) — new copy introduced by this redesign (toast text, section labels, etc.) must
go into the existing `strings.xml`/`values-si`/`values-ta` resource files rather than being
hardcoded, consistent with the existing pattern. Sinhala text needs ~1.5× line-height and no
fixed-height text containers (handoff note).

## Out of scope
- Caregiver-role and Agency-role screen-by-screen redesign (Phase 2/3, future specs).
- Any new data-layer/Firestore wiring — screens keep their existing data sources.
- Charts beyond what's already implemented — sparkline/revenue-bar *animation* is in scope
  (Phase 1 for the Family pulse sparkline only; revenue bars are an Agency-role screen, out of
  scope), but building new chart data pipelines is not.
- Real photo avatars — initials-on-grey placeholder stays as the loading/fallback state; wiring
  real photo URLs is unchanged/future work.

## Verification
1. Every Phase 1 screen renders correctly in light and dark theme, `LocalGlassEnabled = false`
   fallback renders legibly (opaque background-tinted cards, same radius/border, no missing
   content).
2. Bottom nav renders as the 66dp floating glass pill with a sliding 3dp indicator; nav is
   correctly hidden on all ten listed full-height routes.
3. Booking sheet total is computed correctly for all three shift-length options with real rate
   values (spot-check against `rate × hours`).
4. TalkBack announces all interactive elements; touch targets ≥48dp; status pills carry text
   labels.
5. Reduce-motion setting drops pulse rings/stagger but keeps fades.
6. Caregiver-role and Agency-role screens are not visually broken or mixed-style — they inherit
   the new tokens cleanly even without individual redesign.
