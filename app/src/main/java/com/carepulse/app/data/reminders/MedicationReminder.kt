package com.carepulse.app.data.reminders

data class MedicationReminder(
    val id: String,
    val name: String,
    val dosage: String,
    val hour: Int,          // 0..23
    val minute: Int,        // 0..59
    val enabled: Boolean = true
)
