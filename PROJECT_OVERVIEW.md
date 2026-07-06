# CarePulse — Project Overview & Build Plan

> **A B2B SaaS platform for caregiving companies in Sri Lanka.**
> It digitizes how small caregiving agencies (near hospitals) match caregivers to
> patients, lets caregivers manage their work, and keeps patient families connected
> with daily updates.
> Android · Kotlin · Jetpack Compose · Firebase · Multi-tenant.

---

## 1. The Problem (today, offline)

```
1. A caregiving COMPANY runs a small office near a hospital.
2. Caregivers register with the company and wait at the office.
3. A patient's FAMILY visits the office and talks to OFFICE STAFF.
4. Staff matches a caregiver — gender-matched (female patient → female caregiver)
   and skill-matched — and books them.
5. The caregiver goes with the family to the hospital/home and cares for the patient.
6. There is NO digital connection: no live updates, no records, all paper & in-person.
```

**CarePulse digitizes this entire workflow** and adds live family updates, records,
scheduling, and billing — then sells it to caregiving companies across Sri Lanka.

---

## 2. Who we sell to & how we make money

- **Customer = the caregiving company (agency).** Each company is a **tenant**.
- **Monetization:** subscription per agency (monthly/annual), optionally tiered by
  number of caregivers, plus possible per-booking fee.
- **Why they buy it** (the four pillars):
  1. **Daily updates to family** — the emotional hook families love → agencies win clients.
  2. **Caregiver roster & scheduling** — saves office staff hours of manual work.
  3. **Gender & skill matching** — fast, correct caregiver assignment.
  4. **Billing & payments** — track bookings, payments, and company revenue.

---

## 2.1 Go-to-Market Strategy

**Core play: land-and-expand from one hospital cluster.**

1. **Pick one hospital** (e.g. a large Colombo hospital) with several agencies nearby.
2. **Give 1–2 agencies the product free for 2–3 months** as pilot partners; sit with
   them and make it work on their real bookings.
3. **Turn them into a reference customer** — agency owners trust other owners far more
   than salespeople. Word-of-mouth near a hospital sells the rest.
4. **Expand hospital by hospital**, each cluster a beachhead.

**Wedge vs. retention:**
- **Attract with daily family updates** (photos/vitals/mood) — families love it, so
  agencies that offer it win more clients. This is the marketing hook.
- **Retain on office time saved** — instant gender/skill matching, roster at a glance,
  billing tracked, no paper. This is why the owner pays every month.
- *Demo the family updates to win attention; close on time-saved.*

**Pricing:**
- **Free tier** (1–2 caregivers) so any agency starts at zero risk.
- **Low monthly per-agency**, tiered by caregiver count (5 / 15 / 30+).
- **Family app stays 100% free** — families are the viral growth engine, not revenue.
- Bill in **LKR**, support local payment methods.

**Sri Lanka-specific essentials:**
- **Sinhala + Tamil + English** in-app (non-negotiable for adoption).
- **WhatsApp-based onboarding & support.**
- **In-person tablet demos** at the office.
- **Works on cheap Android + flaky data** — keep the app offline-tolerant.
- **Sub-minute onboarding** — adding/assigning a caregiver must be trivial; office
  staff are not technical.

**One-line strategy:** *Build a pilot-ready version, give it free to 2 agencies near
one hospital, make them love it, and let owner word-of-mouth sell the rest.*

---

## 3. The Three Roles

| Role | Who | What they do |
|------|-----|--------------|
| 🏢 **Agency Admin** (office staff) | The company | Manage caregiver roster & availability, receive care requests, **assign** a gender/skill-matched caregiver, track on-duty status, manage billing/payments, view revenue |
| 🧑‍⚕️ **Caregiver** | Staff registered to one agency | Set availability, see assignments, travel to patient, **log daily vitals/mood/meals/photos**, file shift reports, chat with family |
| 👨‍👩‍👧 **Family** | Patient's relatives | **Sign up & pick a company**, submit a care request (gender preference, needs, hospital/home, dates), get matched, receive **live daily updates**, chat/video, pay |

