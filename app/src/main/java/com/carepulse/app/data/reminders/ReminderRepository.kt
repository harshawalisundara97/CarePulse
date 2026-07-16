package com.carepulse.app.data.reminders

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.remindersDataStore by preferencesDataStore(name = "carepulse_reminders")
private val REMINDERS_KEY = stringSetPreferencesKey("reminders")

/** Encodes a reminder as "id|name|dosage|hour|minute|enabled" — simple, no JSON dep needed. */
private fun MedicationReminder.encode() = listOf(id, name, dosage, hour, minute, enabled).joinToString("|")

private fun decode(raw: String): MedicationReminder? {
    val parts = raw.split("|")
    if (parts.size != 6) return null
    return runCatching {
        MedicationReminder(
            id = parts[0],
            name = parts[1],
            dosage = parts[2],
            hour = parts[3].toInt(),
            minute = parts[4].toInt(),
            enabled = parts[5].toBoolean()
        )
    }.getOrNull()
}

class ReminderRepository(private val context: Context) {
    val reminders: Flow<List<MedicationReminder>> = context.remindersDataStore.data.map { prefs ->
        (prefs[REMINDERS_KEY] ?: emptySet()).mapNotNull(::decode).sortedBy { it.hour * 60 + it.minute }
    }

    suspend fun add(name: String, dosage: String, hour: Int, minute: Int): MedicationReminder {
        val reminder = MedicationReminder(id = UUID.randomUUID().toString(), name = name, dosage = dosage, hour = hour, minute = minute)
        context.remindersDataStore.edit { prefs ->
            val current = prefs[REMINDERS_KEY] ?: emptySet()
            prefs[REMINDERS_KEY] = current + reminder.encode()
        }
        ReminderScheduler.ensureChannel(context)
        ReminderScheduler.schedule(context, reminder)
        return reminder
    }

    suspend fun remove(reminder: MedicationReminder) {
        context.remindersDataStore.edit { prefs ->
            val current = prefs[REMINDERS_KEY] ?: emptySet()
            prefs[REMINDERS_KEY] = current.filterNot { decode(it)?.id == reminder.id }.toSet()
        }
        ReminderScheduler.cancel(context, reminder)
    }

    suspend fun loadAllOnce(): List<MedicationReminder> {
        val prefs = context.remindersDataStore.data.first()
        return (prefs[REMINDERS_KEY] ?: emptySet()).mapNotNull(::decode)
    }
}
