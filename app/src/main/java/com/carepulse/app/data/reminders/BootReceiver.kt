package com.carepulse.app.data.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Reschedules stored reminders after device reboot. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        ReminderScheduler.ensureChannel(context)

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = ReminderRepository(appContext)
                repo.loadAllOnce().filter { it.enabled }.forEach { reminder ->
                    ReminderScheduler.schedule(appContext, reminder)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
