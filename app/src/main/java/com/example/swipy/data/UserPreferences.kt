package com.example.swipy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "swipy_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val WEEKLY_NOTIF_ENABLED = booleanPreferencesKey("weekly_notif_enabled")
        val STORAGE_REMINDER_ENABLED = booleanPreferencesKey("storage_reminder_enabled")
        val SELECTED_ACCENT = intPreferencesKey("selected_accent") // 0=DustyBlue,1=SoftPink,2=SageGreen
    }

    val weeklyNotifEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[WEEKLY_NOTIF_ENABLED] ?: true }

    val storageReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[STORAGE_REMINDER_ENABLED] ?: true }

    val selectedAccent: Flow<Int> = context.dataStore.data
        .map { it[SELECTED_ACCENT] ?: 0 }

    suspend fun setWeeklyNotif(enabled: Boolean) {
        context.dataStore.edit { it[WEEKLY_NOTIF_ENABLED] = enabled }
    }

    suspend fun setStorageReminder(enabled: Boolean) {
        context.dataStore.edit { it[STORAGE_REMINDER_ENABLED] = enabled }
    }

    suspend fun setAccent(accent: Int) {
        context.dataStore.edit { it[SELECTED_ACCENT] = accent }
    }
}
