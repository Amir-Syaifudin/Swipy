package com.example.swipy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderPickerViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
) : ViewModel() {

    private val _buckets = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val buckets: StateFlow<List<Pair<String, Int>>> = _buckets.asStateFlow()

    init {
        loadBuckets()
    }

    private fun loadBuckets() {
        viewModelScope.launch {
            val bucketNames = galleryRepository.getBuckets()
            val bucketList = bucketNames.map { name ->
                val count = galleryRepository.getPhotos(name).size
                Pair(name, count)
            }
            // Add "All Photos" option at the top
            val allPhotosCount = galleryRepository.getPhotos(null).size
            _buckets.value = listOf(Pair("Semua Foto", allPhotosCount)) + bucketList.filter { it.second > 0 }
        }
    }
}
