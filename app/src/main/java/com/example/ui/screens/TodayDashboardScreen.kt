package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicationEntity
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
import java.util.Date
import java.util.Locale

@Composable
fun TodayDashboardScreen(
    medications: List<MedicationEntity>,
    todayMedLogs: List<MedicationLogEntity>,
    allMedLogs: List<MedicationLogEntity>,
    allWaterLogs: List<WaterLogEntity>,
    waterSumMl: Int,
    waterGoalMl: Int,
    completionPercent: Int,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onTakePill: (MedicationEntity) -> Unit,
    onSnoozePill: (MedicationEntity) -> Unit,
    onSkipPill: (MedicationEntity) -> Unit,
    onLogWater: (Int) -> Unit,
    onUndoWater: () -> Unit,
    onOpenAddMedication: () -> Unit,
    onTestNotification: (MedicationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val dateFormatted = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())

    // Identify active and pending medications
    val activeMeds = medications.filter { it.isActive }
    val loggedIds = todayMedLogs.map { it.medicationId }
    val pendingMeds = activeMeds.filter { it.id !in loggedIds }
    val nextMedication = pendingMeds.firstOrNull()
    val takenCount = todayMedLogs.count { it.status == "TAKEN" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateFormatted.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = TextMuted
                    )
                    Text(
                        text = "DoseFlow",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextPrimary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .border(1.dp, DarkCardBorder, CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleTheme()
                            }
                            .testTag("theme_toggle_header_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = WarningAmber
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .border(1.dp, DarkCardBorder, CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onOpenAddMedication()
                            }
                            .testTag("add_medication_header_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Medication",
                            tint = PillViolet
                        )
                    }
                }
            }
        }

        // 1. Daily Summary View (Medication Adherence % & Water Goals Met)
        item {
            DailySummaryView(
                completionPercent = completionPercent,
                takenCount = takenCount,
                totalActiveCount = activeMeds.size,
                waterSumMl = waterSumMl,
                waterGoalMl = waterGoalMl
            )
        }

        // 2. Upcoming Medication Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upcoming_medication_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "UPCOMING MEDICATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = PillViolet
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (nextMedication != null) {
                                Text(
                                    text = nextMedication.name,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Italic
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${nextMedication.dosage} • Stock: ${nextMedication.stockRemaining}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            } else {
                                Text(
                                    text = "All Clear!",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Italic
                                    ),
                                    color = SuccessEmerald
                                )
                                Text(
                                    text = "No pending dosages scheduled for today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (nextMedication != null) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PillViolet.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = "Dose",
                                    tint = PillViolet,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (nextMedication != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PillViolet)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SCHEDULED FOR ${String.format("%02d:%02d", nextMedication.timeHour, nextMedication.timeMinute)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = TextSecondary
                            )
                        }

                        var isSuccessMed by remember { mutableStateOf(false) }
                        val coroutineScope = rememberCoroutineScope()
                        val medScale by animateFloatAsState(if (isSuccessMed) 1.05f else 1.0f, label = "med_scale")
                        val medColor by animateColorAsState(if (isSuccessMed) SuccessEmerald else PillViolet, label = "med_color")

                        // 1-Tap Primary Action Button with Haptic Feedback and Success Animation
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isSuccessMed = true
                                coroutineScope.launch {
                                    delay(400)
                                    onTakePill(nextMedication)
                                    isSuccessMed = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .graphicsLayer {
                                    scaleX = medScale
                                    scaleY = medScale
                                }
                                .testTag("log_dosage_now_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = medColor,
                                contentColor = OledBlack
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Log",
                                    tint = OledBlack
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSuccessMed) "Dosage Logged! ✓" else "Log Dosage Now",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary Row (Snooze / Test Alarm) with Haptics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSnoozePill(nextMedication)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("snooze_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkSurface,
                                    contentColor = TextSecondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Snooze,
                                    contentDescription = "Snooze",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Snooze 15m", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onTestNotification(nextMedication)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("test_alarm_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkSurface,
                                    contentColor = TextSecondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Test Notification",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Test Alarm", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Water Counter UI Component (with Daily Reset & 1-Tap Haptic Increment/Decrement)
        item {
            WaterCounterComponent(
                waterSumMl = waterSumMl,
                waterGoalMl = waterGoalMl,
                onLogWater = { amount ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLogWater(amount)
                },
                onUndoWater = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUndoWater()
                }
            )
        }

        // Stock Alerts Section (Low stock < 5)
        val lowStockMeds = medications.filter { it.stockRemaining < 5 && it.isActive }
        if (lowStockMeds.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = WarningAmber
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "LOW PILL STOCK REMINDER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = WarningAmber
                            )
                            Text(
                                text = lowStockMeds.joinToString { "${it.name} (${it.stockRemaining} left)" },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Today's Medication Schedule Checklist
        item {
            Text(
                text = "TODAY'S SCHEDULED DOSES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (medications.isEmpty()) {
            item {
                Text(
                    text = "No medications scheduled yet. Tap + above to add your pills or supplements.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(medications) { med ->
                val log = todayMedLogs.find { it.medicationId == med.id }
                val isTaken = log?.status == "TAKEN"
                val isSkipped = log?.status == "SKIPPED"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isTaken) SuccessEmerald.copy(alpha = 0.5f) else DarkCardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isTaken) SuccessEmerald
                                        else if (isSkipped) TextMuted
                                        else PillViolet
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = med.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isTaken) TextMuted else TextPrimary
                                )
                                Text(
                                    text = "${med.dosage} • ${String.format("%02d:%02d", med.timeHour, med.timeMinute)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (isTaken) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Taken",
                                    tint = SuccessEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "TAKEN",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SuccessEmerald
                                )
                            }
                        } else if (isSkipped) {
                            Text(
                                text = "SKIPPED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextMuted
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSkipPill(med)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Skip",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PillViolet)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onTakePill(med)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "TAKE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = OledBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Daily Summary View component displaying total medication adherence percentage
 * and water goals met for the current day.
 */
@Composable
fun DailySummaryView(
    completionPercent: Int,
    takenCount: Int,
    totalActiveCount: Int,
    waterSumMl: Int,
    waterGoalMl: Int,
    modifier: Modifier = Modifier
) {
    val waterGoalPercent = if (waterGoalMl > 0) {
        ((waterSumMl.toFloat() / waterGoalMl.toFloat()) * 100).toInt().coerceAtMost(100)
    } else 0

    val isWaterGoalMet = waterSumMl >= waterGoalMl

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_summary_view"),
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
                Text(
                    text = "TODAY'S SUMMARY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = TextMuted
                )
                Text(
                    text = if (completionPercent == 100 && isWaterGoalMet) "All Goals Met! 🎉" else "In Progress",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (completionPercent == 100 && isWaterGoalMet) SuccessEmerald else WarningAmber
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Medication Adherence Summary Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(PillViolet.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = "Medication Adherence",
                                    tint = PillViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "$completionPercent%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = PillViolet
                            )
                        }

                        Column {
                            Text(
                                text = "Medication Adherence",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1
                            )
                            Text(
                                text = "$takenCount / $totalActiveCount doses taken",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = TextPrimary
                            )
                        }

                        val progressFloat = (completionPercent / 100f).coerceIn(0f, 1f)
                        val animatedProgress by animateFloatAsState(
                            targetValue = progressFloat,
                            animationSpec = tween(600),
                            label = "MedAdherenceProgress"
                        )
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = PillViolet,
                            trackColor = DarkCardBorder
                        )
                    }
                }

                // Water Goals Met Summary Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(HydrationCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalDrink,
                                    contentDescription = "Water Goal Met",
                                    tint = HydrationCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "$waterGoalPercent%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = HydrationCyan
                            )
                        }

                        Column {
                            Text(
                                text = "Water Goal Met",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1
                            )
                            Text(
                                text = if (isWaterGoalMet) "Goal Reached! 💧" else "${waterSumMl} / ${waterGoalMl} ml",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = TextPrimary
                            )
                        }

                        val waterProgressFloat = (waterGoalPercent / 100f).coerceIn(0f, 1f)
                        val animatedWaterProgress by animateFloatAsState(
                            targetValue = waterProgressFloat,
                            animationSpec = tween(600),
                            label = "WaterGoalProgress"
                        )
                        LinearProgressIndicator(
                            progress = { animatedWaterProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = HydrationCyan,
                            trackColor = DarkCardBorder
                        )
                    }
                }
            }
        }
    }
}

