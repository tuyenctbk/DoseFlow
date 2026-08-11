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
    const val MEDICATION_CHANNEL_ID = "doseflow_medication_channel"
    const val HYDRATION_CHANNEL_ID = "doseflow_hydration_channel"
    const val CHANNEL_NAME = "DoseFlow Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Medication Channel (High Importance, distinct vibration)
            val medChannel = NotificationChannel(
                MEDICATION_CHANNEL_ID,
                "Medication Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for scheduled medications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }
            notificationManager.createNotificationChannel(medChannel)

            // Hydration Channel (Default Importance, water nudge)
            val hydrationChannel = NotificationChannel(
                HYDRATION_CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Friendly reminders to drink water and stay hydrated"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 200, 200)
            }
            notificationManager.createNotificationChannel(hydrationChannel)
        }
    }

    @android.annotation.SuppressLint("ScheduleExactAlarm")
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

    @android.annotation.SuppressLint("ScheduleExactAlarm")
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

    fun scheduleWaterGoalCheck(context: Context) {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
            .build()

        val periodicWork = androidx.work.PeriodicWorkRequestBuilder<WaterGoalCheckWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).setConstraints(constraints).build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WaterGoalCheckWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }

    fun scheduleWaterIntervalCheck(context: Context) {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
            .build()

        val periodicWork = androidx.work.PeriodicWorkRequestBuilder<WaterIntervalWorker>(
            2, java.util.concurrent.TimeUnit.HOURS
        ).setConstraints(constraints).build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WaterIntervalCheckWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }

    fun sendHydrationNudgeNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 888, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, HYDRATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("💧 Time for a Glass of Water!")
            .setContentText("You haven't logged water in 2 hours. Keep your hydration goal on track!")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(888, notification)
    }

    fun sendWaterGoalNotification(context: Context, currentMl: Int, goalMl: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, HYDRATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("💧 Hydration Goal Reminder")
            .setContentText("You've logged ${currentMl}ml of your ${goalMl}ml daily goal. Take a glass of water before bed!")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(999, notification)
    }
}
