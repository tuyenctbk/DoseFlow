package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.waterDataStore by preferencesDataStore(name = "water_intake_datastore")

class WaterDataStoreManager(private val context: Context) {

    companion object {
        val WATER_COUNT_KEY = intPreferencesKey("daily_water_count")
        val LAST_RESET_DATE_KEY = stringPreferencesKey("last_water_reset_date")
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    val dailyWaterCountFlow: Flow<Int> = context.waterDataStore.data.map { preferences ->
        val lastDate = preferences[LAST_RESET_DATE_KEY] ?: ""
        val today = getTodayDateString()

        if (lastDate != today) {
            // Reset count if it's a new day
            0
        } else {
            preferences[WATER_COUNT_KEY] ?: 0
        }
    }

    suspend fun addWater(amountMl: Int) {
        val today = getTodayDateString()
        context.waterDataStore.edit { preferences ->
            val lastDate = preferences[LAST_RESET_DATE_KEY] ?: ""
            val currentCount = if (lastDate == today) (preferences[WATER_COUNT_KEY] ?: 0) else 0

            val newCount = (currentCount + amountMl).coerceAtLeast(0)
            preferences[WATER_COUNT_KEY] = newCount
            preferences[LAST_RESET_DATE_KEY] = today
        }
    }

    suspend fun resetWaterCount() {
        val today = getTodayDateString()
        context.waterDataStore.edit { preferences ->
            preferences[WATER_COUNT_KEY] = 0
            preferences[LAST_RESET_DATE_KEY] = today
        }
    }

    suspend fun setWaterCount(countMl: Int) {
        val today = getTodayDateString()
        context.waterDataStore.edit { preferences ->
            preferences[WATER_COUNT_KEY] = countMl.coerceAtLeast(0)
            preferences[LAST_RESET_DATE_KEY] = today
        }
    }
}
