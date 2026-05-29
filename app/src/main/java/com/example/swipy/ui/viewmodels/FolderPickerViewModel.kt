package com.example.swipy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.GalleryRepository
import com.example.swipy.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderPickerViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _buckets = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val buckets: StateFlow<List<Pair<String, Int>>> = _buckets.asStateFlow()

    private val _dontShowPicker = MutableStateFlow(false)
    val dontShowPicker = _dontShowPicker.asStateFlow()

    init {
        viewModelScope.launch {
            _dontShowPicker.value = userPreferences.dontShowModePicker.first()
        }
    }

    fun loadBuckets(mediaType: String) {
        viewModelScope.launch {
            val bucketCounts = galleryRepository.getBucketsWithCount(mediaType)
            
            // Add "All Photos/Videos" option at the top
            val allMediaCount = galleryRepository.getMedia(mediaType, null).size
            val allTitle = if (mediaType == "video") "Semua Video" else "Semua Foto"
            
            _buckets.value = listOf(Pair(allTitle, allMediaCount)) + bucketCounts
        }
    }

    fun saveModePreferences(dontShow: Boolean, defaultMode: String) {
        viewModelScope.launch {
            userPreferences.setDontShowModePicker(dontShow)
            userPreferences.setDefaultSwipeMode(defaultMode)
        }
    }

    suspend fun getDefaultMode(): String {
        return userPreferences.defaultSwipeMode.first()
    }

    suspend fun shouldSkipPicker(): Boolean {
        return userPreferences.dontShowModePicker.first()
    }
}
