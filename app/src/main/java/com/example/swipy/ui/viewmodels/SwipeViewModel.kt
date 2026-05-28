package com.example.swipy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.GalleryPhoto
import com.example.swipy.data.GalleryRepository
import com.example.swipy.data.dao.DeletedPhotoDao
import com.example.swipy.data.dao.FavoritePhotoDao
import com.example.swipy.data.model.DeletedPhoto
import com.example.swipy.data.model.FavoritePhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SwipeActionType {
    DELETE, KEEP
}

data class SwipeAction(val photo: GalleryPhoto, val type: SwipeActionType)

@HiltViewModel
class SwipeViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val deletedPhotoDao: DeletedPhotoDao,
    private val favoritePhotoDao: FavoritePhotoDao
) : ViewModel() {

    private val _photoList = MutableStateFlow<List<GalleryPhoto>>(emptyList())
    val photoList: StateFlow<List<GalleryPhoto>> = _photoList.asStateFlow()

    private val _deletedCount = MutableStateFlow(0)
    val deletedCount: StateFlow<Int> = _deletedCount.asStateFlow()

    private val _keptCount = MutableStateFlow(0)
    val keptCount: StateFlow<Int> = _keptCount.asStateFlow()

    private val _favoritedCount = MutableStateFlow(0)
    val favoritedCount: StateFlow<Int> = _favoritedCount.asStateFlow()

    private val deleteQueue = mutableListOf<GalleryPhoto>()
    private val actionHistory = mutableListOf<SwipeAction>()

    fun loadPhotos(bucketName: String?) {
        viewModelScope.launch {
            val photos = galleryRepository.getPhotos(bucketName)
            _photoList.value = photos
        }
    }

    fun onSwipeRight(photo: GalleryPhoto) {
        deleteQueue.add(photo)
        _deletedCount.value += 1
        actionHistory.add(SwipeAction(photo, SwipeActionType.DELETE))
        removePhotoFromList(photo)
    }

    fun onSwipeLeft(photo: GalleryPhoto) {
        _keptCount.value += 1
        actionHistory.add(SwipeAction(photo, SwipeActionType.KEEP))
        removePhotoFromList(photo)
    }

    fun onDoubleTap(photo: GalleryPhoto) {
        viewModelScope.launch {
            favoritePhotoDao.insert(
                FavoritePhoto(
                    uri = photo.uri.toString(),
                    name = photo.name
                )
            )
            _favoritedCount.value += 1
        }
    }

    fun onUndo() {
        if (actionHistory.isNotEmpty()) {
            val lastAction = actionHistory.removeLast()
            when (lastAction.type) {
                SwipeActionType.DELETE -> {
                    deleteQueue.remove(lastAction.photo)
                    _deletedCount.value -= 1
                }
                SwipeActionType.KEEP -> {
                    _keptCount.value -= 1
                }
            }
            // Add back to the front of the list
            _photoList.value = listOf(lastAction.photo) + _photoList.value
        }
    }

    private fun removePhotoFromList(photo: GalleryPhoto) {
        _photoList.value = _photoList.value.filter { it.uri != photo.uri }
    }

    fun commitDeletions() {
        viewModelScope.launch {
            deleteQueue.forEach { photo ->
                deletedPhotoDao.insert(
                    DeletedPhoto(
                        uri = photo.uri.toString(),
                        name = photo.name,
                        size = photo.size
                    )
                )
            }
            deleteQueue.clear()
        }
    }
}
