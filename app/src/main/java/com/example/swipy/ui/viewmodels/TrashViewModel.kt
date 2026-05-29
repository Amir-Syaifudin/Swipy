package com.example.swipy.ui.viewmodels

import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.GalleryRepository
import com.example.swipy.data.dao.DeletedPhotoDao
import com.example.swipy.data.model.DeletedPhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val deletedPhotoDao: DeletedPhotoDao,
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    val trashedPhotos: StateFlow<List<DeletedPhoto>> = deletedPhotoDao.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _pendingActionSender = MutableStateFlow<IntentSender?>(null)
    val pendingActionSender: StateFlow<IntentSender?> = _pendingActionSender.asStateFlow()

    private var pendingUrisToDelete = emptyList<DeletedPhoto>()

    fun restore(photo: DeletedPhoto) {
        viewModelScope.launch {
            deletedPhotoDao.delete(photo)
        }
    }

    fun permanentDelete(photo: DeletedPhoto) {
        pendingUrisToDelete = listOf(photo)
        viewModelScope.launch {
            val sender = galleryRepository.deletePhotos(listOf(Uri.parse(photo.uri)))
            if (sender != null) {
                _pendingActionSender.value = sender
            } else {
                onDeleteConfirmed()
            }
        }
    }

    fun deleteAll() {
        val photos = trashedPhotos.value
        if (photos.isEmpty()) return
        pendingUrisToDelete = photos
        viewModelScope.launch {
            val uris = photos.map { Uri.parse(it.uri) }
            val sender = galleryRepository.deletePhotos(uris)
            if (sender != null) {
                _pendingActionSender.value = sender
            } else {
                onDeleteConfirmed()
            }
        }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            pendingUrisToDelete.forEach { deletedPhotoDao.delete(it) }
            pendingUrisToDelete = emptyList()
            _pendingActionSender.value = null
        }
    }

    fun onDeleteCancelled() {
        pendingUrisToDelete = emptyList()
        _pendingActionSender.value = null
    }
}
