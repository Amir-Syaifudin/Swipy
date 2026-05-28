package com.example.swipy.data.dao

import androidx.room.*
import com.example.swipy.data.model.DeletedPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedPhotoDao {
    @Query("SELECT * FROM deleted_photos ORDER BY dateAdded DESC")
    fun getAllFlow(): Flow<List<DeletedPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: DeletedPhoto)

    @Delete
    suspend fun delete(photo: DeletedPhoto)

    @Query("DELETE FROM deleted_photos")
    suspend fun deleteAll()
}
