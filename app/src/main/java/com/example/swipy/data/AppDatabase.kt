package com.example.swipy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.swipy.data.dao.DeletedPhotoDao
import com.example.swipy.data.dao.FavoritePhotoDao
import com.example.swipy.data.dao.KeptPhotoDao
import com.example.swipy.data.model.DeletedPhoto
import com.example.swipy.data.model.FavoritePhoto
import com.example.swipy.data.model.KeptPhoto

@Database(
    entities = [DeletedPhoto::class, FavoritePhoto::class, KeptPhoto::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deletedPhotoDao(): DeletedPhotoDao
    abstract fun favoritePhotoDao(): FavoritePhotoDao
    abstract fun keptPhotoDao(): KeptPhotoDao
}
