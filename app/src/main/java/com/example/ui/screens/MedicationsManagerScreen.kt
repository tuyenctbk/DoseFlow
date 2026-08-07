package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicationEntity
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.OledBlack
import com.example.ui.theme.PillViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MedicationsManagerScreen(
    medications: List<MedicationEntity>,
    waterGoalMl: Int,
    onSaveMedication: (id: Long, name: String, dosage: String, hour: Int, min: Int, freq: String, stock: Int, color: String, iconType: String) -> Unit,
    onDeleteMedication: (MedicationEntity) -> Unit,
    onSetWaterGoal: (Int) -> Unit,
    onTestNotification: (MedicationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMed by remember { mutableStateOf<MedicationEntity?>(null) }
    var showWaterGoalDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MEDICATION & HYDRATION SCHEDULES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = TextMuted
                    )
                    Text(
                        text = "Schedules",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = {
                        editingMed = null
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PillViolet, contentColor = OledBlack),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_med_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New Pill", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Daily Hydration Goal Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(HydrationCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalDrink,
                                contentDescription = "Water Goal",
                                tint = HydrationCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DAILY HYDRATION GOAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = HydrationCyan
                            )
                            Text(
                                text = "${waterGoalMl} ml (${waterGoalMl / 1000f} L)",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                    }

                    TextButton(
                        onClick = { showWaterGoalDialog = true },
                        modifier = Modifier.testTag("change_water_goal_btn")
                    ) {
                        Text(text = "Change", color = HydrationCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "ACTIVE MEDICATION SCHEDULES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = TextSecondary
            )
        }

        items(medications) { med ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = "Med",
                                tint = PillViolet,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = med.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${med.dosage} • Frequency: ${med.frequency}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { onTestNotification(med) }) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Test Notification",
                                    tint = PillViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = {
                                editingMed = med
                                showAddDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = { onDeleteMedication(med) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alarm: ${String.format("%02d:%02d", med.timeHour, med.timeMinute)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PillViolet
                        )
                        Text(
                            text = "Remaining Stock: ${med.stockRemaining} doses",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (med.stockRemaining < 5) MaterialTheme.colorScheme.error else TextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Add / Edit Medication Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf(editingMed?.name ?: "") }
        var dosage by remember { mutableStateOf(editingMed?.dosage ?: "1 Pill") }
        var hourStr by remember { mutableStateOf((editingMed?.timeHour ?: 9).toString()) }
        var minStr by remember { mutableStateOf((editingMed?.timeMinute ?: 0).toString()) }
        var stockStr by remember { mutableStateOf((editingMed?.stockRemaining ?: 30).toString()) }
        var freq by remember { mutableStateOf(editingMed?.frequency ?: "DAILY") }
        var selectedIcon by remember { mutableStateOf(editingMed?.iconType ?: "pill") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = if (editingMed == null) "Schedule New Medication" else "Edit Schedule",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medication / Supplement Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillViolet,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_name_input")
                    )

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage (e.g. 1 Pill, 500mg)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillViolet,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_dosage_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hourStr,
                            onValueChange = { hourStr = it },
                            label = { Text("Hour (0-23)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("med_hour_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PillViolet,
                                unfocusedBorderColor = DarkCardBorder
                            )
                        )
                        OutlinedTextField(
                            value = minStr,
                            onValueChange = { minStr = it },
                            label = { Text("Minute (0-59)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("med_min_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PillViolet,
                                unfocusedBorderColor = DarkCardBorder
                            )
                        )
                    }

                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Initial Stock Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillViolet,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_stock_input")
                    )

                    Text("Icon Type", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val iconTypes = listOf("pill", "capsule", "syrup", "injection")
                        iconTypes.forEach { type ->
                            val isSelected = selectedIcon == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PillViolet else DarkSurface)
                                    .border(1.dp, if (isSelected) PillViolet else DarkCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedIcon = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) OledBlack else TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = hourStr.toIntOrNull()?.coerceIn(0, 23) ?: 9
                        val m = minStr.toIntOrNull()?.coerceIn(0, 59) ?: 0
                        val s = stockStr.toIntOrNull()?.coerceAtLeast(0) ?: 30

                        if (name.isNotBlank()) {
                            onSaveMedication(
                                editingMed?.id ?: 0L,
                                name.trim(),
                                dosage.trim(),
                                h,
                                m,
                                freq,
                                s,
                                "#8B5CF6",
                                selectedIcon
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PillViolet, contentColor = OledBlack),
                    modifier = Modifier.testTag("save_med_btn")
                ) {
                    Text("Save Schedule", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Water Goal Dialog
    if (showWaterGoalDialog) {
        var goalInput by remember { mutableStateOf(waterGoalMl.toString()) }

        AlertDialog(
            onDismissRequest = { showWaterGoalDialog = false },
            containerColor = DarkCard,
            titleContentColor = TextPrimary,
            title = { Text("Set Daily Hydration Goal (ml)") },
            text = {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it },
                    label = { Text("Daily Water Goal in ml") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HydrationCyan,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("water_goal_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val g = goalInput.toIntOrNull()?.coerceIn(500, 10000) ?: 2000
                        onSetWaterGoal(g)
                        showWaterGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HydrationCyan, contentColor = OledBlack),
                    modifier = Modifier.testTag("save_water_goal_btn")
                ) {
                    Text("Save Goal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWaterGoalDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}
