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
import com.carepulse.app.data.model.Agency
import com.carepulse.app.data.model.Booking
import com.carepulse.app.data.model.CareRequest
import com.carepulse.app.data.model.CareType
import com.carepulse.app.data.model.Caregiver
import com.carepulse.app.data.model.Gender
import com.carepulse.app.data.model.MedicationItem
import com.carepulse.app.data.model.RequestStatus
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
import kotlinx.coroutines.flow.combine
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
    private val googleHelper: GoogleSignInHelper,
    private val chatRepo: com.carepulse.app.data.repository.FirestoreChatRepository
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

    /** One-shot confirmation message (e.g. "Reset email sent"). */
    private val _authInfo = MutableStateFlow<String?>(null)
    val authInfo: StateFlow<String?> = _authInfo.asStateFlow()

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
                        // Self-heal: if no profile doc exists yet (e.g. created before
                        // Firestore was enabled), fall back to a minimal profile and
                        // persist it, so the session gate never gets stuck loading.
                        _profile.value = repo.getUserProfile(state.uid)
                            ?: UserProfile(
                                state.uid, state.email,
                                state.displayName.orEmpty(), UserRole.CUSTOMER
                            ).also { repo.saveUserProfile(it) }
                    }
                    AuthState.SignedOut -> _profile.value = null
                    AuthState.Loading -> Unit
                }
            }
        }
    }

    fun clearAuthError() { _authError.value = null }
    fun clearAuthInfo() { _authInfo.value = null }

    /** Sends a password-reset email and reports success/failure via state. */
    fun resetPassword(email: String) {
        val target = email.trim()
        if (target.isBlank()) {
            _authError.value = "Enter your email first"
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            auth.sendPasswordReset(target)
                .onSuccess { _authInfo.value = "Password reset email sent to $target" }
                .onFailure { _authError.value = it.message ?: "Could not send reset email" }
            _authLoading.value = false
        }
    }

    fun signIn(email: String, password: String) = launchAuth {
        val user = auth.signInEmail(email, password).getOrThrow()
        _profile.value = repo.getUserProfile(user.uid)
            ?: UserProfile(user.uid, user.email, user.displayName.orEmpty(), UserRole.CUSTOMER)
                .also { repo.saveUserProfile(it) }
    }

    fun signUp(email: String, password: String, name: String, role: UserRole) = launchAuth {
        val user = auth.signUpEmail(email, password, name).getOrThrow()
        val p = UserProfile(
            user.uid, user.email, name.ifBlank { user.displayName.orEmpty() },
            role, roles = listOf(role)
        )
        repo.saveUserProfile(p)
        _profile.value = p
    }

    /**
     * Switches the active role for the current account. If the role isn't enrolled
     * yet it is added (creating an agency tenant when needed), so one email can act
     * as Family, Caregiver, and Agency.
     */
    fun selectRole(role: UserRole, agencyName: String = "") {
        val p = _profile.value ?: return
        if (p.role == role) return
        viewModelScope.launch {
            var agencyId = p.agencyId
            if (role == UserRole.AGENCY && agencyId == null) {
                agencyId = UUID.randomUUID().toString()
                repo.addAgency(Agency(id = agencyId, name = agencyName.ifBlank { p.displayName }))
            }
            val updated = p.copy(
                role = role,
                agencyId = agencyId,
                roles = (p.enrolledRoles + role).distinct()
            )
            _profile.value = updated
            repo.saveUserProfile(updated)
        }
    }

    /** Registers a new caregiving company + its first admin account. */
    fun signUpAgency(email: String, password: String, adminName: String, agencyName: String) =
        launchAuth {
            val user = auth.signUpEmail(email, password, adminName).getOrThrow()
            val agencyId = UUID.randomUUID().toString()
            repo.addAgency(Agency(id = agencyId, name = agencyName))
            val p = UserProfile(
                user.uid, user.email, adminName, UserRole.AGENCY, agencyId,
                roles = listOf(UserRole.AGENCY)
            )
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

    /** Caregivers belonging to the signed-in agency admin's company. */
    val agencyCaregivers: StateFlow<List<Caregiver>> =
        combine(repo.caregivers, _profile) { all, profile ->
            val agencyId = profile?.agencyId ?: return@combine emptyList()
            all.filter { it.agencyId == agencyId }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // --- Care requests (family ↔ agency) -----------------------------------

    /** Public directory of agencies for families to choose from. */
    private val _agencies = MutableStateFlow<List<Agency>>(emptyList())
    val agencies: StateFlow<List<Agency>> = _agencies.asStateFlow()

    fun loadAgencies() {
        viewModelScope.launch { _agencies.value = repo.listPublicAgencies() }
    }

    /** Care requests addressed to the signed-in agency. */
    val agencyRequests: StateFlow<List<CareRequest>> =
        combine(repo.careRequests, _profile) { all, profile ->
            val agencyId = profile?.agencyId ?: return@combine emptyList()
            all.filter { it.agencyId == agencyId }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Care requests this family has submitted. */
    val familyRequests: StateFlow<List<CareRequest>> =
        combine(repo.careRequests, _profile) { all, profile ->
            val uid = profile?.uid ?: return@combine emptyList()
            all.filter { it.familyUid == uid }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Family submits a care request to a chosen agency. */
    fun submitCareRequest(
        agencyId: String,
        patientName: String,
        patientGender: Gender,
        preferredGender: Gender,
        careType: CareType,
        notes: String
    ) {
        val profile = _profile.value
        viewModelScope.launch {
            repo.addCareRequest(
                CareRequest(
                    id = UUID.randomUUID().toString(),
                    agencyId = agencyId,
                    familyUid = profile?.uid,
                    familyName = profile?.displayName ?: "Family member",
                    patientName = patientName.trim(),
                    patientGender = patientGender,
                    preferredGender = preferredGender,
                    careType = careType,
                    notes = notes.trim()
                )
            )
        }
    }

    /** Agency assigns a caregiver to a pending request. */
    fun assignCaregiver(request: CareRequest, caregiver: Caregiver) {
        viewModelScope.launch {
            repo.updateCareRequest(
                request.copy(
                    status = RequestStatus.ASSIGNED,
                    assignedCaregiverId = caregiver.id,
                    assignedCaregiverName = caregiver.name
                )
            )
        }
    }

    /** Caregivers matching a request's required gender (for assignment). */
    fun matchesFor(request: CareRequest): List<Caregiver> =
        agencyCaregivers.value.filter { it.gender == request.preferredGender }

    /** Agency admin adds a caregiver to their own roster. */
    fun addAgencyCaregiver(
        name: String,
        gender: Gender,
        area: String,
        hourlyRate: Int,
        skill: String
    ) {
        val agencyId = _profile.value?.agencyId ?: return
        viewModelScope.launch {
            repo.addCaregiver(
                Caregiver(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    avatarSeed = name.hashCode(),
                    area = area.trim(),
                    qualifications = emptyList(),
                    specializations = if (skill.isBlank()) emptyList() else listOf(skill.trim()),
                    hourlyRate = hourlyRate,
                    rating = 0f,
                    ratingCount = 0,
                    bio = "",
                    availability = emptyList(),
                    gender = gender,
                    agencyId = agencyId
                )
            )
        }
    }

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

    // --- Chat -------------------------------------------------------------------

    fun messagesIn(chatId: String): kotlinx.coroutines.flow.StateFlow<List<com.carepulse.app.data.model.ChatMessage>> =
        chatRepo.messagesIn(chatId)

    fun sendMessage(agencyId: String, agencyName: String, text: String) {
        val profile = _profile.value ?: return
        val familyUid = profile.uid
        val chatId = "${agencyId}_${familyUid}"
        viewModelScope.launch {
            val msg = com.carepulse.app.data.model.ChatMessage(
                id = java.util.UUID.randomUUID().toString(),
                senderUid = familyUid,
                senderName = profile.displayName,
                text = text.trim(),
                timestamp = System.currentTimeMillis()
            )
            val chat = com.carepulse.app.data.model.Chat(
                id = chatId,
                agencyId = agencyId,
                familyUid = familyUid,
                agencyName = agencyName,
                familyName = profile.displayName,
                lastMessage = text.trim(),
                lastMessageAt = System.currentTimeMillis()
            )
            chatRepo.sendMessage(chatId, msg, chat)
        }
    }

    fun loopInCaregiver(chatId: String, caregiverId: String) {
        viewModelScope.launch { chatRepo.loopInCaregiver(chatId, caregiverId) }
    }

    // --- Reviews ---------------------------------------------------------------

    fun submitReview(caregiverId: String, bookingId: String, rating: Float, text: String) {
        val p = _profile.value ?: return
        viewModelScope.launch {
            repo.addReview(
                com.carepulse.app.data.model.Review(
                    id = UUID.randomUUID().toString(),
                    caregiverId = caregiverId,
                    familyUid = p.uid,
                    reviewerName = p.displayName,
                    bookingId = bookingId,
                    rating = rating,
                    text = text.trim()
                )
            )
        }
    }

    suspend fun reviewsFor(caregiverId: String): List<com.carepulse.app.data.model.Review> =
        repo.reviewsFor(caregiverId)

    // --- Caregiver self-edit ---------------------------------------------------

    fun updateCaregiverProfile(
        bio: String,
        specializations: List<String>,
        hourlyRate: Int,
        availability: List<String>
    ) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            val existing = repo.caregivers.value.firstOrNull { it.id == uid } ?: return@launch
            repo.saveCaregiver(
                existing.copy(
                    bio = bio,
                    specializations = specializations,
                    hourlyRate = hourlyRate,
                    availability = availability
                )
            )
        }
    }

    // --- Booking status ---------------------------------------------------------

    fun clockIn(bookingId: String) {
        viewModelScope.launch {
            repo.updateBookingStatus(bookingId, com.carepulse.app.data.model.BookingStatus.IN_PROGRESS)
        }
    }

    fun completeBooking(bookingId: String) {
        viewModelScope.launch {
            repo.updateBookingStatus(bookingId, com.carepulse.app.data.model.BookingStatus.COMPLETED)
        }
    }

    val familyBookings: kotlinx.coroutines.flow.StateFlow<List<com.carepulse.app.data.model.Booking>> =
        combine(repo.bookings, _profile) { all, profile ->
            val uid = profile?.uid ?: return@combine emptyList()
            all.filter { it.customerUid == uid }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val agencyBookings: kotlinx.coroutines.flow.StateFlow<List<com.carepulse.app.data.model.Booking>> =
        combine(repo.bookings, _profile) { all, profile ->
            val agencyId = profile?.agencyId ?: return@combine emptyList()
            all.filter { it.agencyId == agencyId }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CarePulseApplication
                CarePulseViewModel(
                    app.authRepository,
                    app.repository,
                    app.googleSignInHelper,
                    app.chatRepository
                )
            }
        }
    }
}
