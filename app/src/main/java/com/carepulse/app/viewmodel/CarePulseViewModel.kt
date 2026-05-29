package com.carepulse.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.carepulse.app.CarePulseApplication
import com.carepulse.app.data.auth.AuthRepository
import com.carepulse.app.data.auth.AuthState
import com.carepulse.app.data.auth.GoogleSignInHelper
import com.carepulse.app.data.model.Booking
import com.carepulse.app.data.model.Caregiver
import com.carepulse.app.data.model.MedicationItem
import com.carepulse.app.data.model.Mood
import com.carepulse.app.data.model.ShiftReport
import com.carepulse.app.data.model.UserProfile
import com.carepulse.app.data.model.UserRole
import com.carepulse.app.data.model.VitalsLog
import com.carepulse.app.data.repository.CarePulseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Shared, app-level ViewModel. Backed by Firebase: [AuthRepository] for sign-in
 * and [CarePulseRepository] (Firestore) for data. Session + role are derived
 * from the signed-in user's `users/{uid}` profile, which is what lets the app
 * skip the login screen on relaunch.
 */
class CarePulseViewModel(
    private val auth: AuthRepository,
    private val repo: CarePulseRepository,
    private val googleHelper: GoogleSignInHelper
) : ViewModel() {

    // --- Auth / session -----------------------------------------------------

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val role: StateFlow<UserRole?> =
        _profile.map { it?.role }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val displayName: StateFlow<String> =
        _profile.map { it?.displayName ?: "" }.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    init {
        viewModelScope.launch {
            auth.authState.collect { state ->
                _authState.value = state
                when (state) {
                    is AuthState.SignedIn -> if (_profile.value?.uid != state.uid) {
                        _profile.value = repo.getUserProfile(state.uid)
                    }
                    AuthState.SignedOut -> _profile.value = null
                    AuthState.Loading -> Unit
                }
            }
        }
    }

    fun clearAuthError() { _authError.value = null }

    fun signIn(email: String, password: String) = launchAuth {
        val user = auth.signInEmail(email, password).getOrThrow()
        _profile.value = repo.getUserProfile(user.uid)
            ?: UserProfile(user.uid, user.email, user.displayName.orEmpty(), UserRole.CUSTOMER)
                .also { repo.saveUserProfile(it) }
    }

    fun signUp(email: String, password: String, name: String, role: UserRole) = launchAuth {
        val user = auth.signUpEmail(email, password, name).getOrThrow()
        val p = UserProfile(user.uid, user.email, name.ifBlank { user.displayName.orEmpty() }, role)
        repo.saveUserProfile(p)
        _profile.value = p
    }

    fun signInWithGoogle(context: Context, role: UserRole) = launchAuth {
        val token = googleHelper.getGoogleIdToken(context).getOrThrow()
        val user = auth.signInWithGoogleIdToken(token).getOrThrow()
        _profile.value = repo.getUserProfile(user.uid)
            ?: UserProfile(user.uid, user.email, user.displayName.orEmpty(), role)
                .also { repo.saveUserProfile(it) }
    }

    fun signOut() {
        auth.signOut()
        _profile.value = null
    }

    private fun launchAuth(block: suspend () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            runCatching { block() }
                .onFailure { _authError.value = it.message ?: "Something went wrong" }
            _authLoading.value = false
        }
    }

    private val currentUid: String? get() = (_authState.value as? AuthState.SignedIn)?.uid

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
        viewModelScope.launch {
            repo.addBooking(
                Booking(
                    id = UUID.randomUUID().toString(),
                    caregiverId = caregiver.id,
                    customerName = displayName.value.ifBlank { "Family member" },
                    patientName = patientName,
                    dateLabel = date,
                    timeSlot = time,
                    totalCost = caregiver.hourlyRate * hours,
                    customerUid = currentUid,
                    caregiverUid = caregiver.id
                )
            )
        }
    }

    // --- Caregiver flow -----------------------------------------------------

    fun registerCaregiver(
        name: String, area: String, qualifications: String,
        specialization: String, hourlyRate: Int
    ) {
        val uid = currentUid ?: "cg-${UUID.randomUUID().toString().take(6)}"
        val cg = Caregiver(
            id = uid,
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
        viewModelScope.launch {
            repo.addCaregiver(cg)
            val p = UserProfile(uid, _profile.value?.email, cg.name, UserRole.CAREGIVER)
            repo.saveUserProfile(p)
            _profile.value = p
        }
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
        viewModelScope.launch {
            repo.submitShiftReport(
                ShiftReport(
                    id = UUID.randomUUID().toString(),
                    caregiverName = displayName.value.ifBlank { "Caregiver" },
                    dateLabel = "Today",
                    medicationsGiven = medications,
                    behaviorNotes = behaviorNotes,
                    daySummary = daySummary,
                    vitals = vitals
                )
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CarePulseApplication
                CarePulseViewModel(app.authRepository, app.repository, app.googleSignInHelper)
            }
        }
    }
}
