package com.example.swipy.ui.viewmodels

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.GalleryPhoto
import com.example.swipy.data.GalleryRepository
import com.example.swipy.data.dao.DeletedPhotoDao
import com.example.swipy.data.dao.FavoritePhotoDao
import com.example.swipy.data.dao.KeptPhotoDao
import com.example.swipy.data.model.DeletedPhoto
import com.example.swipy.data.model.FavoritePhoto
import com.example.swipy.data.model.KeptPhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Hold the photo awaiting favorite confirmation
private var currentPendingPhoto: GalleryPhoto? = null

enum class SwipeActionType { DELETE, KEEP, FAVORITE }

data class SwipeAction(val photo: GalleryPhoto, val type: SwipeActionType)

@HiltViewModel
class SwipeViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val deletedPhotoDao: DeletedPhotoDao,
    private val favoritePhotoDao: FavoritePhotoDao,
    private val keptPhotoDao: KeptPhotoDao
) : ViewModel() {

    private val _photoList = MutableStateFlow<List<GalleryPhoto>>(emptyList())
    val photoList: StateFlow<List<GalleryPhoto>> = _photoList.asStateFlow()

    private val _deletedCount = MutableStateFlow(0)
    val deletedCount: StateFlow<Int> = _deletedCount.asStateFlow()

    private val _deletedSize = MutableStateFlow(0L)
    val deletedSize: StateFlow<Long> = _deletedSize.asStateFlow()

    private val _keptCount = MutableStateFlow(0)
    val keptCount: StateFlow<Int> = _keptCount.asStateFlow()

    private val _keptSize = MutableStateFlow(0L)
    val keptSize: StateFlow<Long> = _keptSize.asStateFlow()

    private val _favoriteCount = MutableStateFlow(0)
    val favoriteCount: StateFlow<Int> = _favoriteCount.asStateFlow()

    private val _favoriteSize = MutableStateFlow(0L)
    val favoriteSize: StateFlow<Long> = _favoriteSize.asStateFlow()

    // IntentSender yang dikirim ke SwipeScreen untuk menampilkan system dialog
    private val _pendingFavoriteSender = MutableStateFlow<IntentSender?>(null)
    val pendingFavoriteSender: StateFlow<IntentSender?> = _pendingFavoriteSender.asStateFlow()

    private val deleteQueue  = mutableListOf<GalleryPhoto>()
    private val keptQueue = mutableListOf<GalleryPhoto>()
    private val actionHistory = mutableListOf<SwipeAction>()
    private var isTodaySession = false

    fun loadPhotos(mediaType: String, bucketName: String?) {
        viewModelScope.launch {
            isTodaySession = bucketName == "Hari Ini"
            val isToday = isTodaySession
            val actualBucket = if (bucketName == "Hari Ini" || bucketName == "Semua Foto" || bucketName == "Semua Video") null else bucketName
            val photos = galleryRepository.getMedia(mediaType, actualBucket, isToday)
            val keptUris = keptPhotoDao.getAll().map { it.uri }.toSet()
            val favUris = favoritePhotoDao.getAll().map { it.uri }.toSet()
            val delUris = deletedPhotoDao.getAll().map { it.uri }.toSet()

            val filteredPhotos = photos.filter { photo ->
                val uriStr = photo.uri.toString()
                uriStr !in keptUris && uriStr !in favUris && uriStr !in delUris
            }
            _photoList.value = filteredPhotos
        }
    }

    /** Swipe RIGHT → hapus */
    fun onSwipeRight(photo: GalleryPhoto) {
        deleteQueue.add(photo)
        _deletedCount.value += 1
        _deletedSize.value  += photo.size
        actionHistory.add(SwipeAction(photo, SwipeActionType.DELETE))
        
        if (isTodaySession) {
            viewModelScope.launch {
                deletedPhotoDao.insert(
                    DeletedPhoto(uri = photo.uri.toString(), name = photo.name, size = photo.size)
                )
            }
        }
        
        removePhotoFromList(photo)
    }

    fun onSwipeLeft(photo: GalleryPhoto) {
        keptQueue.add(photo)
        _keptCount.value += 1
        _keptSize.value  += photo.size
        actionHistory.add(SwipeAction(photo, SwipeActionType.KEEP))
        
        if (isTodaySession) {
            viewModelScope.launch {
                keptPhotoDao.insert(KeptPhoto(uri = photo.uri.toString()))
            }
        }
        
        removePhotoFromList(photo)
    }

    /** Double tap → favorit + simpan + lanjut */
    fun onDoubleTap(photo: GalleryPhoto) {
        currentPendingPhoto = photo
        viewModelScope.launch {
            // Request system favorite dialog; actual DB insert waits for user confirmation
            val sender = galleryRepository.toggleFavorite(listOf(photo.uri), true)
            if (sender != null) {
                _pendingFavoriteSender.value = sender
            }
        }
    }
    
    fun onFavoriteConfirmed() {
        // After user confirms system favorite, persist locally
        viewModelScope.launch {
            favoritePhotoDao.insert(FavoritePhoto(uri = currentPendingPhoto?.uri?.toString() ?: return@launch, name = currentPendingPhoto?.name ?: ""))
        }
        _favoriteCount.value += 1
        _favoriteSize.value  += currentPendingPhoto?.size ?: 0L
        _keptCount.value     += 1
        _keptSize.value      += currentPendingPhoto?.size ?: 0L
        currentPendingPhoto?.let { actionHistory.add(SwipeAction(it, SwipeActionType.FAVORITE)) }
        currentPendingPhoto?.let { removePhotoFromList(it) }
        _pendingFavoriteSender.value = null
        currentPendingPhoto = null
    }
    
    fun onFavoriteCancelled() {
        // User cancelled; do not persist
        _pendingFavoriteSender.value = null
        currentPendingPhoto = null
    }

    fun onUndo() {
        if (actionHistory.isNotEmpty()) {
            val lastAction = actionHistory.removeAt(actionHistory.lastIndex)
            when (lastAction.type) {
                SwipeActionType.DELETE -> {
                    deleteQueue.remove(lastAction.photo)
                    _deletedCount.value -= 1
                    _deletedSize.value  -= lastAction.photo.size
                    if (isTodaySession) {
                        viewModelScope.launch {
                            deletedPhotoDao.delete(DeletedPhoto(uri = lastAction.photo.uri.toString(), name = lastAction.photo.name, size = lastAction.photo.size))
                        }
                    }
                }
                SwipeActionType.KEEP -> {
                    keptQueue.remove(lastAction.photo)
                    _keptCount.value -= 1
                    _keptSize.value  -= lastAction.photo.size
                    if (isTodaySession) {
                        viewModelScope.launch {
                            keptPhotoDao.delete(KeptPhoto(uri = lastAction.photo.uri.toString()))
                        }
                    }
                }
                SwipeActionType.FAVORITE -> {
                    viewModelScope.launch {
                        favoritePhotoDao.delete(FavoritePhoto(uri = lastAction.photo.uri.toString(), name = lastAction.photo.name))
                    }
                    _favoriteCount.value -= 1
                    _favoriteSize.value  -= lastAction.photo.size
                    _keptCount.value     -= 1
                    _keptSize.value      -= lastAction.photo.size
                }
            }
            _photoList.value = listOf(lastAction.photo) + _photoList.value
        }
    }

    /**
     * Panggil saat user menekan "Selesai".
     * - Simpan metadata ke Room (local trash & kept)
     */
    fun commitDeletions() {
        viewModelScope.launch {
            // Simpan log ke Room
            deleteQueue.forEach { photo ->
                deletedPhotoDao.insert(
                    DeletedPhoto(uri = photo.uri.toString(), name = photo.name, size = photo.size)
                )
            }
            keptQueue.forEach { photo ->
                keptPhotoDao.insert(
                    KeptPhoto(uri = photo.uri.toString())
                )
            }
            keptQueue.clear()
            deleteQueue.clear()
        }
    }

    private fun removePhotoFromList(photo: GalleryPhoto) {
        _photoList.value = _photoList.value.filter { it.uri != photo.uri }
    }
}
