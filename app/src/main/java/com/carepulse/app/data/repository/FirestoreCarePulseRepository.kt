package com.carepulse.app.data.repository

import com.carepulse.app.data.model.Agency
import com.carepulse.app.data.model.Booking
import com.carepulse.app.data.model.BookingStatus
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

/**
 * Cloud Firestore implementation of [CarePulseRepository]. Each collection is
 * exposed as a real-time [StateFlow] via a snapshot listener, so the family
 * Pulse dashboard updates live the moment a caregiver submits a report.
 *
 * Manual mappers are used (rather than `toObject`) because [VitalsLog.mood] is
 * a [Mood] enum carrying a UI [androidx.compose.ui.graphics.Color], which
 * Firestore cannot serialize — the enum is persisted by name instead.
 */
class FirestoreCarePulseRepository(
    private val scope: CoroutineScope,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CarePulseRepository {

    private val caregiversCol = db.collection("caregivers")
    private val bookingsCol = db.collection("bookings")
    private val vitalsCol = db.collection("vitals")
    private val reportsCol = db.collection("reports")
    private val usersCol = db.collection("users")
    private val agenciesCol = db.collection("agencies")
    private val careRequestsCol = db.collection("careRequests")

    // ---- Real-time flows ---------------------------------------------------

    private val _caregivers = MutableStateFlow<List<Caregiver>>(emptyList())
    override val caregivers: StateFlow<List<Caregiver>> =
        caregiversCol.snapshots { it.toCaregiver() }
            .onEach { _caregivers.value = it }            // cache for caregiverById
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override val bookings: StateFlow<List<Booking>> =
        bookingsCol.snapshots { it.toBooking() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override val vitals: StateFlow<List<VitalsLog>> =
        vitalsCol.orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots { it.toVitals() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override val reports: StateFlow<List<ShiftReport>> =
        reportsCol.orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots { it.toReport() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override val careRequests: StateFlow<List<CareRequest>> =
        careRequestsCol.orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots { it.toCareRequest() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override fun caregiverById(id: String): Caregiver? =
        _caregivers.value.firstOrNull { it.id == id }

    // ---- Writes ------------------------------------------------------------

    override suspend fun addCaregiver(caregiver: Caregiver) {
        runCatching { caregiversCol.document(caregiver.id).set(caregiver.toMap()).await() }
    }

    override suspend fun addBooking(booking: Booking) {
        runCatching { bookingsCol.document(booking.id).set(booking.toMap()).await() }
    }

    override suspend fun submitShiftReport(report: ShiftReport) {
        runCatching {
            reportsCol.document(report.id).set(report.toMap()).await()
            // Mirror the vitals snapshot into the timeline the family dashboard reads.
            vitalsCol.add(report.vitals.toMap()).await()
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        runCatching { usersCol.document(profile.uid).set(profile.toMap()).await() }
    }

    /**
     * Returns null on any failure (offline, permission denied, missing doc) so
     * a network hiccup can never crash the app. Callers treat null as
     * "profile not loaded yet".
     */
    override suspend fun getUserProfile(uid: String): UserProfile? = runCatching {
        val doc = usersCol.document(uid).get().await()
        if (doc.exists()) doc.toUserProfile() else null
    }.getOrNull()

    override suspend fun addAgency(agency: Agency) {
        runCatching { agenciesCol.document(agency.id).set(agency.toMap()).await() }
    }

    override suspend fun getAgency(id: String): Agency? = runCatching {
        val doc = agenciesCol.document(id).get().await()
        if (doc.exists()) doc.toAgency() else null
    }.getOrNull()

    override suspend fun listPublicAgencies(): List<Agency> = runCatching {
        agenciesCol.whereEqualTo("isPublic", true).get().await()
            .documents.mapNotNull { runCatching { it.toAgency() }.getOrNull() }
    }.getOrDefault(emptyList())

    override suspend fun addCareRequest(request: CareRequest) {
        runCatching { careRequestsCol.document(request.id).set(request.toMap()).await() }
    }

    override suspend fun updateCareRequest(request: CareRequest) {
        runCatching { careRequestsCol.document(request.id).set(request.toMap()).await() }
    }

    override suspend fun saveFcmToken(uid: String, token: String) {
        runCatching {
            usersCol.document(uid).update("fcmToken", token).await()
        }
    }

    /** Writes the default caregiver roster if the collection is empty. */
    suspend fun seedCaregiversIfEmpty() {
        val snap = caregiversCol.limit(1).get().await()
        if (snap.isEmpty) {
            MockCarePulseRepository.seedCaregivers().forEach { cg ->
                caregiversCol.document(cg.id).set(cg.toMap()).await()
            }
        }
    }

    // ---- Snapshot helper ---------------------------------------------------

    private fun <T> Query.snapshots(map: (DocumentSnapshot) -> T?) = callbackFlow {
        val reg = addSnapshotListener { snap, _ ->
            if (snap != null) {
                trySend(snap.documents.mapNotNull { runCatching { map(it) }.getOrNull() })
            }
        }
        awaitClose { reg.remove() }
    }
}

// ---- Mappers ---------------------------------------------------------------

private fun Caregiver.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "name" to name, "avatarSeed" to avatarSeed, "area" to area,
    "qualifications" to qualifications, "specializations" to specializations,
    "hourlyRate" to hourlyRate, "rating" to rating.toDouble(),
    "ratingCount" to ratingCount, "bio" to bio, "availability" to availability,
    "gender" to gender.name, "agencyId" to agencyId
)

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toCaregiver(): Caregiver = Caregiver(
    id = getString("id") ?: id,
    name = getString("name").orEmpty(),
    avatarSeed = (getLong("avatarSeed") ?: 1L).toInt(),
    area = getString("area").orEmpty(),
    qualifications = get("qualifications") as? List<String> ?: emptyList(),
    specializations = get("specializations") as? List<String> ?: emptyList(),
    hourlyRate = (getLong("hourlyRate") ?: 0L).toInt(),
    rating = (getDouble("rating") ?: 0.0).toFloat(),
    ratingCount = (getLong("ratingCount") ?: 0L).toInt(),
    bio = getString("bio").orEmpty(),
    availability = get("availability") as? List<String> ?: emptyList(),
    gender = runCatching { Gender.valueOf(getString("gender").orEmpty()) }
        .getOrDefault(Gender.FEMALE),
    agencyId = getString("agencyId")
)

private fun Booking.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "caregiverId" to caregiverId, "customerName" to customerName,
    "patientName" to patientName, "dateLabel" to dateLabel, "timeSlot" to timeSlot,
    "totalCost" to totalCost, "status" to status.name,
    "customerUid" to customerUid, "caregiverUid" to caregiverUid,
    "createdAt" to System.currentTimeMillis()
)

private fun DocumentSnapshot.toBooking(): Booking = Booking(
    id = getString("id") ?: id,
    caregiverId = getString("caregiverId").orEmpty(),
    customerName = getString("customerName").orEmpty(),
    patientName = getString("patientName").orEmpty(),
    dateLabel = getString("dateLabel").orEmpty(),
    timeSlot = getString("timeSlot").orEmpty(),
    totalCost = (getLong("totalCost") ?: 0L).toInt(),
    status = runCatching { BookingStatus.valueOf(getString("status").orEmpty()) }
        .getOrDefault(BookingStatus.CONFIRMED),
    customerUid = getString("customerUid"),
    caregiverUid = getString("caregiverUid")
)

private fun VitalsLog.toMap(): Map<String, Any?> = mapOf(
    "dateLabel" to dateLabel, "heartRate" to heartRate,
    "bloodPressureSystolic" to bloodPressureSystolic,
    "bloodPressureDiastolic" to bloodPressureDiastolic,
    "mood" to mood.name, "mealsEaten" to mealsEaten, "notes" to notes,
    "createdAt" to System.currentTimeMillis()
)

private fun DocumentSnapshot.toVitals(): VitalsLog =
    vitalsFrom({ key -> getLong(key) }, { key -> getString(key) })

/** Shared reader so nested vitals (inside a report) and top-level vitals agree. */
private fun vitalsFrom(
    long: (String) -> Long?,
    string: (String) -> String?
): VitalsLog = VitalsLog(
    dateLabel = string("dateLabel").orEmpty(),
    heartRate = (long("heartRate") ?: 0L).toInt(),
    bloodPressureSystolic = (long("bloodPressureSystolic") ?: 0L).toInt(),
    bloodPressureDiastolic = (long("bloodPressureDiastolic") ?: 0L).toInt(),
    mood = runCatching { Mood.valueOf(string("mood").orEmpty()) }.getOrDefault(Mood.CALM),
    mealsEaten = (long("mealsEaten") ?: 0L).toInt(),
    notes = string("notes").orEmpty()
)

private fun MedicationItem.toMap(): Map<String, Any?> =
    mapOf("name" to name, "dose" to dose, "administered" to administered)

private fun ShiftReport.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "caregiverName" to caregiverName, "dateLabel" to dateLabel,
    "medicationsGiven" to medicationsGiven.map { it.toMap() },
    "behaviorNotes" to behaviorNotes, "daySummary" to daySummary,
    "vitals" to vitals.toMap(), "createdAt" to System.currentTimeMillis()
)

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toReport(): ShiftReport {
    val meds = (get("medicationsGiven") as? List<Map<String, Any?>>).orEmpty().map {
        MedicationItem(
            name = it["name"] as? String ?: "",
            dose = it["dose"] as? String ?: "",
            administered = it["administered"] as? Boolean ?: false
        )
    }
    val v = (get("vitals") as? Map<String, Any?>).orEmpty()
    val vitals = vitalsFrom(
        long = { key -> (v[key] as? Number)?.toLong() },
        string = { key -> v[key] as? String }
    )
    return ShiftReport(
        id = getString("id") ?: id,
        caregiverName = getString("caregiverName").orEmpty(),
        dateLabel = getString("dateLabel").orEmpty(),
        medicationsGiven = meds,
        behaviorNotes = getString("behaviorNotes").orEmpty(),
        daySummary = getString("daySummary").orEmpty(),
        vitals = vitals
    )
}

private fun UserProfile.toMap(): Map<String, Any?> = mapOf(
    "uid" to uid, "email" to email, "displayName" to displayName,
    "role" to role.name, "agencyId" to agencyId,
    "roles" to enrolledRoles.map { it.name }
)

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toUserProfile(): UserProfile {
    val active = runCatching { UserRole.valueOf(getString("role").orEmpty()) }
        .getOrDefault(UserRole.CUSTOMER)
    val roleNames = get("roles") as? List<String> ?: emptyList()
    val roles = roleNames.mapNotNull { runCatching { UserRole.valueOf(it) }.getOrNull() }
    return UserProfile(
        uid = getString("uid") ?: id,
        email = getString("email"),
        displayName = getString("displayName").orEmpty(),
        role = active,
        agencyId = getString("agencyId"),
        roles = roles
    )
}

private fun CareRequest.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "agencyId" to agencyId, "familyUid" to familyUid,
    "familyName" to familyName, "patientName" to patientName,
    "patientGender" to patientGender.name, "preferredGender" to preferredGender.name,
    "careType" to careType.name, "notes" to notes, "status" to status.name,
    "assignedCaregiverId" to assignedCaregiverId,
    "assignedCaregiverName" to assignedCaregiverName,
    "createdAt" to System.currentTimeMillis()
)

private fun DocumentSnapshot.toCareRequest(): CareRequest = CareRequest(
    id = getString("id") ?: id,
    agencyId = getString("agencyId").orEmpty(),
    familyUid = getString("familyUid"),
    familyName = getString("familyName").orEmpty(),
    patientName = getString("patientName").orEmpty(),
    patientGender = runCatching { Gender.valueOf(getString("patientGender").orEmpty()) }
        .getOrDefault(Gender.FEMALE),
    preferredGender = runCatching { Gender.valueOf(getString("preferredGender").orEmpty()) }
        .getOrDefault(Gender.FEMALE),
    careType = runCatching { CareType.valueOf(getString("careType").orEmpty()) }
        .getOrDefault(CareType.HOME),
    notes = getString("notes").orEmpty(),
    status = runCatching { RequestStatus.valueOf(getString("status").orEmpty()) }
        .getOrDefault(RequestStatus.PENDING),
    assignedCaregiverId = getString("assignedCaregiverId"),
    assignedCaregiverName = getString("assignedCaregiverName")
)

private fun Agency.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "name" to name, "city" to city, "address" to address,
    "nearHospital" to nearHospital, "phone" to phone, "isPublic" to isPublic,
    "createdAt" to System.currentTimeMillis()
)

private fun DocumentSnapshot.toAgency(): Agency = Agency(
    id = getString("id") ?: id,
    name = getString("name").orEmpty(),
    city = getString("city").orEmpty(),
    address = getString("address").orEmpty(),
    nearHospital = getString("nearHospital").orEmpty(),
    phone = getString("phone").orEmpty(),
    isPublic = getBoolean("isPublic") ?: true
)
