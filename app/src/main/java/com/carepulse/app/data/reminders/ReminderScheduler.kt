package com.carepulse.app.data.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL_ID = "medication_reminders"
    const val EXTRA_ID = "reminder_id"
    const val EXTRA_NAME = "reminder_name"
    const val EXTRA_DOSAGE = "reminder_dosage"
    const val EXTRA_HOUR = "hour"
    const val EXTRA_MINUTE = "minute"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Medication reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Reminders to take your medication on time" }
                nm.createNotificationChannel(channel)
            }
        }
    }

    fun schedule(context: Context, reminder: MedicationReminder) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pi = pendingIntent(context, reminder)
        val triggerAt = nextTriggerMillis(reminder.hour, reminder.minute)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context, reminder: MedicationReminder) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        am.cancel(pendingIntent(context, reminder))
    }

    private fun pendingIntent(context: Context, r: MedicationReminder): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_ID, r.id)
            putExtra(EXTRA_NAME, r.name)
            putExtra(EXTRA_DOSAGE, r.dosage)
            putExtra(EXTRA_HOUR, r.hour)
            putExtra(EXTRA_MINUTE, r.minute)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, r.id.hashCode(), intent, flags)
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
