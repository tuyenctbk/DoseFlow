package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MedicationEntity
import com.example.data.api.DrugInteractionResult
import com.example.data.api.DrugSearchResult
import com.example.data.api.PillDetailInfo
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.components.PillSafetySheet
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
fun MedicationsManagerScreen(
    medications: List<MedicationEntity>,
    waterGoalMl: Int,
    drugSearchResults: List<DrugSearchResult>,
    isSearchingApi: Boolean,
    cabinetInteractions: List<DrugInteractionResult>,
    selectedPillDetail: PillDetailInfo?,
    isSafetyLoading: Boolean,
    onSaveMedication: (
        id: Long, name: String, dosage: String, hour: Int, min: Int, freq: String,
        stock: Int, color: String, iconType: String, rxcui: String?, upcOrBarcode: String?,
        activeIngredients: String?, visualDescription: String?, foodWarningsSummary: String?
    ) -> Unit,
    onDeleteMedication: (MedicationEntity) -> Unit,
    onSetWaterGoal: (Int) -> Unit,
    onTestNotification: (MedicationEntity) -> Unit,
    onSearchAutoComplete: (String) -> Unit,
    onLookupBarcode: (String, (PillDetailInfo) -> Unit) -> Unit,
    onLoadSafetyDetail: (String, String?, String?) -> Unit,
    onClearSafetySheet: () -> Unit,
    onCheckCabinetInteractions: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMed by remember { mutableStateOf<MedicationEntity?>(null) }
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showSafetySheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onCheckCabinetInteractions()
    }

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

        // Cabinet Drug Interactions Radar Card
        if (cabinetInteractions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cabinet_interaction_radar"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1219)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cabinet Drug Interactions Warning",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEF4444).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${cabinetInteractions.size} ALERT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = cabinetInteractions.first().description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = {
                                val firstMed = medications.firstOrNull { it.name == cabinetInteractions.first().drug1Name }
                                onLoadSafetyDetail(
                                    cabinetInteractions.first().drug1Name,
                                    firstMed?.rxcui,
                                    firstMed?.upcOrBarcode
                                )
                                showSafetySheet = true
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = HydrationCyan),
                            modifier = Modifier.testTag("view_interaction_details_btn")
                        ) {
                            Text(stringResource(R.string.view_safety_analysis), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE MEDICATION SCHEDULES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = TextSecondary
                )

                IconButton(onClick = { showBarcodeScanner = true }) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Barcode Scan",
                        tint = HydrationCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (medications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_medications_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PillViolet.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = "No Medications",
                                tint = PillViolet,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.no_medication_schedules),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.no_meds_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    editingMed = null
                                    showAddDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PillViolet,
                                    contentColor = OledBlack
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("empty_add_med_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.add_pill), fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showBarcodeScanner = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HydrationCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, HydrationCyan),
                                modifier = Modifier.testTag("empty_scan_barcode_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.scan_barcode), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
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
                                IconButton(onClick = {
                                    onLoadSafetyDetail(med.name, med.rxcui, med.upcOrBarcode)
                                    showSafetySheet = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.HealthAndSafety,
                                        contentDescription = "Safety Details",
                                        tint = WarningAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
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
                                text = stringResource(R.string.alarm_time_format, med.timeHour, med.timeMinute),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PillViolet
                            )
                            Text(
                                text = stringResource(R.string.stock_doses_format, med.stockRemaining),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (med.stockRemaining < 5) MaterialTheme.colorScheme.error else TextMuted
                            )
                        }

                        if (!med.activeIngredients.isNullOrEmpty() || !med.foodWarningsSummary.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = DarkCardBorder)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.fda_notes_format, med.activeIngredients ?: stringResource(R.string.rxnorm_verified)),
                                fontSize = 11.sp,
                                color = HydrationCyan
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Barcode Scanner Dialog
    if (showBarcodeScanner) {
        BarcodeScannerModal(
            onDismissRequest = { showBarcodeScanner = false },
            onBarcodeDetected = { barcode ->
                onLookupBarcode(barcode) { detail ->
                    editingMed = null
                    showAddDialog = true
                }
            }
        )
    }

    // Safety & Interactions Bottom Sheet
    if (showSafetySheet) {
        PillSafetySheet(
            pillDetail = selectedPillDetail,
            cabinetInteractions = cabinetInteractions,
            isLoading = isSafetyLoading,
            onDismiss = {
                showSafetySheet = false
                onClearSafetySheet()
            }
        )
    }

    // Add / Edit Medication Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf(editingMed?.name ?: selectedPillDetail?.name ?: "") }
        var dosage by remember { mutableStateOf(editingMed?.dosage ?: selectedPillDetail?.dosageGuidance?.take(20) ?: "1 Pill") }
        var hourStr by remember { mutableStateOf((editingMed?.timeHour ?: 8).toString()) }
        var minStr by remember { mutableStateOf((editingMed?.timeMinute ?: 0).toString()) }
        var stockStr by remember { mutableStateOf((editingMed?.stockRemaining ?: 30).toString()) }
        var freq by remember { mutableStateOf(editingMed?.frequency ?: "DAILY") }
        var selectedColor by remember { mutableStateOf(editingMed?.colorHex ?: "#8B5CF6") }
        var selectedIcon by remember { mutableStateOf(editingMed?.iconType ?: "pill") }
        var currentRxcui by remember { mutableStateOf(editingMed?.rxcui ?: selectedPillDetail?.rxcui) }
        var currentUpc by remember { mutableStateOf(editingMed?.upcOrBarcode ?: selectedPillDetail?.upcOrNdc) }
        var currentActive by remember { mutableStateOf(editingMed?.activeIngredients ?: selectedPillDetail?.activeIngredients) }
        var currentVisual by remember { mutableStateOf(editingMed?.visualDescription ?: selectedPillDetail?.visualDescription) }

        var hasError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingMed == null) "Schedule New Medication" else "Edit Schedule",
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { showBarcodeScanner = true }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode",
                            tint = HydrationCyan
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Name Field with Auto-Complete Dropdown
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (it.isNotBlank()) {
                                    hasError = false
                                    onSearchAutoComplete(it)
                                }
                            },
                            isError = hasError,
                            label = { Text(stringResource(R.string.medication_pill_name_req)) },
                            placeholder = { Text(stringResource(R.string.search_medication_placeholder)) },
                            trailingIcon = {
                                if (isSearchingApi) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = PillViolet,
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PillViolet,
                                unfocusedBorderColor = DarkCardBorder,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("med_name_input")
                        )

                        // Auto-complete Suggestions Dropdown
                        if (drugSearchResults.isNotEmpty() && name.length >= 2) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .padding(top = 4.dp)
                            ) {
                                LazyColumn {
                                    items(drugSearchResults) { result ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    name = result.name
                                                    currentRxcui = result.rxcui
                                                    currentActive = result.activeIngredient ?: result.genericName
                                                    onSearchAutoComplete("") // Clear suggestions
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = result.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary
                                            )
                                            if (!result.activeIngredient.isNullOrEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.active_ingredient_format, result.activeIngredient),
                                                    fontSize = 10.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                        Divider(color = DarkCardBorder.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }

                    if (hasError) {
                        Text(
                            text = stringResource(R.string.enter_or_select_med_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text(stringResource(R.string.dosage_placeholder_label)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillViolet,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_dosage_input")
                    )

                    // Quick dosage preset chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("1 Pill", "1 Cap", "2 Tabs", "5 ml").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (dosage == preset) PillViolet.copy(alpha = 0.25f) else DarkSurface)
                                    .border(1.dp, if (dosage == preset) PillViolet else DarkCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { dosage = preset }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dosage == preset) PillViolet else TextSecondary
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hourStr,
                            onValueChange = { hourStr = it },
                            label = { Text(stringResource(R.string.hour_label)) },
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
                            label = { Text(stringResource(R.string.minute_label)) },
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
                        label = { Text(stringResource(R.string.initial_stock_count)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillViolet,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_stock_input")
                    )

                    Text(stringResource(R.string.frequency_label), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("DAILY", "TWICE DAILY", "WEEKLY", "AS NEEDED").forEach { f ->
                            val isSelected = freq == f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PillViolet else DarkSurface)
                                    .border(1.dp, if (isSelected) PillViolet else DarkCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { freq = f }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(f) {
                                        "TWICE DAILY" -> "2x/Day"
                                        "AS NEEDED" -> "PRN"
                                        else -> f.lowercase().replaceFirstChar { it.uppercase() }
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) OledBlack else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = hourStr.toIntOrNull()?.coerceIn(0, 23) ?: 8
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
                                selectedColor,
                                selectedIcon,
                                currentRxcui,
                                currentUpc,
                                currentActive,
                                currentVisual,
                                null
                            )
                            showAddDialog = false
                        } else {
                            hasError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PillViolet, contentColor = OledBlack),
                    modifier = Modifier.testTag("save_med_btn")
                ) {
                    Text(stringResource(R.string.save_schedule), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.cancel), color = TextMuted)
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
            title = { Text(stringResource(R.string.set_daily_hydration_goal)) },
            text = {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it },
                    label = { Text(stringResource(R.string.daily_water_goal_in_ml)) },
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
                    Text(stringResource(R.string.save_goal), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWaterGoalDialog = false }) {
                    Text(stringResource(R.string.cancel), color = TextMuted)
                }
            }
        )
    }
}
