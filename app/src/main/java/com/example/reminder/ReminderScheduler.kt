package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.DoseFlowRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object ReminderScheduler {

    const val CHANNEL_ID = "doseflow_reminders"
    const val CHANNEL_NAME = "DoseFlow Medication & Water Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled medications and water reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleMedicationAlarm(
        context: Context,
        medicationId: Long,
        medicationName: String,
        dosage: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, DoseFlowNotificationReceiver::class.java).apply {
            action = DoseFlowNotificationReceiver.ACTION_MEDICATION_REMINDER
            putExtra(DoseFlowNotificationReceiver.EXTRA_MED_ID, medicationId)
            putExtra(DoseFlowNotificationReceiver.EXTRA_MED_NAME, medicationName)
            putExtra(DoseFlowNotificationReceiver.EXTRA_DOSAGE, dosage)
            putExtra("extra_hour", hour)
            putExtra("extra_minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "Exact alarm permission missing, fallback to set", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun snoozeMedicationAlarm(
        context: Context,
        medicationId: Long,
        medicationName: String,
        dosage: String,
        snoozeMinutes: Int = 15
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, DoseFlowNotificationReceiver::class.java).apply {
            action = DoseFlowNotificationReceiver.ACTION_MEDICATION_REMINDER
            putExtra(DoseFlowNotificationReceiver.EXTRA_MED_ID, medicationId)
            putExtra(DoseFlowNotificationReceiver.EXTRA_MED_NAME, medicationName)
            putExtra(DoseFlowNotificationReceiver.EXTRA_DOSAGE, dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId + 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelMedicationAlarm(context: Context, medicationId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DoseFlowNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
