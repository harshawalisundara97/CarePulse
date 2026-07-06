# CarePulse — Profile Photo, Health Analytics & Animations

## 1. Profile Photo

Every user (family, caregiver, agency admin) can set a profile photo by tapping their avatar in Settings. A `ModalBottomSheet` offers two options: **Take Photo** (opens device camera) and **Choose from Gallery** (opens image picker). The selected image is copied into the app's internal storage (`filesDir/profile_photo.jpg`). The local file path is persisted in `SharedPreferences` so it survives restarts. Coil renders the real photo; if none exists, `GeneratedAvatar` (initials) is the fallback.

No Firebase Storage is required — photos stay on-device only (Spark free plan compatible).

**Permissions needed:**
- `CAMERA` — for Take Photo
- `READ_MEDIA_IMAGES` (Android 13+) / `READ_EXTERNAL_STORAGE` (≤12) — for Gallery

## 2. Caregiver Health Data Logging + Family Analytics

**Caregiver side:** A new **"Log Vitals"** button on `CaregiverDashboardScreen` opens `VitalsLogScreen`, a standalone screen where the caregiver can record vitals at any time (not only at shift clock-out). Fields: heart rate, blood pressure (systolic/diastolic), mood (emoji chips), meals eaten, notes. On submit it writes directly to Firestore `vitals/` collection. The existing clock-out ShiftSummaryScreen continues to also write vitals as before.

**Family side (Pulse Dashboard):** The existing `PulseDashboardScreen` is enhanced with:
- **Weekly BP trend chart** — mirrors the heart rate chart, shows systolic as a blue line, diastolic as a lighter line
- **Mood distribution strip** — horizontal bar showing breakdown of moods over last 7 entries (Happy / Calm / Tired / Sad counts)
- **Analytics summary row** — three chips: avg heart rate, avg BP, most frequent mood

All data is live via the existing Firestore snapshot listener on `vitals`.

## 3. Animations

Targeted, purposeful animations — not gratuitous. Each adds feedback or delight at a natural moment.

| Location | Animation |
|----------|-----------|
| All screen entries | Fade-in + slide-up via `NavHost` `enterTransition` |
| Dashboard cards | Staggered `AnimatedVisibility` slide-in (50ms delay per card) |
| Vital number display | Count-up from 0 to actual value on first appearance |
| Heart rate vital card | Subtle continuous pulse scale on the red icon (already partially done in VideoCallScreen — apply same pattern) |
| `PrimaryButton` press | Scale-down to 0.96f on press, spring back on release (via `interactionSource`) |
| Bottom nav tab switch | `indicatorColor` already set; add `animateContentSize` on label |
| Booking confirmed toast | Slide-in snackbar with a checkmark icon |

Compose `animation` library is already in the BOM — no new dependencies needed for animations.
