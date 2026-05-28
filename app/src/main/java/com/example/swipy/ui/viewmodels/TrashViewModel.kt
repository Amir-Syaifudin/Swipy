package com.example.swipy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.dao.DeletedPhotoDao
import com.example.swipy.data.model.DeletedPhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val deletedPhotoDao: DeletedPhotoDao,
) : ViewModel() {

    val trashedPhotos: StateFlow<List<DeletedPhoto>> = deletedPhotoDao.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun restore(photo: DeletedPhoto) {
        viewModelScope.launch {
            deletedPhotoDao.delete(photo)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            deletedPhotoDao.deleteAll()
        }
    }
}
