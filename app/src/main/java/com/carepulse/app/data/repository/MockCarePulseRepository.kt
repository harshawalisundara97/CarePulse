package com.carepulse.app.data.repository

import com.carepulse.app.data.model.Booking
import com.carepulse.app.data.model.Caregiver
import com.carepulse.app.data.model.MedicationItem
import com.carepulse.app.data.model.Mood
import com.carepulse.app.data.model.ShiftReport
import com.carepulse.app.data.model.VitalsLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Single-instance in-memory repository. Generates believable mock data on
 * first access so the entire app runs without a backend. The Customer Pulse
 * dashboard and Shift Report screen share the same flow — when the caregiver
 * submits a report it appears instantly in the family view.
 */
object MockCarePulseRepository {

    // ---- Caregivers --------------------------------------------------------

    private val _caregivers = MutableStateFlow(seedCaregivers())
    val caregivers: StateFlow<List<Caregiver>> = _caregivers.asStateFlow()

    fun caregiverById(id: String): Caregiver? = _caregivers.value.firstOrNull { it.id == id }

    fun addCaregiver(c: Caregiver) {
        _caregivers.value = _caregivers.value + c
    }

    // ---- Bookings ----------------------------------------------------------

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    fun addBooking(b: Booking) { _bookings.value = _bookings.value + b }

    // ---- Vitals timeline & Shift reports -----------------------------------

    private val _vitals = MutableStateFlow(seedVitals())
    val vitals: StateFlow<List<VitalsLog>> = _vitals.asStateFlow()

    private val _reports = MutableStateFlow(seedReports())
    val reports: StateFlow<List<ShiftReport>> = _reports.asStateFlow()

    /** Caregiver clocks out: report is published to the family in real time. */
    fun submitShiftReport(report: ShiftReport) {
        _reports.value = listOf(report) + _reports.value
        _vitals.value = listOf(report.vitals) + _vitals.value
    }

    suspend fun logVitals(vitals: VitalsLog) {
        // No-op for mock — real vitals go through FirestoreCarePulseRepository
    }

    // ---- Seed data ---------------------------------------------------------

    /** Public so the Firestore repository can seed an empty database. */
    fun seedCaregivers(): List<Caregiver> = listOf(
        Caregiver(
            id = "cg-1", name = "Amara Patel", avatarSeed = 1,
            area = "Downtown · 90210",
            qualifications = listOf("Registered Nurse", "5 yrs experience", "CPR Certified"),
            specializations = listOf("Elderly Care", "Post-Op Recovery"),
            hourlyRate = 28, rating = 4.9f, ratingCount = 127,
            bio = "Compassionate RN with five years specializing in geriatric care. " +
                "I treat every parent like my own — patient, gentle, attentive.",
            availability = listOf("Mon AM", "Mon PM", "Tue AM", "Wed PM", "Fri AM")
        ),
        Caregiver(
            id = "cg-2", name = "Marcus Lee", avatarSeed = 2,
            area = "Brookside · 90402",
            qualifications = listOf("Licensed Practical Nurse", "8 yrs experience"),
            specializations = listOf("Dementia Care", "Mobility Support"),
            hourlyRate = 24, rating = 4.7f, ratingCount = 86,
            bio = "Calm and methodical LPN. Specialized in dementia and Alzheimer's support — " +
                "I focus on routine, dignity and meaningful daily connection.",
            availability = listOf("Tue PM", "Wed AM", "Thu AM", "Thu PM")
        ),
        Caregiver(
            id = "cg-3", name = "Sofia Rivera", avatarSeed = 3,
            area = "Lakeview · 90301",
            qualifications = listOf("Certified Nursing Assistant", "3 yrs experience"),
            specializations = listOf("Companion Care", "Meal Prep"),
            hourlyRate = 19, rating = 4.8f, ratingCount = 64,
            bio = "Warm, bilingual CNA. I bring great conversation, home-cooked meals " +
                "and a steady hand for medication reminders.",
            availability = listOf("Mon PM", "Wed PM", "Sat AM", "Sun AM")
        ),
        Caregiver(
            id = "cg-4", name = "Daniel Okafor", avatarSeed = 4,
            area = "Hillside · 90405",
            qualifications = listOf("Registered Nurse", "10 yrs experience", "Wound-care specialist"),
            specializations = listOf("Post-Op Recovery", "Wound Care"),
            hourlyRate = 32, rating = 5.0f, ratingCount = 211,
            bio = "Senior RN with a decade of hospital experience. I bring clinical " +
                "rigor home — perfect for complex post-operative recovery.",
            availability = listOf("Mon AM", "Tue AM", "Thu PM", "Fri PM", "Sat PM")
        )
    )

    private fun seedVitals(): List<VitalsLog> = listOf(
        VitalsLog("Today",      72, 122, 78, Mood.HAPPY, 3, "Ate breakfast well, walked in the garden."),
        VitalsLog("Yesterday",  76, 128, 82, Mood.CALM,  3, "Quiet day. Watched a film with the caregiver."),
        VitalsLog("Mon",        80, 130, 84, Mood.TIRED, 2, "Skipped lunch — appetite was low."),
        VitalsLog("Sun",        74, 120, 76, Mood.HAPPY, 3, "Family video call lifted his mood.")
    )

    private fun seedReports(): List<ShiftReport> = listOf(
        ShiftReport(
            id = UUID.randomUUID().toString(),
            caregiverName = "Amara Patel",
            dateLabel = "Today · 9:00 AM – 1:00 PM",
            medicationsGiven = listOf(
                MedicationItem("Lisinopril", "10 mg",  true),
                MedicationItem("Metformin",  "500 mg", true),
                MedicationItem("Vitamin D",  "1000 IU", true)
            ),
            behaviorNotes = "Cheerful in the morning. Brief moment of confusion around 11 but " +
                "quickly settled with a short walk.",
            daySummary = "A genuinely good day. Mr. Lee ate all three meals, took every " +
                "medication on schedule and spent 25 minutes in the garden. Vitals are stable.",
            vitals = VitalsLog("Today", 72, 122, 78, Mood.HAPPY, 3, "Ate breakfast well, walked in the garden.")
        )
    )
}
