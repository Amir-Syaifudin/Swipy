package com.example.swipy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kept_photos")
data class KeptPhoto(
    @PrimaryKey val uri: String,
    val dateAdded: Long = System.currentTimeMillis()
)
