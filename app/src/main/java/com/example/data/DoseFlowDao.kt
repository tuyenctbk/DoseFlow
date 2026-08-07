package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseFlowDao {

    // --- Medications ---
    @Query("SELECT * FROM medications ORDER BY timeHour ASC, timeMinute ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY timeHour ASC, timeMinute ASC")
    fun getActiveMedications(): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET stockRemaining = MAX(0, stockRemaining - 1) WHERE id = :medId")
    suspend fun decrementStock(medId: Long)

    @Query("UPDATE medications SET lastLoggedTime = :timestamp WHERE id = :medId")
    suspend fun updateLastLoggedTime(medId: Long, timestamp: Long)

    // --- Medication Logs ---
    @Query("SELECT * FROM medication_logs WHERE dateString = :date ORDER BY timestamp DESC")
    fun getMedLogsForDate(date: String): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs ORDER BY timestamp DESC")
    fun getAllMedLogs(): Flow<List<MedicationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedLog(log: MedicationLogEntity): Long

    @Delete
    suspend fun deleteMedLog(log: MedicationLogEntity)

    @Query("DELETE FROM medication_logs WHERE id = (SELECT id FROM medication_logs WHERE dateString = :date ORDER BY timestamp DESC LIMIT 1)")
    suspend fun undoLastMedLog(date: String)

    // --- Water Logs ---
    @Query("SELECT * FROM water_logs WHERE dateString = :date ORDER BY timestamp DESC")
    fun getWaterLogsForDate(date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE dateString = :date")
    fun getWaterSumForDate(date: String): Flow<Int?>

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllWaterLogs(): Flow<List<WaterLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity): Long

    @Delete
    suspend fun deleteWaterLog(log: WaterLogEntity)

    @Query("DELETE FROM water_logs WHERE id = (SELECT id FROM water_logs WHERE dateString = :date ORDER BY timestamp DESC LIMIT 1)")
    suspend fun undoLastWaterLog(date: String)
}
