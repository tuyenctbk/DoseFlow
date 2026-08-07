package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicationLogEntity
import com.example.data.WaterLogEntity
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.OledBlack
import com.example.ui.theme.PillViolet
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ReportScreen(
    allMedLogs: List<MedicationLogEntity>,
    allWaterLogs: List<WaterLogEntity>,
    waterGoalMl: Int,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

    val past7Days = remember(allMedLogs, allWaterLogs) {
        val list = mutableListOf<Triple<String, Int, Int>>() // Triple(DayName, WaterMl, AdherencePct)
        for (i in 6 downTo 0) {
            val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -i) }
            val dateStr = dateFormat.format(cal.time)
            val dayName = dayFormat.format(cal.time)

            val waterForDay = allWaterLogs.filter { it.dateString == dateStr }.sumOf { it.amountMl }
            val medsForDay = allMedLogs.filter { it.dateString == dateStr }
            val takenForDay = medsForDay.count { it.status == "TAKEN" }
            val totalForDay = maxOf(1, medsForDay.size)
            val pct = ((takenForDay.toFloat() / totalForDay.toFloat()) * 100).toInt().coerceAtMost(100)

            list.add(Triple(dayName, waterForDay, pct))
        }
        list
    }

    val avgAdherence = if (past7Days.isNotEmpty()) past7Days.map { it.third }.average().toInt() else 0
    val avgWater = if (past7Days.isNotEmpty()) past7Days.map { it.second }.average().toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Text(
                    text = "HABIT CONSISTENCY & ANALYTICS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = TextMuted
                )
                Text(
                    text = "Weekly Report",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = TextPrimary
                )
            }
        }

        // Summary Overview Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PillViolet.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PillViolet, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Avg Adherence", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "$avgAdherence%",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = PillViolet
                        )
                        Text(
                            text = "Last 7 days average",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HydrationCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.LocalDrink, contentDescription = null, tint = HydrationCyan, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Avg Hydration", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "$avgWater ml",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = HydrationCyan
                        )
                        Text(
                            text = "Goal: $waterGoalMl ml",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Detailed Weekly Trend Chart Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_weekly_chart_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "7-DAY TREND ANALYSIS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = TextMuted
                            )
                            Text(
                                text = "Adherence & Water Intake",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = TextPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(HydrationCyan))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Water", fontSize = 10.sp, color = TextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(PillViolet))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Meds %", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }

                    val hydCyan = HydrationCyan
                    val pillViolet = PillViolet
                    val darkSurf = DarkSurface

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(darkSurf, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val barWidth = width / 16f
                            val spacing = (width - (barWidth * 7)) / 8f

                            past7Days.forEachIndexed { index, (_, water, pct) ->
                                val left = spacing + index * (barWidth + spacing)
                                val maxWater = 4000f // 4L max scale
                                val waterRatio = (water.toFloat() / maxWater).coerceIn(0f, 1f)
                                val barHeight = waterRatio * (height - 40f)

                                // Draw Water Bar
                                drawRect(
                                    color = hydCyan.copy(alpha = 0.75f),
                                    topLeft = Offset(left, height - 30f - barHeight),
                                    size = Size(barWidth, barHeight)
                                )

                                // Draw Adherence Dot connected by line or circles
                                val dotY = height - 30f - (pct / 100f * (height - 40f))
                                drawCircle(
                                    color = pillViolet,
                                    radius = 5.dp.toPx(),
                                    center = Offset(left + barWidth / 2f, dotY)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        past7Days.forEach { (day, _, pct) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondary
                                )
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = PillViolet
                                )
                            }
                        }
                    }
                }
            }
        }

        // Habit Consistency Insights Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = SuccessEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Consistency Insights",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    val insightText = when {
                        avgAdherence >= 80 -> "🌟 Excellent consistency! You've taken over 80% of your scheduled doses on time this week. Keep up the phenomenal routine."
                        avgAdherence >= 50 -> "👍 Good effort! You are maintaining moderate medication adherence. Setting more reminders can help you hit peak consistency."
                        else -> "⚠️ Room for improvement. Try enabling push reminders in settings to stay on top of your daily medications."
                    }

                    Text(
                        text = insightText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
