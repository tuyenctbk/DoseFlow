package com.example.tile

import android.content.Context
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.TimelineBuilders
import androidx.wear.tiles.ResourceBuilders
import com.example.data.AppDatabase
import com.example.data.DoseFlowRepository
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class ImmediateFuture<V>(private val value: V) : ListenableFuture<V> {
    override fun addListener(listener: Runnable, executor: Executor) {
        executor.execute(listener)
    }
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false
    override fun isCancelled(): Boolean = false
    override fun isDone(): Boolean = true
    override fun get(): V = value
    override fun get(timeout: Long, unit: TimeUnit): V = value
}

class DoseFlowTileService : TileService() {

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        val context = applicationContext
        val db = AppDatabase.getDatabase(context)
        val dao = db.doseFlowDao()
        val repo = DoseFlowRepository(dao, context)

        val waterGoal = repo.getWaterGoalMl()
        val todayWater = runBlocking {
            try {
                repo.getWaterSumForToday().first() ?: 0
            } catch (e: Exception) {
                0
            }
        }

        val nextMedicationText = runBlocking {
            try {
                val meds = repo.activeMedications.first()
                if (meds.isEmpty()) {
                    "No active meds"
                } else {
                    val calendar = Calendar.getInstance()
                    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = calendar.get(Calendar.MINUTE)

                    val todayNext = meds
                        .filter { it.timeHour > currentHour || (it.timeHour == currentHour && it.timeMinute > currentMinute) }
                        .minByOrNull { it.timeHour * 60 + it.timeMinute }

                    val nextMed = todayNext ?: meds.minByOrNull { it.timeHour * 60 + it.timeMinute }
                    if (nextMed != null) {
                        val timeString = String.format("%02d:%02d", nextMed.timeHour, nextMed.timeMinute)
                        "Next: ${nextMed.name} ($timeString)"
                    } else {
                        "No meds scheduled"
                    }
                }
            } catch (e: Exception) {
                "DoseFlow Active"
            }
        }

        // Use standard tiles LayoutElementBuilders for perfect type-safety and visual clarity
        val rootLayout = LayoutElementBuilders.Column.Builder()
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("DoseFlow Hydration")
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(DimensionBuilders.dp(4f))
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("$todayWater / $waterGoal ml")
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(DimensionBuilders.dp(8f))
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(nextMedicationText)
                    .build()
            )
            .build()

        val layout = LayoutElementBuilders.Layout.Builder()
            .setRoot(rootLayout)
            .build()

        val timelineEntry = TimelineBuilders.TimelineEntry.Builder()
            .setLayout(layout)
            .build()

        val timeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(timelineEntry)
            .build()

        val tile = TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setTimeline(timeline)
            .build()

        return ImmediateFuture(tile)
    }

    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        val resources = ResourceBuilders.Resources.Builder()
            .setVersion("1")
            .build()
        return ImmediateFuture(resources)
    }
}
