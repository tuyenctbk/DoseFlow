package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface UnifiedLog {
    val id: Long
    val timestamp: Long

    data class Medication(val log: MedicationLogEntity) : UnifiedLog {
        override val id: Long get() = log.id
        override val timestamp: Long get() = log.timestamp
    }

    data class Water(val log: WaterLogEntity) : UnifiedLog {
        override val id: Long get() = log.id
        override val timestamp: Long get() = log.timestamp
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryAndExportScreen(
    allMedLogs: List<MedicationLogEntity>,
    allWaterLogs: List<WaterLogEntity>,
    csvString: String?,
    onGenerateCsv: () -> Unit,
    onClearCsv: () -> Unit,
    onDeleteLogs: (List<MedicationLogEntity>, List<WaterLogEntity>) -> Unit,
    onUpdateMedLog: (MedicationLogEntity) -> Unit,
    onUpdateWaterLog: (WaterLogEntity) -> Unit,
    onRevisitOnboarding: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null && csvString != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvString.toByteArray())
                }
                Toast.makeText(context, "CSV exported successfully to document storage!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to export CSV: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val unifiedLogs = androidx.compose.runtime.remember(allMedLogs, allWaterLogs) {
        val list = mutableListOf<UnifiedLog>()
        allMedLogs.forEach { list.add(UnifiedLog.Medication(it)) }
        allWaterLogs.forEach { list.add(UnifiedLog.Water(it)) }
        list.sortByDescending { it.timestamp }
        list
    }

    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedMedLogs = remember { mutableStateListOf<MedicationLogEntity>() }
    val selectedWaterLogs = remember { mutableStateListOf<WaterLogEntity>() }

    var editingMedLog by remember { mutableStateOf<MedicationLogEntity?>(null) }
    var editingWaterLog by remember { mutableStateOf<WaterLogEntity?>(null) }

    Box(modifier = modifier.fillMaxSize().background(OledBlack)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Text(
                    text = "LOCAL LOGS & DATA EXPORT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = TextMuted
                )
                Text(
                    text = "Health Logs",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = TextPrimary
                )
            }
        }

        // Privacy Guarantee Card
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SuccessEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Privacy",
                                tint = SuccessEmerald
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "100% LOCAL PRIVACY GUARANTEE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = SuccessEmerald
                            )
                            Text(
                                text = "Your health, medication, and water records never leave this device. Zero ads, zero accounts, $0 subscription.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    if (onRevisitOnboarding != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRevisitOnboarding,
                            modifier = Modifier.fillMaxWidth().testTag("revisit_onboarding_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurface,
                                contentColor = TextPrimary
                            )
                        ) {
                            Text(text = "Revisit Onboarding & Permissions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // CSV Export Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DOCTOR VISIT REPORT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PillViolet
                            )
                            Text(
                                text = "Export History to CSV",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = onGenerateCsv,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PillViolet,
                                contentColor = OledBlack
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("generate_csv_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Export CSV", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (csvString != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = csvString,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            Button(
                                onClick = {
                                    exportDocumentLauncher.launch("doseflow_health_backup.csv")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PillViolet,
                                    contentColor = OledBlack
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("save_csv_storage_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SaveAlt,
                                    contentDescription = "Save",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.export_csv_file), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("DoseFlow CSV Export", csvString)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied CSV to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkSurface,
                                    contentColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("copy_csv_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Timeline Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CHRONOLOGICAL HEALTH TIMELINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = TextSecondary
                    )
                    Text(
                        text = "${unifiedLogs.size} Total Logs" + if (isSelectionMode) " • Selection Mode" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isSelectionMode) {
                        Button(
                            onClick = { isSelectionMode = true },
                            modifier = Modifier.testTag("enable_selection_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = SuccessEmerald),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Select", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                isSelectionMode = false
                                selectedMedLogs.clear()
                                selectedWaterLogs.clear()
                            },
                            modifier = Modifier.testTag("cancel_selection_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = TextMuted),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (unifiedLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No logs recorded yet today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(unifiedLogs.take(50)) { item ->
                val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                when (item) {
                    is UnifiedLog.Medication -> {
                        val log = item.log
                        val isSelected = selectedMedLogs.any { it.id == log.id }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            if (isSelected) selectedMedLogs.removeAll { it.id == log.id }
                                            else selectedMedLogs.add(log)
                                        } else {
                                            editingMedLog = log
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedMedLogs.add(log)
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PillViolet.copy(alpha = 0.15f) else DarkCard
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) PillViolet else DarkCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    if (isSelectionMode) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedMedLogs.add(log)
                                                else selectedMedLogs.removeAll { it.id == log.id }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = PillViolet,
                                                uncheckedColor = TextMuted
                                            ),
                                            modifier = Modifier.size(20.dp).testTag("checkbox_med_log_${log.id}")
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Medication,
                                        contentDescription = "Med Log",
                                        tint = PillViolet,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = log.medicationName,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${log.dosage} • $dateStr",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = log.status,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = when (log.status) {
                                            "TAKEN" -> SuccessEmerald
                                            "SNOOZED" -> PillViolet
                                            else -> TextMuted
                                        }
                                    )
                                    if (!isSelectionMode) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit log status",
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp).clickable { editingMedLog = log }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is UnifiedLog.Water -> {
                        val log = item.log
                        val isSelected = selectedWaterLogs.any { it.id == log.id }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            if (isSelected) selectedWaterLogs.removeAll { it.id == log.id }
                                            else selectedWaterLogs.add(log)
                                        } else {
                                            editingWaterLog = log
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedWaterLogs.add(log)
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) HydrationCyan.copy(alpha = 0.15f) else DarkCard
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) HydrationCyan else DarkCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    if (isSelectionMode) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedWaterLogs.add(log)
                                                else selectedWaterLogs.removeAll { it.id == log.id }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = HydrationCyan,
                                                uncheckedColor = TextMuted
                                            ),
                                            modifier = Modifier.size(20.dp).testTag("checkbox_water_log_${log.id}")
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }

                                    Icon(
                                        imageVector = Icons.Default.LocalDrink,
                                        contentDescription = "Water Log",
                                        tint = HydrationCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "+${log.amountMl} ml Water Intake",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "LOGGED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = HydrationCyan
                                    )
                                    if (!isSelectionMode) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit water amount",
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp).clickable { editingWaterLog = log }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Batch Action Floating Panel
    if (isSelectionMode && (selectedMedLogs.isNotEmpty() || selectedWaterLogs.isNotEmpty())) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${selectedMedLogs.size + selectedWaterLogs.size} logs selected",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${selectedMedLogs.size} Meds, ${selectedWaterLogs.size} Water",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onDeleteLogs(selectedMedLogs.toList(), selectedWaterLogs.toList())
                            selectedMedLogs.clear()
                            selectedWaterLogs.clear()
                            isSelectionMode = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("batch_delete_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    } // Closes Box

    editingMedLog?.let { log ->
        EditMedicationLogDialog(
            log = log,
            onDismiss = { editingMedLog = null },
            onConfirm = { updated ->
                onUpdateMedLog(updated)
                editingMedLog = null
            }
        )
    }

    editingWaterLog?.let { log ->
        EditWaterLogDialog(
            log = log,
            onDismiss = { editingWaterLog = null },
            onConfirm = { updated ->
                onUpdateWaterLog(updated)
                editingWaterLog = null
            }
        )
    }
}

// Edit Medication Log Dialog
@Composable
fun EditMedicationLogDialog(
    log: MedicationLogEntity,
    onDismiss: () -> Unit,
    onConfirm: (MedicationLogEntity) -> Unit
) {
    var currentStatus by remember(log.id) { mutableStateOf(log.status) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Medication Log",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Change logging status for ${log.medicationName}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                listOf("TAKEN", "SNOOZED", "SKIPPED").forEach { statusOption ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentStatus = statusOption }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = currentStatus == statusOption,
                            onClick = { currentStatus = statusOption },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PillViolet,
                                unselectedColor = TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusOption,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (currentStatus == statusOption) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(log.copy(status = currentStatus))
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = SuccessEmerald)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = DarkCard
    )
}

// Edit Water Log Dialog
@Composable
fun EditWaterLogDialog(
    log: WaterLogEntity,
    onDismiss: () -> Unit,
    onConfirm: (WaterLogEntity) -> Unit
) {
    var mlInput by remember(log.id) { mutableStateOf(log.amountMl.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Water Log",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Modify water intake amount in ml:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = mlInput,
                    onValueChange = { mlInput = it },
                    modifier = Modifier.fillMaxWidth().testTag("edit_water_ml_input"),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HydrationCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = mlInput.toIntOrNull() ?: log.amountMl
                    onConfirm(log.copy(amountMl = amt))
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = HydrationCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = DarkCard
    )
}