---

## 4. Multi-Tenancy (how companies stay separate)

Every record carries an **`agencyId`**. A company's caregivers, requests, patients,
and bookings are visible only within that company.

```
agencies/{agencyId}
  ├── caregivers (agencyId == this)
  ├── careRequests (agencyId == this)
  ├── assignments / bookings (agencyId == this)
  └── families/patients linked via requests
```

Families browse a **public directory of agencies** to choose who to request from;
once they request, their data lives under that agency.

---

## 5. Real-world → Digital workflow mapping

| Real step | In CarePulse |
|-----------|--------------|
| Caregiver waits at office | Caregiver sets status **Available** in app; appears on agency roster |
| Family visits office | Family opens app, picks the company, taps **Request a caregiver** |
| Family explains needs (gender, hospital/home, dates) | Structured **Care Request** form (patient gender, preferred caregiver gender, care type, dates, notes) |
| Staff matches a caregiver | Agency admin sees the request + a filtered list of **available, gender/skill-matched** caregivers → taps **Assign** |
| Booking made | **Assignment/Booking** created with status `ASSIGNED` |
| Caregiver goes to patient | Caregiver sees assignment, taps **Start shift** (check-in) |
| Caregiver provides care | Logs **vitals, mood, meals, photos, medications**; family sees them live |
| Care ends | Caregiver files **Shift Report**; booking → `COMPLETED` |
| Payment | Family pays in-app / agency records payment; revenue tracked |

---

## 5.1 Filling a job: two models (assign **or** open-job alerts)

Caregivers don't have to sit at the office — they work from their phone like
PickMe/Uber drivers. A job can be filled two ways:

**A) Direct assign (office style):** Agency admin picks an available, gender/skill-
matched caregiver and assigns them. Caregiver gets a notification to accept.

**B) Open-job broadcast (gig style):**
```
1. Agency posts an OPEN JOB (patient need, gender required, location, time, pay).
2. Nearby available caregivers get a PUSH ALERT (FCM + location radius).
3. A caregiver taps ACCEPT.
4. Agency CONFIRMS that caregiver (first-come or agency picks among applicants).
5. Caregiver travels to the patient and STARTS the job (check-in).
```

Both coexist: small agencies use direct assign; busy ones broadcast to fill fast.
Caregivers see a **home feed of nearby open jobs** and their alerts.

---

## 6. Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation-Compose (single-activity) |
| State | ViewModel + Kotlin `StateFlow` |
| Auth | Firebase Authentication (email/password, Google) |
| Database | Cloud Firestore (real-time, multi-tenant) |
| Files | Firebase Storage (profile & update photos) |
| Notifications | Firebase Cloud Messaging (FCM) |
| Payments | Stripe / Razorpay / local SL gateway |
| Min SDK 24 · Target SDK 34 | |

---

## 7. Architecture

```
UI (Composable screens, per role)
   ↕  observe StateFlow / emit events
ViewModel (CarePulseViewModel + role-specific state)
   ↕  suspend calls
Repositories
   ├── AuthRepository            → Firebase Auth
   ├── AgencyRepository          → agencies, directory
   ├── CaregiverRepository       → caregivers, availability
   ├── RequestRepository         → careRequests, assignments
   └── CareLogRepository         → vitals, reports, messages
Models (data classes, all carry agencyId where relevant)
```

**Principles:** stateless screens; repositories own Firebase; every query scoped by
`agencyId`; all network failures caught; never crash on a hiccup.

---

## 8. Data Model (Firestore)

