package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.DoseFlowRepository
import com.example.data.WaterDataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class DoseFlowAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_LOG_WATER = "com.example.widget.ACTION_WIDGET_LOG_WATER"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_doseflow)

            // Intent to open MainActivity
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val appPendingIntent = PendingIntent.getActivity(
                context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_open_app, appPendingIntent)

            // Intent to log 250ml water directly from widget
            val waterIntent = Intent(context, DoseFlowAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_LOG_WATER
            }
            val waterPendingIntent = PendingIntent.getBroadcast(
                context, 1, waterIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_water, waterPendingIntent)

            // Query upcoming medication asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val repo = DoseFlowRepository(db.doseFlowDao(), context)
                    val activeMeds = repo.activeMedications.first()

                    val now = Calendar.getInstance()
                    val currentHour = now.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = now.get(Calendar.MINUTE)
                    val currentTotal = currentHour * 60 + currentMinute

                    val upcoming = activeMeds
                        .map { med -> med to (med.timeHour * 60 + med.timeMinute) }
                        .filter { (_, time) -> time >= currentTotal }
                        .minByOrNull { (_, time) -> time }

                    val statusText = if (upcoming != null) {
                        val (med, _) = upcoming
                        val timeStr = String.format("%02d:%02d", med.timeHour, med.timeMinute)
                        "Next: ${med.name} (${med.dosage}) at $timeStr"
                    } else if (activeMeds.isNotEmpty()) {
                        val (med, _) = activeMeds.minBy { it.timeHour * 60 + it.timeMinute }
                            .let { it to (it.timeHour * 60 + it.timeMinute) }
                        val timeStr = String.format("%02d:%02d", med.timeHour, med.timeMinute)
                        "Tomorrow: ${med.name} (${med.dosage}) at $timeStr"
                    } else {
                        "Next: No active medications scheduled"
                    }

                    views.setTextViewText(R.id.widget_upcoming_med, statusText)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    views.setTextViewText(R.id.widget_upcoming_med, "DoseFlow Quick Tracker Active")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_LOG_WATER) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val repo = DoseFlowRepository(db.doseFlowDao(), context)
                    repo.logWater(250)

                    val dsManager = WaterDataStoreManager(context)
                    dsManager.addWater(250)

                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "💧 +250ml Water logged via Widget!", Toast.LENGTH_SHORT).show()
                    }

                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val componentName = ComponentName(context, DoseFlowAppWidgetProvider::class.java)
                    val ids = appWidgetManager.getAppWidgetIds(componentName)
                    for (id in ids) {
                        updateAppWidget(context, appWidgetManager, id)
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to log water via Widget", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
