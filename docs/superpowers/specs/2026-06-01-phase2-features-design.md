# CarePulse Phase 2 — Feature Design Spec
**Date:** 2026-06-01  
**Scope:** 4 new feature areas built sequentially on top of the existing Firebase/Firestore backend  
**Target:** Working but basic — functional end-to-end, UI can be polished later  

---

## Context

CarePulse Phase 1 delivered the core office-desk workflow: Agency adds caregivers → Family submits a care request → Agency assigns a gender-matched caregiver → Status updates to Assigned. The app uses Firebase Auth + Firestore, Jetpack Compose, MVVM with StateFlow, and supports 3 roles (CUSTOMER / CAREGIVER / AGENCY) on one account.

Phase 2 adds 4 features in dependency order, making the app ready to sell to real agencies:

1. **Push Notifications** — real-time alerts for the care workflow
2. **Real-time Chat** — Family ↔ Agency messaging with caregiver loop-in
3. **Caregiver Ratings & Profile Editing** — reviews after bookings, caregivers manage their own profiles
4. **Booking History & Status Tracking** — families see their bookings, agencies see earnings

---

## Feature 1: Push Notifications (FCM)

### Goal
Alert the right person at each step of the care workflow without them having to check the app.

### Architecture
- Add `firebase-messaging-ktx` to `app/build.gradle.kts`
- Create `CarePulseFirebaseMessagingService.kt` extending `FirebaseMessagingService`
  - Override `onMessageReceived` to show a `NotificationCompat` with title + body
  - Override `onNewToken` to save the token to Firestore
- On sign-in, save `fcmToken` field to `users/{uid}` in Firestore
- Firebase Cloud Functions (Node.js) trigger on Firestore writes:
  - `careRequests/{id}` **created** → look up `agencies/{agencyId}` admin uid → send notification to their token
  - `careRequests/{id}` **updated** (status = ASSIGNED) → look up `familyUid` → notify family
  - `reports/{id}` **created** → look up the caregiver's booking `customerUid` → notify family

### Triggers (all 3)
| Event | Who gets notified | Message |
|-------|------------------|---------|
| Family submits care request | Agency admin | "New care request from [familyName] for [patientName]" |
| Agency assigns caregiver | Family | "Your caregiver [caregiverName] has been assigned" |
| Caregiver files shift report | Family | "[caregiverName] has submitted today's shift report" |

### Android permissions
Add `POST_NOTIFICATIONS` permission (required Android 13+) with runtime request on first launch.

### Files to create/modify
- `app/src/main/java/com/carepulse/app/CarePulseFirebaseMessagingService.kt` (NEW)
- `app/src/main/AndroidManifest.xml` — register service + permission
- `app/build.gradle.kts` — add `firebase-messaging-ktx`
- `functions/index.js` (NEW) — Cloud Functions triggers
- `FirestoreCarePulseRepository.kt` — add `saveFcmToken(uid, token)`
- `CarePulseViewModel.kt` — call `saveFcmToken` after sign-in

---

## Feature 2: Real-time Chat

### Goal
Families and agencies can exchange messages inside the app. Agencies can loop the assigned caregiver into the conversation.

### Data Model (Firestore)
```
chats/{chatId}
  - agencyId: String
  - familyUid: String
  - caregiverId: String? (null until looped in)
  - lastMessage: String
  - lastMessageAt: Timestamp
  - participants: List<String> (uids)

chats/{chatId}/messages/{messageId}
  - senderUid: String
  - senderName: String
  - text: String
  - timestamp: Timestamp
```

`chatId` = `"{agencyId}_{familyUid}"` — deterministic, so both sides open the same thread.

