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

class WaterGoalCheckWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.doseFlowDao()
        val repository = DoseFlowRepository(dao, applicationContext)

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val goal = repository.getWaterGoalMl()

        val sum = runBlocking {
            dao.getWaterSumForDate(todayStr).first() ?: 0
        }

        if (sum < goal) {
            ReminderScheduler.sendWaterGoalNotification(applicationContext, sum, goal)
        }

        return Result.success()
    }
}
