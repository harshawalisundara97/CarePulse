package com.carepulse.app.data.model

import androidx.compose.ui.graphics.Color

/** App user roles. */
enum class UserRole { CUSTOMER, CAREGIVER }

/** Account profile stored in Firestore at `users/{uid}` — drives session routing. */
data class UserProfile(
    val uid: String,
    val email: String?,
    val displayName: String,
    val role: UserRole
)

/** A registered caregiver profile. */
data class Caregiver(
    val id: String,
    val name: String,
    val avatarSeed: Int,                // drives the generated pastel avatar
    val area: String,                   // "Downtown · 90210"
    val qualifications: List<String>,   // ["Registered Nurse", "5 yrs exp"]
    val specializations: List<String>,  // ["Elderly Care", "Post-Op"]
    val hourlyRate: Int,
    val rating: Float,
    val ratingCount: Int,
    val bio: String,
    val availability: List<String>      // ["Mon AM", "Tue PM", ...]
)

/** A booking the customer creates for a caregiver. */
data class Booking(
    val id: String,
    val caregiverId: String,
    val customerName: String,
    val patientName: String,
    val dateLabel: String,              // "Mon, Jun 3"
    val timeSlot: String,               // "9:00 AM - 1:00 PM"
    val totalCost: Int,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val customerUid: String? = null,    // owner, for Firestore querying
    val caregiverUid: String? = null
)

enum class BookingStatus { CONFIRMED, IN_PROGRESS, COMPLETED }

/** A single day's vitals snapshot — the heart of the "Pulse" dashboard. */
data class VitalsLog(
    val dateLabel: String,
    val heartRate: Int,                 // bpm
    val bloodPressureSystolic: Int,
    val bloodPressureDiastolic: Int,
    val mood: Mood,
    val mealsEaten: Int,                // out of 3
    val notes: String
)

enum class Mood(val emoji: String, val label: String, val color: Color) {
    HAPPY("😊", "Happy",     Color(0xFFA8E6CF)),
    CALM ("😌", "Calm",      Color(0xFFDED2F9)),
    TIRED("😴", "Tired",     Color(0xFFFFD3B6)),
    SAD  ("😟", "Sad",       Color(0xFFFFB6B6))
}

/** End-of-shift handover report from a caregiver to the family. */
data class ShiftReport(
    val id: String,
    val caregiverName: String,
    val dateLabel: String,
    val medicationsGiven: List<MedicationItem>,
    val behaviorNotes: String,
    val daySummary: String,
    val vitals: VitalsLog
)

data class MedicationItem(
    val name: String,
    val dose: String,
    val administered: Boolean
)