| Collection | Key fields |
|------------|-----------|
| `agencies` | id, name, city, address, nearHospital, phone, logoUrl, **isPublic** |
| `users` | uid, role (ADMIN/CAREGIVER/FAMILY), agencyId, displayName, email, photoUrl, gender |
| `caregivers` | id, agencyId, name, **gender**, skills[], qualifications, experienceYrs, hourlyRate, rating, **status** (AVAILABLE/ON_DUTY/OFF), photoUrl |
| `careRequests` | id, agencyId, familyUid, patientName, **patientGender**, **preferredCaregiverGender**, careType (HOSPITAL/HOME), needs, startDate, endDate, status (PENDING/ASSIGNED/DECLINED) |
| `jobPostings` | id, agencyId, requestId, requiredGender, skills[], careType, location(lat/lng), startTime, pay, status (OPEN/FILLED/CANCELLED), applicants[] |
| `assignments` | id, agencyId, requestId, jobPostingId?, caregiverId, familyUid, status (ASSIGNED/ACCEPTED/IN_PROGRESS/COMPLETED), startedAt, endedAt, totalCost |
| `vitals` | assignmentId, agencyId, dateLabel, heartRate, bloodPressure, mood, mealsEaten, notes, photoUrls[] |
| `reports` | id, assignmentId, agencyId, caregiverName, dateLabel, daySummary, behaviorNotes, medicationsGiven[] |
| `conversations` / `messages` | per assignment, participants, text, sentAt |
| `payments` | id, agencyId, assignmentId, amount, status (DUE/PAID), method, paidAt |

---

## 9. Screens & Navigation (per role)

### 9.1 Shared / Auth (no bottom bar)
Splash · RoleSelection *(Family vs Caregiver vs Agency-login)* · Login/Sign-up
(Forgot password) · Onboarding.

### 9.2 🏢 Agency Admin — 5 tabs
| Tab | Purpose |
|-----|---------|
| **Dashboard** | Today's requests, on-duty caregivers, alerts, revenue summary |
| **Requests** | Incoming care requests → open one → see matched caregivers → **Assign** |
| **Caregivers** | Roster: add/edit caregivers, availability, status |
| **Billing** | Bookings, payments due/paid, revenue reports |
| **Settings** | Company profile, staff, sign out |

### 9.3 🧑‍⚕️ Caregiver — 5 tabs
| Tab | Purpose |
|-----|---------|
| **Home** | **Nearby open jobs feed** + alerts (accept), current/next assignment, **Start/End shift**, availability toggle |
| **Pulse** | Log patient vitals/mood/meals/photos |
| **Messages** | Chat with assigned family |
| **Activity** | Past assignments & shift reports |
| **Settings** | Profile, sign out |

### 9.4 👨‍👩‍👧 Family — 5 tabs
| Tab | Purpose |
|-----|---------|
| **Home** | Pick/search agency, **Request a caregiver**, current caregiver card |
| **Pulse** | Loved one's live vitals, mood, meals, photos, charts |
| **Messages** | Chat with caregiver |
| **Activity** | Requests & booking history, payments |
| **Settings** | Profile, sign out |

### 9.5 Full-screen routes
AgencyDirectory · CareRequestForm · AssignCaregiver · CaregiverDetail ·
AssignmentDetail · ShiftSummary · VideoCall · ChatDetail · Payment.

---

## 10. Animations (target)
Splash fade · tab cross-fade · list item enter (staggered) · dashboard→detail
shared-element · button press scale · shimmer skeletons · animated charts ·
status/mood chip transitions · request→assigned state animation.

---

## 11. Feature Roadmap (phased)

### ✅ Phase 0 — Foundation (DONE)
Compose UI, theme, core screens, Firebase Auth (+Google), Firestore repo with live
data, crash-safe handling, 5-tab bottom nav, app icon + splash, Material-icon moods,
name capitalization, forgot-password reset.
> *Note: current build has 2 roles (Family/Caregiver) and a single-tenant data model.*

### 🔜 Phase 1 — The 3-role, multi-tenant core (NEXT — the real pivot)
- [ ] Add **Agency** role + `agencies` collection + **public directory**
- [ ] Add `agencyId` to all records; scope every query by it
- [ ] **Family self-sign-up → pick a company**
- [ ] **Care Request** flow (gender, care type, dates, needs)
- [ ] **Agency assigns** gender/skill-matched caregiver → Assignment
- [ ] Caregiver **availability/status** + roster on agency dashboard

