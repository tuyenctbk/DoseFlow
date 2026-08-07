package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DoseFlowRepository
import com.example.data.MedicationEntity
import com.example.data.MedicationLogEntity
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

    init {
        val dao = AppDatabase.getDatabase(context).doseFlowDao()
        repository = DoseFlowRepository(dao, context)

        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
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

    fun setWaterGoal(goalMl: Int) {
        repository.setWaterGoalMl(goalMl)
        _waterGoalMl.value = goalMl
        viewModelScope.launch {
            _userMessage.emit(UserMessage.Toast("🎯 Hydration goal set to ${goalMl}ml"))
        }
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
        iconType: String = "pill"
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
                iconType = iconType
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

            _userMessage.emit(UserMessage.Toast("💊 Saved ${name} schedule"))
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
            ReminderScheduler.cancelMedicationAlarm(context, medication.id)
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
