package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.DoseFlowRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.MY_PACKAGE_REPLACED" ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            ReminderScheduler.createNotificationChannel(context)
            ReminderScheduler.scheduleDailyMedicationCheck(context)
            ReminderScheduler.scheduleWaterGoalCheck(context)
            ReminderScheduler.scheduleWaterIntervalCheck(context)

            // Reschedule all active medication alarms stored in Room Database entries
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val repo = DoseFlowRepository(db.doseFlowDao(), context)
                    val activeMeds = repo.activeMedications.first()
                    activeMeds.forEach { med ->
                        ReminderScheduler.scheduleMedicationAlarm(
                            context = context,
                            medicationId = med.id,
                            medicationName = med.name,
                            dosage = med.dosage,
                            hour = med.timeHour,
                            minute = med.timeMinute
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BootReceiver", "Error restoring medication alarms on boot", e)
                }
            }
        }
    }
}
