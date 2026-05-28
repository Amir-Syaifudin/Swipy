package com.example.swipy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.swipy.data.dao.DeletedPhotoDao
import com.example.swipy.data.dao.FavoritePhotoDao
import com.example.swipy.data.model.DeletedPhoto
import com.example.swipy.data.model.FavoritePhoto

@Database(
    entities = [DeletedPhoto::class, FavoritePhoto::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deletedPhotoDao(): DeletedPhotoDao
    abstract fun favoritePhotoDao(): FavoritePhotoDao
}
