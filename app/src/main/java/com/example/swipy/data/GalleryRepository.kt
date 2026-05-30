package com.example.swipy.data

import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.database.Cursor
import android.net.Uri
import android.os.Build
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
    val isVideo: Boolean = false
)

@Singleton
class GalleryRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    suspend fun getMedia(mediaType: String, bucketName: String? = null, isToday: Boolean = false): List<GalleryPhoto> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<GalleryPhoto>()
        val isAll = mediaType == "all"
        val isVideo = mediaType == "video"
        val collection = if (isAll) MediaStore.Files.getContentUri("external") else if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = mutableListOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
        ).apply {
            if (isAll) add(MediaStore.Files.FileColumns.MEDIA_TYPE)
        }.toTypedArray()

        var selectionStr = if (bucketName != null) {
            "${MediaStore.MediaColumns.BUCKET_DISPLAY_NAME} = ?"
        } else null
        
        val selectionArgsList = mutableListOf<String>()
        if (bucketName != null) {
            selectionArgsList.add(bucketName)
        }
        
        if (isAll) {
            val typeCondition = "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?)"
            selectionStr = if (selectionStr == null) typeCondition else "$selectionStr AND $typeCondition"
            selectionArgsList.add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
            selectionArgsList.add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
        }
        
        if (isToday) {
            val startOfDayMillis = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis / 1000 // DATE_ADDED is in seconds
            
            val dateCondition = "${MediaStore.MediaColumns.DATE_ADDED} >= ?"
            selectionStr = if (selectionStr == null) dateCondition else "$selectionStr AND $dateCondition"
            selectionArgsList.add(startOfDayMillis.toString())
        }

        val selection = selectionStr
        val selectionArgs = if (selectionArgsList.isEmpty()) null else selectionArgsList.toTypedArray()

        val cursor: Cursor? = context.contentResolver.query(
            collection, projection, selection, selectionArgs,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idCol    = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol  = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeCol  = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val bucketCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val typeCol = if (isAll) it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE) else -1

            while (it.moveToNext()) {
                val id  = it.getLong(idCol)
                val isItemVideo = if (isAll) {
                    it.getInt(typeCol) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                } else {
                    isVideo
                }
                val itemCollection = if (isItemVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val uri = ContentUris.withAppendedId(itemCollection, id)
                photos.add(
                    GalleryPhoto(
                        uri    = uri,
                        name   = it.getString(nameCol) ?: "Media",
                        size   = it.getLong(sizeCol),
                        bucket = it.getString(bucketCol) ?: "Unknown",
                        isVideo = isItemVideo
                    )
                )
            }
        }
        photos
    }

    suspend fun getBucketsWithCount(mediaType: String): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val bucketCounts = mutableMapOf<String, Int>()
        val isVideo = mediaType == "video"
        val collection = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val bucketColName = if (isVideo) MediaStore.Video.Media.BUCKET_DISPLAY_NAME else MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        
        val cursor: Cursor? = context.contentResolver.query(
            collection,
            arrayOf(bucketColName),
            null, null, null
        )
        cursor?.use {
            val col = it.getColumnIndexOrThrow(bucketColName)
            while (it.moveToNext()) {
                val name = it.getString(col) ?: "Unknown"
                bucketCounts[name] = bucketCounts.getOrDefault(name, 0) + 1
            }
        }
        bucketCounts.map { Pair(it.key, it.value) }.sortedBy { it.first }
    }

    /**
     * Hapus foto secara permanen dari MediaStore.
     *
     * Android 11+ (API 30): Mengembalikan IntentSender → SwipeScreen menampilkan
     *   system dialog konfirmasi, lalu user menekan OK → foto terhapus.
     *
     * Android < 11: Menghapus langsung via ContentResolver (mengembalikan null).
     */
    suspend fun deletePhotos(uris: List<Uri>): IntentSender? = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext null

        // Filter URIs that actually still exist in MediaStore to avoid IllegalArgumentException
        val validUris = uris.filter { uri ->
            try {
                context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns._ID), null, null, null)?.use { cursor ->
                    cursor.moveToFirst()
                } ?: false
            } catch (e: Exception) {
                false
            }
        }

        if (validUris.isEmpty()) return@withContext null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                MediaStore.createDeleteRequest(context.contentResolver, validUris).intentSender
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            // API < 30 — hapus langsung
            validUris.forEach { uri ->
                try { context.contentResolver.delete(uri, null, null) }
                catch (e: Exception) { e.printStackTrace() }
            }
            null
        }
    }

    suspend fun toggleFavorite(uris: List<Uri>, isFavorite: Boolean): IntentSender? = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStore.createFavoriteRequest(context.contentResolver, uris, isFavorite).intentSender
        } else {
            null
        }
    }

    suspend fun getDeviceFavorites(): List<GalleryPhoto> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<GalleryPhoto>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.MEDIA_TYPE
            )
            val selection = "${MediaStore.MediaColumns.IS_FAVORITE} = 1 AND (${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?)"
            val selectionArgs = arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
            
            val cursor = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection, selection, selectionArgs,
                "${MediaStore.MediaColumns.DATE_ADDED} DESC"
            )
            
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val bucketCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                val typeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                
                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val type = it.getInt(typeCol)
                    val isVideo = type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                    val collection = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val uri = ContentUris.withAppendedId(collection, id)
                    
                    photos.add(
                        GalleryPhoto(
                            uri = uri,
                            name = it.getString(nameCol) ?: "Media",
                            size = it.getLong(sizeCol),
                            bucket = it.getString(bucketCol) ?: "Unknown",
                            isVideo = isVideo
                        )
                    )
                }
            }
        }
        photos
    }
}
