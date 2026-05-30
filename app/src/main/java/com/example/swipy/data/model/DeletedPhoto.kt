package com.example.swipy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_photos")
data class DeletedPhoto(
    @PrimaryKey val uri: String,
    val name: String,
    val size: Long,
    val isVideo: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)
