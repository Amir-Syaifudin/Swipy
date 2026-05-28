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
    private val userPreferences: UserPreferences
) : ViewModel() {

    val selectedAccent: StateFlow<Int> = userPreferences.selectedAccent
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val storageReminderEnabled: StateFlow<Boolean> = userPreferences.storageReminderEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val weeklyNotifEnabled: StateFlow<Boolean> = userPreferences.weeklyNotifEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

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
}
