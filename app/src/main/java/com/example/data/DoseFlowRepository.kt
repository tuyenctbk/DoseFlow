package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.os.Environment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DoseFlowRepository(private val dao: DoseFlowDao, private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("doseflow_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WATER_GOAL_ML = "water_goal_ml"
        private const val DEFAULT_WATER_GOAL_ML = 2000
        private const val KEY_REMINDER_INTERVAL = "reminder_interval_hours"
        private const val KEY_SNOOZE_MINUTES = "reminder_snooze_minutes"
        private const val KEY_SEED_DONE = "seed_data_done"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_BIOMETRIC_LOCKED = "biometric_locked"
    }

    fun isBiometricLocked(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_LOCKED, false)
    }

    fun setBiometricLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCKED, locked).apply()
    }

    fun getReminderIntervalHours(): Int {
        return prefs.getInt(KEY_REMINDER_INTERVAL, 4)
    }

    fun setReminderIntervalHours(hours: Int) {
        prefs.edit().putInt(KEY_REMINDER_INTERVAL, hours).apply()
    }

    fun getSnoozeMinutes(): Int {
        return prefs.getInt(KEY_SNOOZE_MINUTES, 15)
    }

    fun setSnoozeMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_SNOOZE_MINUTES, minutes).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isDarkThemeEnabled(): Boolean {
        return prefs.getBoolean("dark_theme_enabled", true)
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("dark_theme_enabled", enabled).apply()
    }

    val allMedications: Flow<List<MedicationEntity>> = dao.getAllMedications()
    val activeMedications: Flow<List<MedicationEntity>> = dao.getActiveMedications()
    val allMedLogs: Flow<List<MedicationLogEntity>> = dao.getAllMedLogs()
    val allWaterLogs: Flow<List<WaterLogEntity>> = dao.getAllWaterLogs()

    fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    val currentDateFlow: Flow<String> = flow {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        while (true) {
            emit(sdf.format(Date()))
            delay(10_000) // Re-check date every 10 seconds for instant midnight rollover
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMedLogsForToday(): Flow<List<MedicationLogEntity>> {
        return currentDateFlow.flatMapLatest { date ->
            dao.getMedLogsForDate(date)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWaterLogsForToday(): Flow<List<WaterLogEntity>> {
        return currentDateFlow.flatMapLatest { date ->
            dao.getWaterLogsForDate(date)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWaterSumForToday(): Flow<Int?> {
        return currentDateFlow.flatMapLatest { date ->
            dao.getWaterSumForDate(date)
        }
    }

    fun getWaterGoalMl(): Int {
        return prefs.getInt(KEY_WATER_GOAL_ML, DEFAULT_WATER_GOAL_ML)
    }

    fun setWaterGoalMl(goalMl: Int) {
        prefs.edit().putInt(KEY_WATER_GOAL_ML, goalMl).apply()
    }

    suspend fun insertMedication(medication: MedicationEntity): Long {
        return dao.insertMedication(medication)
    }

    suspend fun updateMedication(medication: MedicationEntity) {
        dao.updateMedication(medication)
    }

    suspend fun deleteMedication(medication: MedicationEntity) {
        dao.deleteMedication(medication)
    }

    suspend fun logMedicationAction(
        medicationId: Long,
        medicationName: String,
        dosage: String,
        status: String
    ) {
        val today = getTodayString()
        val log = MedicationLogEntity(
            medicationId = medicationId,
            medicationName = medicationName,
            dosage = dosage,
            status = status,
            timestamp = System.currentTimeMillis(),
            dateString = today
        )
        dao.insertMedLog(log)
        if (medicationId > 0) {
            val now = System.currentTimeMillis()
            dao.updateLastLoggedTime(medicationId, now)
            if (status == "TAKEN") {
                dao.decrementStock(medicationId)
            }
        }
    }

    suspend fun logWater(amountMl: Int) {
        val today = getTodayString()
        val log = WaterLogEntity(
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            dateString = today
        )
        dao.insertWaterLog(log)
    }

    suspend fun undoLastWaterLog() {
        dao.undoLastWaterLog(getTodayString())
    }

    suspend fun undoLastMedLog() {
        dao.undoLastMedLog(getTodayString())
    }

    suspend fun deleteMedLog(log: MedicationLogEntity) {
        dao.deleteMedLog(log)
    }

    suspend fun deleteWaterLog(log: WaterLogEntity) {
        dao.deleteWaterLog(log)
    }

    suspend fun getLastWaterLogTime(): Long {
        return dao.getLatestWaterLogTimestamp() ?: System.currentTimeMillis()
    }

    suspend fun insertMedLog(log: MedicationLogEntity) {
        dao.insertMedLog(log)
    }

    suspend fun insertWaterLog(log: WaterLogEntity) {
        dao.insertWaterLog(log)
    }

    suspend fun updateMedLog(log: MedicationLogEntity) {
        dao.insertMedLog(log)
    }

    suspend fun updateWaterLog(log: WaterLogEntity) {
        dao.insertWaterLog(log)
    }

    suspend fun seedSampleDataIfEmpty() {
        if (!prefs.getBoolean(KEY_SEED_DONE, false)) {
            val existing = dao.getAllMedications().first()
            if (existing.isEmpty()) {
                dao.insertMedication(
                    MedicationEntity(
                        name = "Multivitamin",
                        dosage = "1 Tablet",
                        timeHour = 8,
                        timeMinute = 0,
                        stockRemaining = 28,
                        colorHex = "#8B5CF6"
                    )
                )
                dao.insertMedication(
                    MedicationEntity(
                        name = "Omega-3 Fish Oil",
                        dosage = "2 Softgels",
                        timeHour = 12,
                        timeMinute = 30,
                        stockRemaining = 45,
                        colorHex = "#3B82F6"
                    )
                )
                dao.insertMedication(
                    MedicationEntity(
                        name = "Magnesium Glycinate",
                        dosage = "1 Capsule",
                        timeHour = 21,
                        timeMinute = 0,
                        stockRemaining = 14,
                        colorHex = "#10B981"
                    )
                )
                // Add initial water log
                dao.insertWaterLog(
                    WaterLogEntity(
                        amountMl = 500,
                        timestamp = System.currentTimeMillis() - 7200000,
                        dateString = getTodayString()
                    )
                )
            }
            prefs.edit().putBoolean(KEY_SEED_DONE, true).apply()
        }
    }

    suspend fun generateCsvExport(): String {
        val sb = StringBuilder()
        sb.append("Type,Name/Amount,Dosage,Status,Date,Time,Timestamp\n")

        val medLogs = dao.getAllMedLogs().first()
        val waterLogs = dao.getAllWaterLogs().first()

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        medLogs.forEach { log ->
            val timeStr = timeFormat.format(Date(log.timestamp))
            sb.append("Medication,\"${log.medicationName.replace("\"", "\"\"")}\",\"${log.dosage}\",${log.status},${log.dateString},$timeStr,${log.timestamp}\n")
        }

        waterLogs.forEach { log ->
            val timeStr = timeFormat.format(Date(log.timestamp))
            sb.append("Water,${log.amountMl} ml,-,LOGGED,${log.dateString},$timeStr,${log.timestamp}\n")
        }

        return sb.toString()
    }

    suspend fun backupDatabaseToFile(): String = withContext(Dispatchers.IO) {
        val meds = dao.getAllMedications().first()
        val medLogs = dao.getAllMedLogs().first()
        val waterLogs = dao.getAllWaterLogs().first()

        val root = JSONObject()
        val medsArray = JSONArray()
        meds.forEach { m ->
            medsArray.put(
                JSONObject().apply {
                    put("id", m.id)
                    put("name", m.name)
                    put("dosage", m.dosage)
                    put("timeHour", m.timeHour)
                    put("timeMinute", m.timeMinute)
                    put("frequency", m.frequency)
                    put("stockRemaining", m.stockRemaining)
                    put("colorHex", m.colorHex)
                    put("iconType", m.iconType)
                    put("isActive", m.isActive)
                    put("lastLoggedTime", m.lastLoggedTime)
                }
            )
        }

        val medLogsArray = JSONArray()
        medLogs.forEach { l ->
            medLogsArray.put(
                JSONObject().apply {
                    put("id", l.id)
                    put("medicationId", l.medicationId)
                    put("medicationName", l.medicationName)
                    put("dosage", l.dosage)
                    put("status", l.status)
                    put("timestamp", l.timestamp)
                    put("dateString", l.dateString)
                }
            )
        }

        val waterLogsArray = JSONArray()
        waterLogs.forEach { w ->
            waterLogsArray.put(
                JSONObject().apply {
                    put("id", w.id)
                    put("amountMl", w.amountMl)
                    put("timestamp", w.timestamp)
                    put("dateString", w.dateString)
                }
            )
        }

        root.put("medications", medsArray)
        root.put("medicationLogs", medLogsArray)
        root.put("waterLogs", waterLogsArray)

        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: File(context.filesDir, "documents")
        if (!documentsDir.exists()) documentsDir.mkdirs()
        val file = File(documentsDir, "DoseFlow_Backup_${System.currentTimeMillis()}.json")
        FileOutputStream(file).use { it.write(root.toString(2).toByteArray()) }
        file.absolutePath
    }

    suspend fun restoreDatabaseFromJsonString(jsonString: String) = withContext(Dispatchers.IO) {
        val root = JSONObject(jsonString)
        val medsArray = root.optJSONArray("medications") ?: JSONArray()
        for (i in 0 until medsArray.length()) {
            val obj = medsArray.getJSONObject(i)
            val m = MedicationEntity(
                id = obj.optLong("id", 0),
                name = obj.getString("name"),
                dosage = obj.getString("dosage"),
                timeHour = obj.getInt("timeHour"),
                timeMinute = obj.getInt("timeMinute"),
                frequency = obj.optString("frequency", "Daily"),
                stockRemaining = obj.optInt("stockRemaining", 30),
                colorHex = obj.optString("colorHex", "#8B5CF6"),
                iconType = obj.optString("iconType", "pill"),
                isActive = obj.optBoolean("isActive", true),
                lastLoggedTime = obj.optLong("lastLoggedTime", 0)
            )
            dao.insertMedication(m)
        }

        val medLogsArray = root.optJSONArray("medicationLogs") ?: JSONArray()
        for (i in 0 until medLogsArray.length()) {
            val obj = medLogsArray.getJSONObject(i)
            val l = MedicationLogEntity(
                id = obj.optLong("id", 0),
                medicationId = obj.optLong("medicationId", 0),
                medicationName = obj.getString("medicationName"),
                dosage = obj.getString("dosage"),
                status = obj.getString("status"),
                timestamp = obj.getLong("timestamp"),
                dateString = obj.getString("dateString")
            )
            dao.insertMedLog(l)
        }

        val waterLogsArray = root.optJSONArray("waterLogs") ?: JSONArray()
        for (i in 0 until waterLogsArray.length()) {
            val obj = waterLogsArray.getJSONObject(i)
            val w = WaterLogEntity(
                id = obj.optLong("id", 0),
                amountMl = obj.getInt("amountMl"),
                timestamp = obj.getLong("timestamp"),
                dateString = obj.getString("dateString")
            )
            dao.insertWaterLog(w)
        }
    }
}
