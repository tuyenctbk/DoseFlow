package com.example.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.DoseFlowRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyMedicationReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.doseFlowDao()
            val repository = DoseFlowRepository(dao, applicationContext)

            val activeMeds = repository.activeMedications.first()
            if (activeMeds.isNotEmpty()) {
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                // Re-schedule alarm reminders for all active medications to guarantee OS alarms persist
                activeMeds.forEach { med ->
                    ReminderScheduler.scheduleMedicationAlarm(
                        context = applicationContext,
                        medicationId = med.id,
                        medicationName = med.name,
                        dosage = med.dosage,
                        hour = med.timeHour,
                        minute = med.timeMinute
                    )
                }

                // If there is an upcoming dose within the next hour, send a friendly WorkManager reminder
                val upcomingInHour = activeMeds.find { med ->
                    val medMinutes = med.timeHour * 60 + med.timeMinute
                    val currentTotalMinutes = currentHour * 60 + currentMinute
                    val diff = medMinutes - currentTotalMinutes
                    diff in 0..60
                }

                if (upcomingInHour != null) {
                    ReminderScheduler.sendMedicationReminderNotification(
                        context = applicationContext,
                        medicationId = upcomingInHour.id,
                        medicationName = upcomingInHour.name,
                        dosage = upcomingInHour.dosage
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
