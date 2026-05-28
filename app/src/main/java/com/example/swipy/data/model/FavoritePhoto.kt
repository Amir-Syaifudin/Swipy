package com.example.swipy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_photos")
data class FavoritePhoto(
    @PrimaryKey val uri: String,
    val name: String,
    val dateAdded: Long = System.currentTimeMillis()
)
