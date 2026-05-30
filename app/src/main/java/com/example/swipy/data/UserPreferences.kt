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
        val DAILY_NOTIF_ENABLED = booleanPreferencesKey("daily_notif_enabled")
        val STORAGE_REMINDER_ENABLED = booleanPreferencesKey("storage_reminder_enabled")
        val SELECTED_ACCENT = intPreferencesKey("selected_accent") // 0=DustyBlue,1=SoftPink,2=SageGreen
        val DONT_SHOW_MODE_PICKER = booleanPreferencesKey("dont_show_mode_picker")
        val DEFAULT_SWIPE_MODE = stringPreferencesKey("default_swipe_mode")
    }

    val weeklyNotifEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[WEEKLY_NOTIF_ENABLED] ?: true }

    val dailyNotifEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[DAILY_NOTIF_ENABLED] ?: true }

    val storageReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[STORAGE_REMINDER_ENABLED] ?: true }

    val selectedAccent: Flow<Int> = context.dataStore.data
        .map { it[SELECTED_ACCENT] ?: 0 }

    val dontShowModePicker: Flow<Boolean> = context.dataStore.data
        .map { it[DONT_SHOW_MODE_PICKER] ?: false }

    val defaultSwipeMode: Flow<String> = context.dataStore.data
        .map { it[DEFAULT_SWIPE_MODE] ?: "bouncy" }

    suspend fun setWeeklyNotif(enabled: Boolean) {
        context.dataStore.edit { it[WEEKLY_NOTIF_ENABLED] = enabled }
    }

    suspend fun setDailyNotif(enabled: Boolean) {
        context.dataStore.edit { it[DAILY_NOTIF_ENABLED] = enabled }
    }

    suspend fun setStorageReminder(enabled: Boolean) {
        context.dataStore.edit { it[STORAGE_REMINDER_ENABLED] = enabled }
    }

    suspend fun setAccent(accent: Int) {
        context.dataStore.edit { it[SELECTED_ACCENT] = accent }
    }

    suspend fun setDontShowModePicker(dontShow: Boolean) {
        context.dataStore.edit { it[DONT_SHOW_MODE_PICKER] = dontShow }
    }

    suspend fun setDefaultSwipeMode(mode: String) {
        context.dataStore.edit { it[DEFAULT_SWIPE_MODE] = mode }
    }
}
