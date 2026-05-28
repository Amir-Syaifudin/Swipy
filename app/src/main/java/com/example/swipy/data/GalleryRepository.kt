package com.example.swipy.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class GalleryPhoto(
    val uri: Uri,
    val name: String,
    val size: Long,
    val bucket: String,
)

@Singleton
class GalleryRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    suspend fun getPhotos(bucketName: String? = null): List<GalleryPhoto> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<GalleryPhoto>()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val selection = if (bucketName != null) "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?" else null
        val selectionArgs = bucketName?.let { arrayOf(it) }

        val cursor: Cursor? = context.contentResolver.query(
            collection, projection, selection, selectionArgs,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val bucketCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                photos.add(GalleryPhoto(
                    uri = uri,
                    name = it.getString(nameCol) ?: "Photo",
                    size = it.getLong(sizeCol),
                    bucket = it.getString(bucketCol) ?: "Unknown"
                ))
            }
        }
        photos
    }

    suspend fun getBuckets(): List<String> = withContext(Dispatchers.IO) {
        val buckets = mutableSetOf<String>()
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME),
            null, null, null
        )
        cursor?.use {
            val col = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (it.moveToNext()) {
                it.getString(col)?.let { name -> buckets.add(name) }
            }
        }
        buckets.sorted()
    }
}
