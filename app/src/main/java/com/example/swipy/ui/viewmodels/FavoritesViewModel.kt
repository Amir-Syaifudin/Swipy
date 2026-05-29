package com.example.swipy.ui.viewmodels

import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.GalleryRepository
import com.example.swipy.data.dao.FavoritePhotoDao
import com.example.swipy.data.model.FavoritePhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritePhotoDao: FavoritePhotoDao,
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private val _deviceFavorites = MutableStateFlow<List<FavoritePhoto>>(emptyList())
    
    private val _pendingActionSender = MutableStateFlow<IntentSender?>(null)
    val pendingActionSender: StateFlow<IntentSender?> = _pendingActionSender.asStateFlow()
    
    private var pendingPhotoToRemove: FavoritePhoto? = null

    val favorites: StateFlow<List<FavoritePhoto>> = combine(
        favoritePhotoDao.getAllFlow(),
        _deviceFavorites
    ) { localFavs, deviceFavs ->
        (localFavs + deviceFavs).distinctBy { it.uri }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadDeviceFavorites()
    }

    fun loadDeviceFavorites() {
        viewModelScope.launch {
            val devicePhotos = galleryRepository.getDeviceFavorites()
            _deviceFavorites.value = devicePhotos.map { FavoritePhoto(uri = it.uri.toString(), name = it.name) }
        }
    }

    fun remove(photo: FavoritePhoto) {
        pendingPhotoToRemove = photo
        viewModelScope.launch {
            favoritePhotoDao.delete(photo)
            val sender = galleryRepository.toggleFavorite(listOf(Uri.parse(photo.uri)), false)
            if (sender != null) {
                _pendingActionSender.value = sender
            } else {
                onRemoveConfirmed()
            }
        }
    }
    
    fun onRemoveConfirmed() {
        _pendingActionSender.value = null
        pendingPhotoToRemove = null
        loadDeviceFavorites()
    }
    
    fun onRemoveCancelled() {
        _pendingActionSender.value = null
        pendingPhotoToRemove = null
        // Re-add to local DB if they cancelled un-favoriting? 
        // We'll leave it as is, it might just resync next time.
        loadDeviceFavorites()
    }
}
