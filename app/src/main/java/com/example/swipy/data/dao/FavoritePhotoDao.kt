package com.example.swipy.data.dao

import androidx.room.*
import com.example.swipy.data.model.FavoritePhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePhotoDao {
    @Query("SELECT * FROM favorite_photos ORDER BY dateAdded DESC")
    fun getAllFlow(): Flow<List<FavoritePhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: FavoritePhoto)

    @Delete
    suspend fun delete(photo: FavoritePhoto)

    @Query("DELETE FROM favorite_photos")
    suspend fun deleteAll()
}
