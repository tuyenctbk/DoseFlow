package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosageAmount: Double = 1.0,
    val unit: String = "Pill",
    val dosage: String = "$dosageAmount $unit", // e.g. "1.0 Pill", "500mg"
    val lastLoggedTime: Long? = null, // Timestamp when last logged/taken
    val timeHour: Int = 8, // 0-23
    val timeMinute: Int = 0, // 0-59
    val frequency: String = "DAILY", // "DAILY", "WEEKDAYS", "INTERVAL"
    val stockRemaining: Int = 30,
    val colorHex: String = "#8B5CF6",
    val iconType: String = "pill", // "pill", "capsule", "syrup", "injection"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medication_logs")
data class MedicationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val dosage: String,
    val status: String, // "TAKEN", "SKIPPED", "SNOOZED"
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // "YYYY-MM-DD"
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int, // e.g. 250
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // "YYYY-MM-DD"
)
