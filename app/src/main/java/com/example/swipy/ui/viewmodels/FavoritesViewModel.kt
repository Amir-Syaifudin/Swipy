package com.example.swipy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.dao.FavoritePhotoDao
import com.example.swipy.data.model.FavoritePhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritePhotoDao: FavoritePhotoDao
) : ViewModel() {

    val favorites: StateFlow<List<FavoritePhoto>> = favoritePhotoDao.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun remove(photo: FavoritePhoto) {
        viewModelScope.launch {
            favoritePhotoDao.delete(photo)
        }
    }
}
