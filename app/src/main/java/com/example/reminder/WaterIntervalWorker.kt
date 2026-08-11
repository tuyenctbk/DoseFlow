package com.example.reminder

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.DoseFlowRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WaterIntervalWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.doseFlowDao()
        val repository = DoseFlowRepository(dao, applicationContext)

        val lastWaterTime = runBlocking { repository.getLastWaterLogTime() }
        val currentTime = System.currentTimeMillis()
        val twoHoursMillis = 2 * 60 * 60 * 1000L

        if (currentTime - lastWaterTime > twoHoursMillis) {
            ReminderScheduler.sendHydrationNudgeNotification(applicationContext)
        }

        return Result.success()
    }
}
