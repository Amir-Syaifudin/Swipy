package com.example.swipy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.swipy.data.model.KeptPhoto

@Dao
interface KeptPhotoDao {
    @Query("SELECT * FROM kept_photos")
    suspend fun getAll(): List<KeptPhoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: KeptPhoto)
}
