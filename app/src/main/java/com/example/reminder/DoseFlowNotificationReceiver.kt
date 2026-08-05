package com.example.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.DoseFlowRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DoseFlowNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MEDICATION_REMINDER = "com.example.doseflow.ACTION_MEDICATION_REMINDER"
        const val ACTION_TAKE_NOW = "com.example.doseflow.ACTION_TAKE_NOW"
        const val ACTION_SNOOZE_15 = "com.example.doseflow.ACTION_SNOOZE_15"
        const val ACTION_SKIP = "com.example.doseflow.ACTION_SKIP"

        const val EXTRA_MED_ID = "extra_med_id"
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_DOSAGE = "extra_dosage"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(EXTRA_MED_ID, -1L)
        val medicationName = intent.getStringExtra(EXTRA_MED_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: "1 Dose"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent.action) {
            ACTION_MEDICATION_REMINDER -> {
                showNotification(context, notificationManager, medicationId, medicationName, dosage)
            }

            ACTION_TAKE_NOW -> {
                notificationManager.cancel(medicationId.toInt())
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(context)
                    val repo = DoseFlowRepository(db.doseFlowDao(), context)
                    repo.logMedicationAction(medicationId, medicationName, dosage, "TAKEN")
                }
            }

            ACTION_SNOOZE_15 -> {
                notificationManager.cancel(medicationId.toInt())
                ReminderScheduler.snoozeMedicationAlarm(
                    context = context,
                    medicationId = medicationId,
                    medicationName = medicationName,
                    dosage = dosage,
                    snoozeMinutes = 15
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(context)
                    val repo = DoseFlowRepository(db.doseFlowDao(), context)
                    repo.logMedicationAction(medicationId, medicationName, dosage, "SNOOZED")
                }
            }

            ACTION_SKIP -> {
                notificationManager.cancel(medicationId.toInt())
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(context)
                    val repo = DoseFlowRepository(db.doseFlowDao(), context)
                    repo.logMedicationAction(medicationId, medicationName, dosage, "SKIPPED")
                }
            }
        }
    }

    private fun showNotification(
        context: Context,
        notificationManager: NotificationManager,
        medicationId: Long,
        medicationName: String,
        dosage: String
    ) {
        ReminderScheduler.createNotificationChannel(context)

        // Content Intent (Open App)
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            medicationId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Take
        val takeIntent = Intent(context, DoseFlowNotificationReceiver::class.java).apply {
            action = ACTION_TAKE_NOW
            putExtra(EXTRA_MED_ID, medicationId)
            putExtra(EXTRA_MED_NAME, medicationName)
            putExtra(EXTRA_DOSAGE, dosage)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 10 + 1).toInt(),
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Snooze
        val snoozeIntent = Intent(context, DoseFlowNotificationReceiver::class.java).apply {
            action = ACTION_SNOOZE_15
            putExtra(EXTRA_MED_ID, medicationId)
            putExtra(EXTRA_MED_NAME, medicationName)
            putExtra(EXTRA_DOSAGE, dosage)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Skip
        val skipIntent = Intent(context, DoseFlowNotificationReceiver::class.java).apply {
            action = ACTION_SKIP
            putExtra(EXTRA_MED_ID, medicationId)
            putExtra(EXTRA_MED_NAME, medicationName)
            putExtra(EXTRA_DOSAGE, dosage)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 10 + 3).toInt(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("💊 Medication Reminder: $medicationName")
            .setContentText("Time to take $dosage. 1-tap confirmation from wrist or phone.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "✅ Take Now", takePendingIntent)
            .addAction(0, "⏰ Snooze 15m", snoozePendingIntent)
            .addAction(0, "🚫 Skip", skipPendingIntent)

        notificationManager.notify(medicationId.toInt(), builder.build())
    }
}
