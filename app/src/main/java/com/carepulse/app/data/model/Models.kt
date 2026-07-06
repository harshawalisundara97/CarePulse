package com.carepulse.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** App user roles. */
enum class UserRole { CUSTOMER, CAREGIVER, AGENCY }

/**
 * Account profile stored in Firestore at `users/{uid}` — drives session routing.
 * One email/account can hold multiple [roles]; [role] is the currently active one.
 */
data class UserProfile(
    val uid: String,
    val email: String?,
    val displayName: String,
    val role: UserRole,                 // currently active role
    val agencyId: String? = null,       // tenant the user belongs to (admins/caregivers)
    val roles: List<UserRole> = emptyList()  // all roles this account is enrolled in
) {
    /** Enrolled roles, always including the active [role] (handles legacy docs). */
    val enrolledRoles: List<UserRole>
        get() = (roles + role).distinct()
}

/** A caregiving company (tenant) stored at `agencies/{id}`. */
data class Agency(
    val id: String,
    val name: String,
    val city: String = "",
    val address: String = "",
    val nearHospital: String = "",
    val phone: String = "",
    val isPublic: Boolean = true        // listed in the family-facing directory
)

/** Caregiver gender — used for gender-matching a patient to a caregiver. */
enum class Gender { FEMALE, MALE }

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
    val availability: List<String>,     // ["Mon AM", "Tue PM", ...]
    val gender: Gender = Gender.FEMALE,
    val agencyId: String? = null        // owning agency (tenant)
)

/** Where care is delivered. */
enum class CareType { HOSPITAL, HOME }

/** Lifecycle of a family's care request. */
enum class RequestStatus { PENDING, ASSIGNED, DECLINED }

/**
 * A family's request to a caregiving agency for a caregiver.
 * The agency reviews it and assigns a gender/skill-matched caregiver.
 */
data class CareRequest(
    val id: String,
    val agencyId: String,
    val familyUid: String?,
    val familyName: String,
    val patientName: String,
    val patientGender: Gender,
    val preferredGender: Gender,        // required caregiver gender
    val careType: CareType,
    val notes: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val assignedCaregiverId: String? = null,
    val assignedCaregiverName: String? = null
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
    val caregiverUid: String? = null,
    val agencyId: String? = null        // for agency billing queries
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

enum class Mood(val icon: ImageVector, val label: String, val color: Color) {
    HAPPY(Icons.Filled.SentimentVerySatisfied, "Happy", Color(0xFFA8E6CF)),
    CALM (Icons.Filled.SentimentSatisfied,     "Calm",  Color(0xFFDED2F9)),
    TIRED(Icons.Filled.SentimentNeutral,       "Tired", Color(0xFFFFD3B6)),
    SAD  (Icons.Filled.SentimentDissatisfied,  "Sad",   Color(0xFFFFB6B6))
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

/**
 * A chat thread between a family and an agency.
 * chatId = "{agencyId}_{familyUid}" — deterministic so both sides find the same doc.
 */
data class Chat(
    val id: String,
    val agencyId: String,
    val familyUid: String,
    val agencyName: String = "",
    val familyName: String = "",
    val caregiverId: String? = null,     // set when agency loops in a caregiver
    val lastMessage: String = "",
    val lastMessageAt: Long = 0L         // epoch millis
)

data class ChatMessage(
    val id: String,
    val senderUid: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

/** A family's star rating for a caregiver after a completed booking. */
data class Review(
    val id: String,
    val caregiverId: String,
    val familyUid: String,
    val reviewerName: String,
    val bookingId: String,
    val rating: Float,          // 1.0–5.0
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
