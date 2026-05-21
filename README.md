# CarePulse

> A warm, empathetic and professional Healthcare Caregiver Booking App — balancing vital clinical reliability with family comfort.

CarePulse connects families with verified caregivers, lets adult children remotely monitor an admitted parent's daily "pulse" (vitals, meals, mood), and gives caregivers a clean handover workflow at the end of every shift.

## ✨ Features

### 1. Role-based Onboarding
- Split flow for **Customers (Family / Children)** and **Caregivers**.
- Caregiver registration captures profile picture, area, qualifications, hourly rate and availability.

### 2. Location-based Caregiver Search & Booking (Customer flow)
- Dashboard with filters: Area / Zip, Rating, Specialization.
- Caregiver Profile Detail screen with credentials, bio and a calendar layout.
- Step-by-step booking: **Select Date/Time → Confirm Details → Mock Payment / Success**.

### 3. Remote Parent Care Dashboard (Children flow)
- The **"Pulse" Dashboard** — daily vitals (Heart Rate, Blood Pressure, Mood, Meals).
- Realistic mock **Video Call UI** for visual check-ins.

### 4. Caregiver Duty Handover & Collaboration
- End-of-shift **"CarePulse Shift Summary Report"**:
  - Medication checklist
  - Behavior notes
  - Free-text daily summary
- Syncs instantly to the Customer view.

## 🎨 Design System

| Token | Value | Use |
|-------|-------|-----|
| Primary | `#A8E6CF` Pastel Teal / Mint | health, calm, "the pulse" |
| Secondary | `#DED2F9` Soft Lavender | accents, notifications |
| Tertiary | `#FFD3B6` Soft Peach | warm secondary actions |
| Background | `#FAFAFA` Off-white / Cream | clean readability |

Rounded, highly readable typography. Large touch targets. Smooth Compose `AnimatedVisibility` transitions and shimmer loading states.

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (100% declarative, Material 3)
- **Architecture:** MVVM with `StateFlow` state holders
- **Data:** Clean repository pattern with in-memory mock generators — runs fully functional out-of-the-box.
- **Navigation:** Jetpack Compose Navigation with type-safe arguments.
- **Min SDK:** 24 · **Target SDK:** 34

## 🚀 Getting Started

1. Open the project in **Android Studio Hedgehog** (or newer).
2. Let Gradle sync (first sync downloads Compose BOM + dependencies).
3. Run on any emulator or device with API 24+.

```bash
./gradlew :app:assembleDebug
```

## 📂 Project Structure

```
app/src/main/java/com/carepulse/app/
├── CarePulseApplication.kt
├── MainActivity.kt
├── data/
│   ├── model/         # Caregiver, Booking, VitalsLog, ShiftReport
│   └── repository/    # MockCarePulseRepository
├── navigation/        # NavGraph + typed routes
├── ui/
│   ├── theme/         # Color, Type, Theme
│   ├── components/    # Reusable pastel components
│   └── screens/
│       ├── onboarding/
│       ├── auth/
│       ├── customer/
│       └── caregiver/
└── viewmodel/         # CarePulseViewModel (shared)
```

## 📸 Screens

- Role Selection → Login → (Customer or Caregiver Registration)
- Customer Dashboard → Caregiver Detail → Booking → Confirmation
- Pulse Dashboard → Video Call
- Caregiver Dashboard → Shift Summary

---

Made with care. 💚
