package com.carepulse.app.data.repository

import com.carepulse.app.data.model.Agency
import com.carepulse.app.data.model.Booking
import com.carepulse.app.data.model.CareRequest
import com.carepulse.app.data.model.Caregiver
import com.carepulse.app.data.model.ShiftReport
import com.carepulse.app.data.model.UserProfile
import com.carepulse.app.data.model.VitalsLog
import kotlinx.coroutines.flow.StateFlow

/**
 * Data surface consumed by [com.carepulse.app.viewmodel.CarePulseViewModel].
 * Backed at runtime by [FirestoreCarePulseRepository]; the in-memory
 * [MockCarePulseRepository] remains available for previews and tests.
 */
interface CarePulseRepository {
    val caregivers: StateFlow<List<Caregiver>>
    val bookings: StateFlow<List<Booking>>
    val vitals: StateFlow<List<VitalsLog>>
    val reports: StateFlow<List<ShiftReport>>

    fun caregiverById(id: String): Caregiver?

    suspend fun addCaregiver(caregiver: Caregiver)
    suspend fun addBooking(booking: Booking)
    suspend fun submitShiftReport(report: ShiftReport)

    // --- Account profiles (users/{uid}) — drives session routing ----------
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun getUserProfile(uid: String): UserProfile?

    // --- Agencies (tenants) -----------------------------------------------
    suspend fun addAgency(agency: Agency)
    suspend fun getAgency(id: String): Agency?
    /** Public directory of agencies for families to choose from. */
    suspend fun listPublicAgencies(): List<Agency>

    // --- Care requests (family → agency) ----------------------------------
    val careRequests: StateFlow<List<CareRequest>>
    suspend fun addCareRequest(request: CareRequest)
    suspend fun updateCareRequest(request: CareRequest)
    suspend fun saveFcmToken(uid: String, token: String)
}
