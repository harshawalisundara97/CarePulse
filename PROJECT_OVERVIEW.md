# CarePulse — Project Overview & Build Plan

> A mobile platform that connects **families** with **professional caregivers**, and
> lets families monitor their loved one's wellbeing in real time.
> Android · Kotlin · Jetpack Compose · Firebase.

---

## 1. Vision

CarePulse is a two-sided care platform:

- **Families** find and book trusted caregivers, then stay connected — viewing daily
  vitals, mood, meals, shift reports, and chatting with the caregiver.
- **Caregivers** manage shifts, log patient vitals, file end-of-shift reports, and
  communicate with families.

The product feeling: **calm, trustworthy, professional, human** — soft pastel palette,
Material vector iconography (no emoji), smooth animations, zero clutter.

---

## 2. Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation-Compose (single-activity) |
| State | ViewModel + Kotlin `StateFlow` |
| Auth | Firebase Authentication (email/password, Google) |
| Database | Cloud Firestore (real-time) |
| Files | Firebase Storage (profile photos, attachments) |
| Notifications | Firebase Cloud Messaging (FCM) |
| Min SDK | 24 · Target SDK | 34 |

---

## 3. Architecture

Single-activity, Compose-first, unidirectional data flow:

```
UI (Composable screens)
   ↕  observe StateFlow / emit events
ViewModel (CarePulseViewModel)
   ↕  suspend calls
Repositories
   ├── AuthRepository           → Firebase Auth
   └── CarePulseRepository      → Cloud Firestore  (interface)
        └── FirestoreCarePulseRepository (impl)
Models (data classes)
```

**Principles**
- Screens are stateless; they render `StateFlow` and call ViewModel functions.
- Repositories are the only place that touch Firebase.
- All network failures are caught — a hiccup must never crash the app.
- One shared `CarePulseViewModel` holds session + cross-screen state.

---

## 4. Data Model (Firestore collections)

| Collection | Key fields |
|------------|-----------|
| `users` | uid, email, displayName, role (CUSTOMER/CAREGIVER), photoUrl |
| `caregivers` | id, name, area, qualifications, specializations[], hourlyRate, rating, reviewCount, avatarSeed, bio, photoUrl |
| `bookings` | id, caregiverId, customerUid, caregiverUid, patientName, dateLabel, timeSlot, hours, totalCost, status |
| `vitals` | dateLabel, heartRate, bloodPressure, mood, mealsEaten, notes, patientId |
| `reports` | id, caregiverName, dateLabel, daySummary, behaviorNotes, medicationsGiven[], vitals |
| `conversations` *(planned)* | id, participants[], lastMessage, updatedAt |
| `messages` *(planned)* | conversationId, senderUid, text, sentAt |

---

## 5. Screens & Navigation

### 5.1 Onboarding / Auth flow (no bottom bar)
| Screen | Purpose |
|--------|---------|
| **Splash** | Session gate → routes to RoleSelection or Home |
| **RoleSelection** | Choose Family or Caregiver |
| **Login / Sign-up** | Email/password + Google, **Forgot password** |
| **CaregiverRegistration** | New caregivers complete their profile |

### 5.2 Main app — 5-tab bottom navigation (role-aware)
| Tab | Family sees | Caregiver sees |
|-----|-------------|----------------|
| **Home** | Find caregivers (search, filters) | Shift dashboard (clock in/out) |
| **Pulse** | Loved one's vitals & charts | Patient vitals they log |
| **Messages** | Chat with caregivers | Chat with families |
| **Activity** | My bookings | My shift reports |
| **Settings** | Profile, account, sign out | Profile, account, sign out |

### 5.3 Full-screen routes (bottom bar hidden)
`CaregiverDetail` · `Booking` · `VideoCall` · `ShiftSummary` · `ChatDetail` *(planned)*

### 5.4 Navigation map
```
Splash ─┬─ RoleSelection ─ Login ─┬─ Home (Customer)
        │                          └─ CaregiverRegistration ─ Home (Caregiver)
        │
   [Bottom nav]  Home · Pulse · Messages · Activity · Settings
        │
   Home(Customer) ─ CaregiverDetail ─ Booking ─ (back to Activity)
   Home(Caregiver) ─ ShiftSummary ─ (back to Home)
   Pulse ─ VideoCall
   Messages ─ ChatDetail (planned)
```

