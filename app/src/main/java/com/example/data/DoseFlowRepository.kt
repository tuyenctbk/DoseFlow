package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DoseFlowRepository(private val dao: DoseFlowDao, context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("doseflow_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WATER_GOAL_ML = "water_goal_ml"
        private const val DEFAULT_WATER_GOAL_ML = 2000
        private const val KEY_SEED_DONE = "seed_data_done"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    val allMedications: Flow<List<MedicationEntity>> = dao.getAllMedications()
    val activeMedications: Flow<List<MedicationEntity>> = dao.getActiveMedications()
    val allMedLogs: Flow<List<MedicationLogEntity>> = dao.getAllMedLogs()
    val allWaterLogs: Flow<List<WaterLogEntity>> = dao.getAllWaterLogs()

    fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getMedLogsForToday(): Flow<List<MedicationLogEntity>> {
        return dao.getMedLogsForDate(getTodayString())
    }

    fun getWaterLogsForToday(): Flow<List<WaterLogEntity>> {
        return dao.getWaterLogsForDate(getTodayString())
    }

    fun getWaterSumForToday(): Flow<Int?> {
        return dao.getWaterSumForDate(getTodayString())
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

    suspend fun deleteMedLog(log: MedicationLogEntity) {
        dao.deleteMedLog(log)
    }

    suspend fun deleteWaterLog(log: WaterLogEntity) {
        dao.deleteWaterLog(log)
    }

    suspend fun clearAllSampleData() {
        dao.deleteAllMedications()
        dao.deleteAllMedLogs()
        dao.deleteAllWaterLogs()
        prefs.edit().putBoolean(KEY_SEED_DONE, true).apply()
    }

    suspend fun seedSampleDataIfEmpty() {
        if (!prefs.getBoolean("sample_data_purged", false)) {
            clearAllSampleData()
            prefs.edit().putBoolean("sample_data_purged", true).apply()
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
}