### 🔜 Phase 2 — Daily updates & connection (the selling hook)
- [ ] Caregiver logs vitals/mood/meals + **photo updates** (Firebase Storage)
- [ ] Family **live Pulse** view of their patient
- [ ] **Real chat** (conversations + messages, real-time)
- [ ] **Push notifications** (new request, assignment, update, message)

### 🔜 Phase 2.5 — Caregiver marketplace (Uber/PickMe style)
- [ ] Agency posts **open jobs** (`jobPostings`)
- [ ] Caregiver **nearby-jobs feed** (location radius) on Home
- [ ] **Push alerts** to nearby available caregivers (FCM + location)
- [ ] Caregiver **Accept** → agency **Confirm** → caregiver **check-in/start**
- [ ] Availability toggle drives who receives alerts

### 🔜 Phase 3 — Scheduling & Billing
- [ ] Assignments **calendar** for agency
- [ ] Shift check-in/out, hours tracking
- [ ] **Billing**: cost per booking, payments due/paid, **revenue reports**
- [ ] In-app **payments** (Stripe/Razorpay/local)

### 🔜 Phase 4 — Trust, polish & launch
- [ ] **Multi-tenant Firestore security rules** (replace test mode — open until 2026-06-28!)
- [ ] Caregiver verification (documents, background check)
- [ ] Real **video call** (Agora/Jitsi)
- [ ] Location / nearby agencies, biometric unlock
- [ ] Full animation pass, branded emails, accessibility
- [ ] Crashlytics + Analytics, signed release, Play Store
- [ ] **Agency onboarding/sales flow** (sign up a new company)

---

## 12. Native Android Features
Camera/photo picker (updates & profiles) · FCM push · local notifications (med
reminders) · location (nearby agencies, distance) · biometric login · dialer intent
(call) · share intent (reports).

---

## 12.1 Platforms — Android now, iOS later

Today the app is **native Android** (Kotlin + Jetpack Compose). Firebase already works
for iOS, so the **backend is reusable**. Options for iOS:

| Approach | Reuse | Notes |
|----------|-------|-------|
| **Compose Multiplatform / KMP** (recommended) | Highest — reuse Kotlin models, repos, ViewModels, and most Compose UI on iOS | Natural fit since we're already Kotlin + Compose. Needs a Mac + Xcode. iOS support is newer but production-ready. |
| **Flutter** (rewrite in Dart) | Backend only — UI rewritten | One codebase for both, big ecosystem, but throws away the Compose work. |
| **Native SwiftUI** (separate app) | Backend only | Best iOS feel, but double the code and maintenance. |

**Recommendation:** Finish the **Android pilot first** (validate with real agencies),
then add iOS via **Compose Multiplatform** — migrating the existing Kotlin/Compose code
rather than rewriting. Requires a Mac with Xcode + an Apple Developer account ($99/yr).
Don't split effort across two platforms before the product is proven.

---

## 13. Security & Privacy (must-do before launch)
- [ ] **Multi-tenant rules**: a user only accesses data where `agencyId` matches their
      own; families see only their assignments; caregivers see only assigned patients;
      admins see only their agency.
- [ ] Replace **test-mode rules** (open until **2026-06-28**).
- [ ] Health data is sensitive → encrypt where possible, clear privacy policy, consent.
- [ ] Validate inputs; never log PII.

---

## 14. Quality
ViewModel unit tests · repository tests with fakes · Compose UI tests for login,
request, assign, log-update flows · CI build+test on PRs · definition of done =
builds clean, runs on device, no crash, verified end-to-end.

---

## 15. Current Status & Next Step

- Foundation runs end-to-end on device with live Firebase. ✅
- **Biggest gap:** the app is still **2-role, single-tenant**. The real product needs
  the **3-role, multi-tenant core (Phase 1)**.
- **Recommended next build:** introduce the **Agency role + agencyId scoping + public
  agency directory**, then the **Care Request → Assign** flow. That turns the demo into
  your actual sellable product.

---

*Living document — each checkbox is a build task. Update as features land.*