### UI
**MessagesScreen (Family view):**
- List of chat threads (one per agency they've contacted)
- Tap a thread → full-screen conversation with bubbles (sent = right/mint, received = left/cream)
- Text field + Send button at bottom

**MessagesScreen (Agency view):**
- List of all active family threads, sorted by `lastMessageAt`
- Each row shows family name, last message preview, timestamp
- "Loop in caregiver" button inside a thread → sets `caregiverId` on the chat doc; caregiver now appears as a participant

**Caregiver home:**
- Badge on Messages tab if they are looped into any chat
- Same conversation view

### Architecture
- New `ChatRepository` interface + `FirestoreChatRepository` implementation
- `callbackFlow` snapshot listener on `chats/{chatId}/messages` ordered by timestamp
- `ChatViewModel` or extend `CarePulseViewModel` with chat StateFlows
- Messages composable reuses existing `CarePulseTextField` and `PrimaryButton`

### Files to create/modify
- `data/model/Models.kt` — add `Chat`, `ChatMessage` data classes
- `data/repository/ChatRepository.kt` (NEW interface)
- `data/repository/FirestoreChatRepository.kt` (NEW)
- `ui/screens/messages/MessagesScreen.kt` — replace placeholder with real chat list + conversation
- `CarePulseApplication.kt` — inject `chatRepository`
- `CarePulseNavGraph.kt` — add `conversation/{chatId}` route (full-screen, no bottom bar)
- `CarePulseViewModel.kt` — add chat StateFlows and send/load methods

---

## Feature 3: Caregiver Ratings & Profile Editing

### Goal
After a completed booking, families can leave a star rating and review. Caregivers can edit their own profile from their dashboard.

### Data Model (Firestore)
```
reviews/{reviewId}
  - caregiverId: String
  - familyUid: String
  - bookingId: String
  - rating: Float (1.0–5.0)
  - text: String
  - createdAt: Timestamp
  - reviewerName: String
```

After a review is submitted, a Cloud Function (or client-side) recalculates the average and updates `caregivers/{id}.rating` and `.ratingCount`.

### UI
**Rating flow (Family):**
- After a booking moves to COMPLETED, `ActivityScreen` shows a "Rate your caregiver" card
- Tap → `RatingDialog` composable: 5-star tap selector + optional text field + Submit
- Submitted reviews are shown on `CaregiverDetailScreen` below the bio

**Caregiver profile editing:**
- In `CaregiverDashboardScreen`, add "Edit profile" button in top bar
- Opens `EditProfileScreen` (new full-screen route): fields for bio, specializations (tags), hourly rate, availability slots
- Save → `repo.saveCaregiver(updated)` → updates Firestore

### Files to create/modify
- `data/model/Models.kt` — add `Review` data class
- `data/repository/CarePulseRepository.kt` — add `addReview()`, `reviewsFor(caregiverId)`, `saveCaregiver()`
- `data/repository/FirestoreCarePulseRepository.kt` — implement above + recalculate rating
- `ui/screens/customer/CaregiverDetailScreen.kt` — add reviews list section
- `ui/screens/customer/ActivityScreen.kt` — add "Rate caregiver" card for completed bookings
- `ui/screens/caregiver/EditProfileScreen.kt` (NEW)
- `navigation/CarePulseNavGraph.kt` — add `edit-profile` route
- `CarePulseViewModel.kt` — add `submitReview()`, `updateCaregiverProfile()`, `reviewsFor()` StateFlow

---

## Feature 4: Booking History & Status Tracking

### Goal
Families see all their bookings with current status. Agencies see all bookings and total earnings. Caregiver sees their upcoming shifts.

### Status Transitions
```
CONFIRMED → IN_PROGRESS (caregiver clocks in via CaregiverDashboard)
IN_PROGRESS → COMPLETED (caregiver submits ShiftSummary)
```

Clock-in is already in `CaregiverDashboardScreen`; connect it to update the booking status in Firestore.

### UI
**ActivityScreen (Family):**
Replace generic timeline with:
- Tabs: **Upcoming** | **Past**
- Each booking card: caregiver name + avatar, date/time, cost, status chip, "Rate" button (if COMPLETED and not yet reviewed)

**Billing tab (Agency):**
Replace placeholder with:
- Booking list grouped by caregiver
- Per-caregiver: name, number of bookings, total LKR earned
- Grand total at top

**Caregiver Dashboard:**
- Add "Upcoming shifts" section below clock-in button
- Each shift card: patient name, date/time, family contact

### Architecture
- `bookings` Firestore collection already has `customerUid` and `caregiverUid` fields
- Add `agencyId` field to `Booking` model (so agency can query their caregivers' bookings)
- Filter by `customerUid` for family view, by `caregiverUid` for caregiver view, by `agencyId` for agency
- `CaregiverDashboardScreen` clock-in button calls `vm.clockIn(bookingId)` → sets status to `IN_PROGRESS`

### Files to create/modify
- `data/model/Models.kt` — add `agencyId` to `Booking`
- `data/repository/FirestoreCarePulseRepository.kt` — add `updateBookingStatus()`, `bookingsForAgency()`
- `ui/screens/customer/ActivityScreen.kt` — real booking list with tabs and rating trigger
- `ui/screens/agency/AgencyScreens.kt` — real billing screen with earnings breakdown
- `ui/screens/caregiver/CaregiverDashboardScreen.kt` — upcoming shifts section + clock-in updates booking
- `CarePulseViewModel.kt` — add `clockIn()`, `agencyBookings`, `familyBookings` StateFlows

---

## Build Order (Sequential)

| Step | Feature | Why this order |
|------|---------|----------------|
| 1 | Push Notifications | FCM setup needed before chat alerts work |
| 2 | Real-time Chat | Depends on FCM token infrastructure |
| 3 | Ratings & Profile Editing | Self-contained, high family value |
| 4 | Booking History & Status | Ties everything together for agency billing |

---

## Verification

Each feature verified as follows:

**Notifications:**
- Submit a care request as Family → Agency admin device receives push
- Assign caregiver → Family device receives push
- Submit shift report → Family device receives push
- Check Firebase console → Cloud Functions → Logs for execution success

**Chat:**
- Family opens Messages tab → sees agency thread (or "Start a conversation" empty state)
- Send a message → appears in real time on Agency's Messages tab
- Agency taps "Loop in caregiver" → caregiver sees thread in their Messages tab
- Kill and reopen app → messages persist

**Ratings:**
- Complete a booking (via clock-in → shift summary flow)
- ActivityScreen shows "Rate your caregiver" card
- Submit 4-star review → appears on CaregiverDetailScreen
- Caregiver's rating updates in Firestore

**Booking History:**
- Family ActivityScreen shows booking with status chip
- Caregiver clocks in → status changes to IN_PROGRESS in real time on family screen
- Shift summary submitted → status COMPLETED
- Agency Billing tab shows real LKR totals per caregiver

---

## Firestore Security (reminder)
Current rules expire **2026-06-28** (test mode). Before any agency goes live, replace with auth-scoped rules:
- `users/{uid}` — read/write own doc only
- `caregivers/{id}` — read all, write only own (caregiverUid == auth.uid) or agency admin
- `careRequests/{id}` — read/write by agencyId match or familyUid match
- `chats/{chatId}/messages` — read/write by participants array contains auth.uid
- `reviews/{id}` — read all, write only familyUid == auth.uid (one per booking)
