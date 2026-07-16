package com.carepulse.app.data.reminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.carepulse.app.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(ReminderScheduler.EXTRA_ID) ?: return
        val name = intent.getStringExtra(ReminderScheduler.EXTRA_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(ReminderScheduler.EXTRA_DOSAGE).orEmpty()

        ReminderScheduler.ensureChannel(context)
        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.reminders_notification_title, name))
            .setContentText(
                if (dosage.isBlank()) context.getString(R.string.reminders_notification_body_generic)
                else context.getString(R.string.reminders_notification_body_with_dose, dosage)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id.hashCode(), notification)

        // Reschedule for next day
        val hour = intent.getIntExtra(ReminderScheduler.EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(ReminderScheduler.EXTRA_MINUTE, -1)
        if (hour in 0..23 && minute in 0..59) {
            ReminderScheduler.schedule(
                context,
                MedicationReminder(id = id, name = name, dosage = dosage, hour = hour, minute = minute)
            )
        }
    }
}