/**
 * Water Counter Component providing an interactive increment/decrement counter UI
 * with daily reset mechanism at midnight and 1-tap tactile feedback.
 */
@Composable
fun WaterCounterComponent(
    waterSumMl: Int,
    waterGoalMl: Int,
    onLogWater: (Int) -> Unit,
    onUndoWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isSuccessWater by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val waterScale by animateFloatAsState(if (isSuccessWater) 1.05f else 1.0f, label = "water_scale")
    val waterColor by animateColorAsState(if (isSuccessWater) SuccessEmerald else HydrationCyan, label = "water_color")

    val glassesCount = waterSumMl / 250
    val targetGlasses = if (waterGoalMl > 0) waterGoalMl / 250 else 8
    val waterRatio = (waterSumMl.toFloat() / waterGoalMl.toFloat()).coerceIn(0f, 1f)
    val animatedWaterRatio by animateFloatAsState(
        targetValue = waterRatio,
        animationSpec = tween(durationMillis = 800), label = "WaterCounterRing"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("water_counter_component"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = "Water Counter",
                        tint = HydrationCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WATER COUNTER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = HydrationCyan
                    )
                }

                // Reset badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Resets at Midnight 🌙",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress Ring Display
                val darkSurf = DarkSurface
                val hydCyan = HydrationCyan
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 10.dp.toPx()
                        drawArc(
                            color = darkSurf,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                        drawArc(
                            color = hydCyan,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedWaterRatio,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$glassesCount",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "GLASSES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = HydrationCyan,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Counter Stats & Direct Increment/Decrement Controls
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column {
                        Text(
                            text = "${waterSumMl} ml / ${waterGoalMl} ml",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Goal: $targetGlasses glasses (250ml each)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    // Interactive Counter Buttons (+ / - Glass Controls)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decrement / Undo Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                                .clickable { onUndoWater() }
                                .testTag("counter_decrement_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Remove Glass",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "-1 Glass",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                            }
                        }

                        // Increment Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .graphicsLayer {
                                    scaleX = waterScale
                                    scaleY = waterScale
                                }
                                .clip(RoundedCornerShape(14.dp))
                                .background(waterColor)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isSuccessWater = true
                                    onLogWater(250)
                                    coroutineScope.launch {
                                        delay(800)
                                        isSuccessWater = false
                                    }
                                }
                                .testTag("counter_increment_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Glass",
                                    tint = OledBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSuccessWater) "Logged! ✓" else "+1 Glass",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OledBlack
                                )
                            }
                        }
                    }
                }
            }


        }
    }
}
