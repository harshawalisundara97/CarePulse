package com.carepulse.app.viewmodel

import androidx.lifecycle.ViewModel
import com.carepulse.app.data.model.Booking
import com.carepulse.app.data.model.Caregiver
import com.carepulse.app.data.model.MedicationItem
import com.carepulse.app.data.model.Mood
import com.carepulse.app.data.model.ShiftReport
import com.carepulse.app.data.model.UserRole
import com.carepulse.app.data.model.VitalsLog
import com.carepulse.app.data.repository.MockCarePulseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Shared, app-level ViewModel. In a real product you'd split this per feature,
 * but for a mock-data app this keeps wiring simple while still using StateFlow
 * and unidirectional data flow throughout.
 */
class CarePulseViewModel : ViewModel() {

    private val repo = MockCarePulseRepository

    // --- Session ------------------------------------------------------------

    private val _role = MutableStateFlow<UserRole?>(null)
    val role: StateFlow<UserRole?> = _role.asStateFlow()

    private val _displayName = MutableStateFlow("Alex Chen")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    fun setRole(role: UserRole) { _role.value = role }
    fun setDisplayName(name: String) { _displayName.value = name }

    // --- Customer flow ------------------------------------------------------

    val caregivers = repo.caregivers
    val bookings = repo.bookings
    val vitals = repo.vitals
    val reports = repo.reports

    private val _areaFilter = MutableStateFlow("")
    val areaFilter: StateFlow<String> = _areaFilter.asStateFlow()

    private val _minRating = MutableStateFlow(0f)
    val minRating: StateFlow<Float> = _minRating.asStateFlow()

    private val _specializationFilter = MutableStateFlow<String?>(null)
    val specializationFilter: StateFlow<String?> = _specializationFilter.asStateFlow()

    fun setAreaFilter(value: String) { _areaFilter.value = value }
    fun setMinRating(value: Float) { _minRating.value = value }
    fun setSpecializationFilter(value: String?) { _specializationFilter.value = value }

    fun caregiverById(id: String): Caregiver? = repo.caregiverById(id)

    fun confirmBooking(caregiver: Caregiver, patientName: String, date: String, time: String, hours: Int) {
        repo.addBooking(
            Booking(
                id = UUID.randomUUID().toString(),
                caregiverId = caregiver.id,
                customerName = _displayName.value,
                patientName = patientName,
                dateLabel = date,
                timeSlot = time,
                totalCost = caregiver.hourlyRate * hours
            )
        )
    }

    // --- Caregiver flow -----------------------------------------------------

    fun registerCaregiver(
        name: String, area: String, qualifications: String,
        specialization: String, hourlyRate: Int
    ) {
        val cg = Caregiver(
            id = "cg-${UUID.randomUUID().toString().take(6)}",
            name = name.ifBlank { "New Caregiver" },
            avatarSeed = (1..99).random(),
            area = area.ifBlank { "Unspecified" },
            qualifications = qualifications.split(",").map { it.trim() }.filter { it.isNotBlank() },
            specializations = listOfNotNull(specialization.ifBlank { null }),
            hourlyRate = hourlyRate.coerceAtLeast(10),
            rating = 5.0f, ratingCount = 0,
            bio = "Newly joined CarePulse caregiver — ready to help.",
            availability = listOf("Mon AM", "Tue PM", "Wed AM")
        )
        repo.addCaregiver(cg)
        _displayName.value = cg.name
    }

    fun submitShiftReport(
        medications: List<MedicationItem>,
        behaviorNotes: String,
        daySummary: String,
        heartRate: Int,
        systolic: Int,
        diastolic: Int,
        mood: Mood,
        mealsEaten: Int
    ) {
        val vitals = VitalsLog(
            dateLabel = "Today",
            heartRate = heartRate,
            bloodPressureSystolic = systolic,
            bloodPressureDiastolic = diastolic,
            mood = mood,
            mealsEaten = mealsEaten,
            notes = daySummary.take(80)
        )
        repo.submitShiftReport(
            ShiftReport(
                id = UUID.randomUUID().toString(),
                caregiverName = _displayName.value,
                dateLabel = "Today",
                medicationsGiven = medications,
                behaviorNotes = behaviorNotes,
                daySummary = daySummary,
                vitals = vitals
            )
        )
    }
}