---

## 6. Animations (target)

| Where | Animation |
|-------|-----------|
| Splash → app | System splash fade (done) |
| Tab switches | Cross-fade / slide between tab content |
| Dashboard → Detail | Shared-element / slide + fade transition |
| Lists | Item enter (fade + slide up), staggered |
| Buttons | Press scale + ripple |
| Loading | Shimmer skeletons (component exists) |
| Charts | Animated draw-in (heart-rate chart done) |
| Mood / status chips | Selection color + scale |

Use `AnimatedContent`, `animate*AsState`, `Crossfade`, and
`androidx.navigation` transitions.

---

## 7. Feature Roadmap (phased)

### ✅ Phase 0 — Foundation (DONE)
- Compose UI, theme, all core screens
- Firebase Auth (email/password + Google) + persistent session
- Firestore repository with live data + seeding
- Crash-safe error handling
- 5-tab bottom navigation
- App icon + splash screen
- Material icon moods (no emoji), name capitalization
- Forgot-password reset email

### 🔜 Phase 1 — Connect & Profiles
- [ ] **Messages**: real chat (conversations + messages collections, real-time)
- [ ] **Profile photos**: camera / gallery → Firebase Storage
- [ ] **Caregiver reviews & ratings**: families rate after a booking
- [ ] **Profile editing** for both roles

### 🔜 Phase 2 — Engagement
- [ ] **Push notifications (FCM)**: new report, new message, booking status
- [ ] **Medication reminders**: local notifications/alarms
- [ ] **Booking lifecycle**: request → accept → in-progress → complete states
- [ ] **Real video call** (Agora / Jitsi / WebRTC) replacing placeholder

### 🔜 Phase 3 — Trust & Scale
- [ ] **Firestore security rules** (per-user access — replace test mode!)
- [ ] **Caregiver verification** (documents, background check status)
- [ ] **Payments** (Stripe / Razorpay) for bookings
- [ ] **Location**: distance to caregiver, map of nearby caregivers
- [ ] **Biometric unlock**

### 🔜 Phase 4 — Polish & Launch
- [ ] Full animation pass
- [ ] Branded transactional emails (Trigger Email extension)
- [ ] Custom email domain
- [ ] Accessibility audit (TalkBack, contrast, font scaling)
- [ ] Crash/analytics (Crashlytics + Analytics)
- [ ] Play Store listing, signed release build

---

## 8. Native Android Features to integrate

| Feature | API | Use case |
|---------|-----|----------|
| Camera / photo picker | CameraX / PhotoPicker | Profile photos |
| Push notifications | FCM | Reports, messages, bookings |
| Local notifications | AlarmManager / WorkManager | Medication reminders |
| Location | FusedLocationProvider | Distance, nearby caregivers |
| Biometric | BiometricPrompt | Quick secure login |
| Phone / dialer | Intent | Call caregiver |
| Share | Intent | Share a report |

---

## 9. Security & Privacy (must-do before launch)

- [ ] **Replace Firestore test-mode rules** (currently open until **2026-06-28**) with
      per-user rules: a user reads/writes only their own data; caregivers are
      publicly readable; bookings/reports restricted to their participants.
- [ ] Validate all inputs.
- [ ] Don't log PII.
- [ ] Health data = sensitive → encrypt at rest where possible, clear privacy policy.

---

## 10. Quality

- **Testing**: ViewModel unit tests, repository tests with a fake, Compose UI tests
  for critical flows (login, booking, report).
- **CI**: build + test on every PR.
- **Definition of done**: builds clean, runs on device, no crash, feature verified
  end-to-end on a real device.

---

## 11. Current Status Snapshot

- Runs end-to-end on device with live Firebase backend. ✅
- Auth, Firestore reads/writes, session persistence, navigation, icon/splash,
  password reset — all working.
- Next recommended step: **Phase 1 → real Messages chat** (the heart of the
  "connect" idea), or **profile photos** (camera + Storage).

---

*Living document — update as features land. Each checkbox is a build task.*
