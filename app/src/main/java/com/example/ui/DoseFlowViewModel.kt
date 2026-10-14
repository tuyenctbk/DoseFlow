package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DoseFlowRepository
import com.example.data.MedicationEntity
import com.example.data.MedicationLogEntity
import com.example.data.WaterDataStoreManager
import com.example.data.WaterLogEntity
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UserMessage {
    data class Toast(val message: String) : UserMessage()
}

class DoseFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DoseFlowRepository
    private val context = application.applicationContext
    private val waterDataStoreManager = WaterDataStoreManager(context)

    val dsWaterCount: StateFlow<Int> = waterDataStoreManager.dailyWaterCountFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        val dao = AppDatabase.getDatabase(context).doseFlowDao()
        repository = DoseFlowRepository(dao, context)
    }

    val medications: StateFlow<List<MedicationEntity>> = repository.allMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayMedLogs: StateFlow<List<MedicationLogEntity>> = repository.getMedLogsForToday()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayWaterLogs: StateFlow<List<WaterLogEntity>> = repository.getWaterLogsForToday()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayWaterSum: StateFlow<Int> = repository.getWaterSumForToday()
        .map { it ?: 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _waterGoalMl = MutableStateFlow(repository.getWaterGoalMl())
    val waterGoalMl: StateFlow<Int> = _waterGoalMl.asStateFlow()

    private val _reminderIntervalHours = MutableStateFlow(repository.getReminderIntervalHours())
    val reminderIntervalHours: StateFlow<Int> = _reminderIntervalHours.asStateFlow()

    private val _snoozeMinutes = MutableStateFlow(repository.getSnoozeMinutes())
    val snoozeMinutes: StateFlow<Int> = _snoozeMinutes.asStateFlow()

    fun setReminderIntervalHours(hours: Int) {
        repository.setReminderIntervalHours(hours)
        _reminderIntervalHours.value = hours
        viewModelScope.launch {
            _userMessage.emit(UserMessage.Toast("⏱️ Reminder interval set to every ${hours}h"))
        }
    }

    fun setSnoozeMinutes(minutes: Int) {
        repository.setSnoozeMinutes(minutes)
        _snoozeMinutes.value = minutes
        viewModelScope.launch {
            _userMessage.emit(UserMessage.Toast("⏰ Snooze duration set to ${minutes}m"))
        }
    }

    fun backupDatabase() {
        viewModelScope.launch {
            try {
                val path = repository.backupDatabaseToFile()
                _userMessage.emit(UserMessage.Toast("💾 Backup saved to Documents"))
            } catch (e: Exception) {
                _userMessage.emit(UserMessage.Toast("❌ Backup failed: ${e.localizedMessage}"))
            }
        }
    }

    fun restoreDatabase(jsonString: String) {
        viewModelScope.launch {
            try {
                repository.restoreDatabaseFromJsonString(jsonString)
                _userMessage.emit(UserMessage.Toast("📂 Database restored successfully!"))
            } catch (e: Exception) {
                _userMessage.emit(UserMessage.Toast("❌ Restore failed: ${e.localizedMessage}"))
            }
        }
    }

    private val _isOnboardingCompleted = MutableStateFlow(repository.isOnboardingCompleted())
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(repository.isDarkThemeEnabled())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isBiometricLocked = MutableStateFlow(repository.isBiometricLocked())
    val isBiometricLocked: StateFlow<Boolean> = _isBiometricLocked.asStateFlow()

    fun setBiometricLocked(locked: Boolean) {
        repository.setBiometricLocked(locked)
        _isBiometricLocked.value = locked
        viewModelScope.launch {
            _userMessage.emit(UserMessage.Toast(if (locked) "🔒 Biometric App Security Enabled" else "🔓 Biometric App Security Disabled"))
        }
    }

    fun toggleTheme() {
        val newMode = !_isDarkTheme.value
        repository.setDarkThemeEnabled(newMode)
        _isDarkTheme.value = newMode
    }

    fun completeOnboarding() {
        repository.setOnboardingCompleted(true)
        _isOnboardingCompleted.value = true
        viewModelScope.launch {
            _userMessage.emit(UserMessage.Toast("🚀 Welcome to DoseFlow!"))
        }
    }

    fun resetOnboarding() {
        repository.setOnboardingCompleted(false)
        _isOnboardingCompleted.value = false
    }

    val allMedLogs: StateFlow<List<MedicationLogEntity>> = repository.allMedLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allWaterLogs: StateFlow<List<WaterLogEntity>> = repository.allWaterLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _csvString = MutableStateFlow<String?>(null)
    val csvString: StateFlow<String?> = _csvString.asStateFlow()

    private val _userMessage = MutableSharedFlow<UserMessage>()
    val userMessage: SharedFlow<UserMessage> = _userMessage.asSharedFlow()

    // Calculate daily med compliance percent
    val dailyMedCompletionPercent: StateFlow<Int> = combine(
        medications,
        todayMedLogs
    ) { meds, logs ->
        if (meds.isEmpty()) return@combine 100
        val takenCount = logs.count { it.status == "TAKEN" }
        ((takenCount.toFloat() / meds.size.toFloat()) * 100).toInt().coerceAtMost(100)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun logMedicationAction(medication: MedicationEntity, status: String) {
        viewModelScope.launch {
            repository.logMedicationAction(
                medicationId = medication.id,
                medicationName = medication.name,
                dosage = medication.dosage,
                status = status
            )
            val msg = when (status) {
                "TAKEN" -> "✅ Logged: ${medication.name} taken"
                "SNOOZED" -> "⏰ Snoozed ${medication.name} for 15 mins"
                "SKIPPED" -> "🚫 Skipped ${medication.name}"
                else -> "Logged ${medication.name}"
            }
            _userMessage.emit(UserMessage.Toast(msg))
        }
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            repository.logWater(amountMl)
            waterDataStoreManager.addWater(amountMl)
            val msg = if (amountMl >= 0) "💧 +${amountMl}ml Water logged!" else "💧 ${amountMl}ml Water adjusted!"
            _userMessage.emit(UserMessage.Toast(msg))
        }
    }

    fun undoLastWater() {
        viewModelScope.launch {
            repository.undoLastWaterLog()
            _userMessage.emit(UserMessage.Toast("↩️ Undid last water log"))
        }
    }

    fun undoLastMedication() {
        viewModelScope.launch {
            repository.undoLastMedLog()
            _userMessage.emit(UserMessage.Toast("↩️ Undid last medication action"))
        }
    }

    private var lastDeletedMedLogs = listOf<MedicationLogEntity>()
    private var lastDeletedWaterLogs = listOf<WaterLogEntity>()

    fun deleteMedicationLog(log: MedicationLogEntity) {
        viewModelScope.launch {
            lastDeletedMedLogs = listOf(log)
            lastDeletedWaterLogs = emptyList()
            repository.deleteMedLog(log)
            _userMessage.emit(UserMessage.Toast("🗑️ Deleted medication log"))
        }
    }

    fun deleteWaterLog(log: WaterLogEntity) {
        viewModelScope.launch {
            lastDeletedWaterLogs = listOf(log)
            lastDeletedMedLogs = emptyList()
            repository.deleteWaterLog(log)
            _userMessage.emit(UserMessage.Toast("🗑️ Deleted water log"))
        }
    }

    fun updateMedicationLog(log: MedicationLogEntity) {
        viewModelScope.launch {
            repository.updateMedLog(log)
            _userMessage.emit(UserMessage.Toast("✏️ Updated medication log"))
        }
    }

    fun updateWaterLog(log: WaterLogEntity) {
        viewModelScope.launch {
            repository.updateWaterLog(log)
            _userMessage.emit(UserMessage.Toast("✏️ Updated water log"))
        }
    }

    fun deleteLogs(medLogs: List<MedicationLogEntity>, waterLogs: List<WaterLogEntity>) {
        viewModelScope.launch {
            lastDeletedMedLogs = medLogs
            lastDeletedWaterLogs = waterLogs
            medLogs.forEach { repository.deleteMedLog(it) }
            waterLogs.forEach { repository.deleteWaterLog(it) }
            _userMessage.emit(UserMessage.Toast("🗑️ Deleted ${medLogs.size + waterLogs.size} logs"))
        }
    }

    fun undoLastDeletion() {
        viewModelScope.launch {
            lastDeletedMedLogs.forEach { repository.insertMedLog(it) }
            lastDeletedWaterLogs.forEach { repository.insertWaterLog(it) }
            lastDeletedMedLogs = emptyList()
            lastDeletedWaterLogs = emptyList()
            _userMessage.emit(UserMessage.Toast("↩️ Restored deleted items"))
        }
    }

    fun setWaterGoal(goalMl: Int) {
        repository.setWaterGoalMl(goalMl)
        _waterGoalMl.value = goalMl
        viewModelScope.launch {
            _userMessage.emit(UserMessage.Toast("🎯 Hydration goal set to ${goalMl}ml"))
        }
    }

    // API Auto-complete & Search State
    private val _drugSearchResults = MutableStateFlow<List<com.example.data.api.DrugSearchResult>>(emptyList())
    val drugSearchResults: StateFlow<List<com.example.data.api.DrugSearchResult>> = _drugSearchResults.asStateFlow()

    private val _isApiSearching = MutableStateFlow(false)
    val isApiSearching: StateFlow<Boolean> = _isApiSearching.asStateFlow()

    // Pill Detail & Safety State
    private val _selectedPillDetail = MutableStateFlow<com.example.data.api.PillDetailInfo?>(null)
    val selectedPillDetail: StateFlow<com.example.data.api.PillDetailInfo?> = _selectedPillDetail.asStateFlow()

    private val _isSafetyLoading = MutableStateFlow(false)
    val isSafetyLoading: StateFlow<Boolean> = _isSafetyLoading.asStateFlow()

    // Cabinet Interactions State
    private val _cabinetInteractions = MutableStateFlow<List<com.example.data.api.DrugInteractionResult>>(emptyList())
    val cabinetInteractions: StateFlow<List<com.example.data.api.DrugInteractionResult>> = _cabinetInteractions.asStateFlow()

    fun searchDrugsAutoComplete(query: String) {
        if (query.trim().length < 2) {
            _drugSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isApiSearching.value = true
            try {
                val results = com.example.data.api.DrugRepository.searchDrugs(query)
                _drugSearchResults.value = results
            } catch (e: Exception) {
                _drugSearchResults.value = emptyList()
            } finally {
                _isApiSearching.value = false
            }
        }
    }

    fun lookupBarcode(barcodeOrNdc: String, onFound: (com.example.data.api.PillDetailInfo) -> Unit) {
        viewModelScope.launch {
            _isSafetyLoading.value = true
            _userMessage.emit(UserMessage.Toast("🔍 Looking up barcode $barcodeOrNdc on OpenFDA..."))
            try {
                val detail = com.example.data.api.DrugRepository.searchByBarcodeOrUpc(barcodeOrNdc)
                if (detail != null) {
                    _selectedPillDetail.value = detail
                    onFound(detail)
                    _userMessage.emit(UserMessage.Toast("✨ Found ${detail.name} on OpenFDA!"))
                } else {
                    _userMessage.emit(UserMessage.Toast("⚠️ No official FDA record found for code: $barcodeOrNdc"))
                }
            } catch (e: Exception) {
                _userMessage.emit(UserMessage.Toast("❌ Barcode lookup failed: ${e.localizedMessage}"))
            } finally {
                _isSafetyLoading.value = false
            }
        }
    }

    fun checkCabinetInteractions() {
        viewModelScope.launch {
            _isSafetyLoading.value = true
            try {
                val activeMeds = medications.value.filter { it.isActive }.map { it.name to it.rxcui }
                val inters = com.example.data.api.DrugRepository.checkDrugInteractions(activeMeds)
                _cabinetInteractions.value = inters
            } catch (e: Exception) {
                _cabinetInteractions.value = emptyList()
            } finally {
                _isSafetyLoading.value = false
            }
        }
    }

    fun loadPillDetailAndSafety(medicationName: String, rxcui: String? = null, upc: String? = null) {
        viewModelScope.launch {
            _isSafetyLoading.value = true
            try {
                val detail = com.example.data.api.DrugRepository.getPillDetailInfo(medicationName, rxcui, upc)
                _selectedPillDetail.value = detail

                val activeMeds = medications.value.filter { it.isActive }.map { it.name to it.rxcui }
                val inters = com.example.data.api.DrugRepository.checkDrugInteractions(activeMeds)
                _cabinetInteractions.value = inters
            } catch (e: Exception) {
                _userMessage.emit(UserMessage.Toast("⚠️ Could not load online safety details"))
            } finally {
                _isSafetyLoading.value = false
            }
        }
    }

    fun clearPillSafetySheet() {
        _selectedPillDetail.value = null
    }

    fun saveMedication(
        id: Long = 0,
        name: String,
        dosage: String,
        timeHour: Int,
        timeMinute: Int,
        frequency: String,
        stock: Int,
        colorHex: String,
        iconType: String = "pill",
        rxcui: String? = null,
        upcOrBarcode: String? = null,
        activeIngredients: String? = null,
        visualDescription: String? = null,
        foodWarningsSummary: String? = null
    ) {
        viewModelScope.launch {
            val med = MedicationEntity(
                id = id,
                name = name,
                dosage = dosage,
                timeHour = timeHour,
                timeMinute = timeMinute,
                frequency = frequency,
                stockRemaining = stock,
                colorHex = colorHex,
                iconType = iconType,
                rxcui = rxcui,
                upcOrBarcode = upcOrBarcode,
                activeIngredients = activeIngredients,
                visualDescription = visualDescription,
                foodWarningsSummary = foodWarningsSummary
            )
            val insertedId = repository.insertMedication(med)
            val finalId = if (id == 0L) insertedId else id

            // Schedule Local Alarm
            ReminderScheduler.scheduleMedicationAlarm(
                context = context,
                medicationId = finalId,
                medicationName = name,
                dosage = dosage,
                hour = timeHour,
                minute = timeMinute
            )

            // Re-check cabinet drug interactions
            checkCabinetInteractions()

            _userMessage.emit(UserMessage.Toast("💊 Saved ${name} schedule"))
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
            ReminderScheduler.cancelMedicationAlarm(context, medication.id)
            checkCabinetInteractions()
            _userMessage.emit(UserMessage.Toast("Deleted ${medication.name}"))
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            val csv = repository.generateCsvExport()
            _csvString.value = csv
            _userMessage.emit(UserMessage.Toast("📄 CSV Generated! Tap to copy or share"))
        }
    }

    fun clearCsv() {
        _csvString.value = null
    }

    fun testSendReminderNotification(medication: MedicationEntity) {
        ReminderScheduler.scheduleMedicationAlarm(
            context = context,
            medicationId = medication.id,
            medicationName = medication.name,
            dosage = medication.dosage,
            hour = medication.timeHour,
            minute = medication.timeMinute
        )
        viewModelScope.launch {
            _userMessage.emit(UserMessage.Toast("🔔 Test alarm scheduled for ${medication.name}"))
        }
    }
}
