package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun SettingsScreen(
    waterGoalMl: Int,
    onSetWaterGoal: (Int) -> Unit,
    reminderIntervalHours: Int,
    onSetReminderInterval: (Int) -> Unit,
    snoozeMinutes: Int,
    onSetSnoozeMinutes: (Int) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isBiometricLocked: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onBackupDatabase: () -> Unit,
    onRestoreDatabase: (String) -> Unit,
    onRevisitOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var customJsonInput by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var useOzUnit by remember { mutableStateOf(false) }

    // Conversion: 1 oz ≈ 30 ml
    val displayGoal = if (useOzUnit) (waterGoalMl / 30) else waterGoalMl
    val goalUnit = if (useOzUnit) "oz" else "ml"
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()

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
                    text = "PREFERENCES & BACKUP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = TextMuted
                )
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = TextPrimary
                )
            }
        }

        // Appearance & Theme Card (Light / Dark / System Auto Mode selection)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "APPEARANCE & THEME MODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = PillViolet
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf("Light", "Dark", "System Auto")
                        themes.forEach { mode ->
                            val isSelected = when (mode) {
                                "Light" -> !isDarkTheme
                                "Dark" -> isDarkTheme
                                else -> false
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PillViolet.copy(alpha = 0.3f) else DarkSurface)
                                    .border(1.dp, if (isSelected) PillViolet else DarkCardBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (mode == "Light" && isDarkTheme) onToggleTheme()
                                        else if (mode == "Dark" && !isDarkTheme) onToggleTheme()
                                        else if (mode == "System Auto") {
                                            if (systemDark != isDarkTheme) onToggleTheme()
                                        }
                                    }
                                    .testTag("theme_mode_$mode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PillViolet else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Biometric Security Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SuccessEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Security",
                                    tint = SuccessEmerald
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Biometric App Lock",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Secure medical & health logs with biometrics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = isBiometricLocked,
                            onCheckedChange = { onToggleBiometric(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessEmerald
                            ),
                            modifier = Modifier.testTag("biometric_switch")
                        )
                    }
                }
            }
        }

        // Water Intake Goal Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(HydrationCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Water Goal",
                                    tint = HydrationCyan
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Daily Water Goal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$displayGoal $goalUnit target",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HydrationCyan
                                )
                            }
                        }

                        // Unit toggle (ml / oz)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ML",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (!useOzUnit) HydrationCyan else Color.Transparent)
                                    .clickable { useOzUnit = false }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!useOzUnit) Color.Black else TextSecondary
                            )
                            Text(
                                text = "OZ",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (useOzUnit) HydrationCyan else Color.Transparent)
                                    .clickable { useOzUnit = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (useOzUnit) Color.Black else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = displayGoal.toFloat(),
                        onValueChange = { newVal ->
                            val mlVal = if (useOzUnit) (newVal.toInt() * 30) else newVal.toInt()
                            onSetWaterGoal(mlVal.coerceIn(500, 6000))
                        },
                        valueRange = if (useOzUnit) 16f..200f else 500f..6000f,
                        steps = 20,
                        colors = SliderDefaults.colors(
                            thumbColor = HydrationCyan,
                            activeTrackColor = HydrationCyan,
                            inactiveTrackColor = DarkCardBorder
                        ),
                        modifier = Modifier.testTag("water_goal_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (useOzUnit) "16 oz" else "500 ml", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(if (useOzUnit) "100 oz" else "3000 ml", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(if (useOzUnit) "200 oz" else "6000 ml", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }

                    // Calculator Section
                    var isCalculatorExpanded by remember { mutableStateOf(false) }
                    var weightInput by remember { mutableStateOf("70") }
                    var isWeightInKg by remember { mutableStateOf(true) }
                    var activityLevel by remember { mutableStateOf("Active") } // "Sedentary", "Active", "Athletic"

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { isCalculatorExpanded = !isCalculatorExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_calculator_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurface,
                            contentColor = HydrationCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Text(
                            text = if (isCalculatorExpanded) "Close Calculator ✕" else "Estimate My Daily Water Target 🧮",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isCalculatorExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface, RoundedCornerShape(16.dp))
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "PERSONAL HYDRATION ESTIMATOR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextMuted
                            )

                            // Weight Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Body Weight", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = { weightInput = it.filter { c -> c.isDigit() } },
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(50.dp)
                                            .testTag("weight_input"),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = HydrationCyan,
                                            unfocusedBorderColor = DarkCardBorder
                                        ),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(OledBlack)
                                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                            .padding(2.dp)
                                    ) {
                                        Text(
                                            text = "KG",
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isWeightInKg) HydrationCyan else Color.Transparent)
                                                .clickable { isWeightInKg = true }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                                .testTag("weight_kg_btn"),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWeightInKg) Color.Black else TextSecondary
                                        )
                                        Text(
                                            text = "LBS",
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (!isWeightInKg) HydrationCyan else Color.Transparent)
                                                .clickable { isWeightInKg = false }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                                .testTag("weight_lbs_btn"),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isWeightInKg) Color.Black else TextSecondary
                                        )
                                    }
                                }
                            }

                            // Activity Level Row
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Activity Level", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Sedentary", "Active", "Athletic").forEach { level ->
                                        val isSelected = activityLevel == level
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) HydrationCyan else OledBlack)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) HydrationCyan else DarkCardBorder,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable { activityLevel = level }
                                                .padding(vertical = 8.dp)
                                                .testTag("activity_${level.lowercase()}_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = level,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.Black else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // Calculations
                            val weightVal = weightInput.toDoubleOrNull() ?: 70.0
                            val weightKg = if (isWeightInKg) weightVal else weightVal * 0.45359237
                            val baseIntakeMl = weightKg * 35.0
                            val extraIntakeMl = when (activityLevel) {
                                "Sedentary" -> 0.0
                                "Active" -> 350.0
                                "Athletic" -> 750.0
                                else -> 350.0
                            }
                            val calculatedMl = (baseIntakeMl + extraIntakeMl).toInt().coerceIn(500, 6000)
                            val displaySuggested = if (useOzUnit) "${calculatedMl / 30} oz" else "$calculatedMl ml"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Suggested Target", fontSize = 11.sp, color = TextMuted)
                                    Text(
                                        text = displaySuggested,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                        color = HydrationCyan
                                    )
                                }

                                Button(
                                    onClick = {
                                        onSetWaterGoal(calculatedMl)
                                        isCalculatorExpanded = false
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HydrationCyan,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier.testTag("apply_suggested_water_btn")
                                ) {
                                    Text("Apply", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Medication Reminder Intervals Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Reminders",
                                tint = SuccessEmerald
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Reminder Intervals & Snooze",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Configure notification timing & snooze duration",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Preferred Reminder Frequency",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 4, 6, 12).forEach { hours ->
                            val isSelected = reminderIntervalHours == hours
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) SuccessEmerald.copy(alpha = 0.2f) else DarkSurface)
                                    .border(1.dp, if (isSelected) SuccessEmerald else DarkCardBorder, RoundedCornerShape(10.dp))
                                    .clickable { onSetReminderInterval(hours) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${hours}h",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) SuccessEmerald else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Snooze Duration",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 30).forEach { mins ->
                            val isSelected = snoozeMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) PillViolet.copy(alpha = 0.2f) else DarkSurface)
                                    .border(1.dp, if (isSelected) PillViolet else DarkCardBorder, RoundedCornerShape(10.dp))
                                    .clickable { onSetSnoozeMinutes(mins) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) PillViolet else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val context = LocalContext.current
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, "doseflow_reminders")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback to general app notification settings
                                val generalIntent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                try {
                                    context.startActivity(generalIntent)
                                } catch (ex: Exception) {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("custom_notification_channels_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = SuccessEmerald),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Customize Notification Channels (OS)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Database Backup & Restore Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(WarningAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Backup",
                                tint = WarningAmber
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Database Backup & Restore",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Export Room DB to JSON in device documents folder",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onBackupDatabase,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("backup_db_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber, contentColor = Color.Black)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back Up JSON", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showRestoreDialog = !showRestoreDialog },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("restore_db_toggle_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore JSON", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (showRestoreDialog) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Paste Backup JSON to Restore:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customJsonInput,
                                onValueChange = { customJsonInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .testTag("restore_json_input"),
                                placeholder = { Text("{\"medications\": [...], \"medicationLogs\": [...], \"waterLogs\": [...]}") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WarningAmber,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (customJsonInput.isNotBlank()) {
                                        onRestoreDatabase(customJsonInput)
                                        customJsonInput = ""
                                        showRestoreDialog = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("confirm_restore_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald, contentColor = Color.Black)
                            ) {
                                Text("Import & Restore Data", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Revisit Onboarding
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Button(
                        onClick = onRevisitOnboarding,
                        modifier = Modifier.fillMaxWidth().testTag("revisit_onboarding_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = PillViolet)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Revisit Welcome Onboarding", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
