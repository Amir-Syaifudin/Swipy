package com.example.swipy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_photos")
data class DeletedPhoto(
    @PrimaryKey val uri: String,
    val filename: String,
    val deletedAt: Long,
    val sizeBytes: Long,
    val thumbnailPath: String?
)
