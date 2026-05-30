package com.example.swipy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val selectedAccent: StateFlow<Int> = userPreferences.selectedAccent
        .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = 0)

    val storageReminderEnabled: StateFlow<Boolean> = userPreferences.storageReminderEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = true)

    val weeklyNotifEnabled: StateFlow<Boolean> = userPreferences.weeklyNotifEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = true)

    val dailyNotifEnabled: StateFlow<Boolean> = userPreferences.dailyNotifEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = true)

    val dontShowModePicker: StateFlow<Boolean> = userPreferences.dontShowModePicker
        .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = false)

    val defaultSwipeMode: StateFlow<String> = userPreferences.defaultSwipeMode
        .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = "bouncy")

    fun setAccent(accentIndex: Int) {
        viewModelScope.launch {
            userPreferences.setAccent(accentIndex)
        }
    }

    fun setStorageReminder(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setStorageReminder(enabled)
        }
    }

    fun setWeeklyNotif(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setWeeklyNotif(enabled)
        }
    }

    fun setDailyNotif(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDailyNotif(enabled)
        }
    }

    fun setDontShowModePicker(dontShow: Boolean) {
        viewModelScope.launch {
            userPreferences.setDontShowModePicker(dontShow)
        }
    }

    fun setDefaultSwipeMode(mode: String) {
        viewModelScope.launch {
            userPreferences.setDefaultSwipeMode(mode)
        }
    }
}
