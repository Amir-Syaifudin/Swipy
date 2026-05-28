package com.example.swipy.di

import android.content.Context
import androidx.room.Room
import com.example.swipy.data.AppDatabase
import com.example.swipy.data.GalleryRepository
import com.example.swipy.data.UserPreferences
import com.example.swipy.data.dao.DeletedPhotoDao
import com.example.swipy.data.dao.FavoritePhotoDao
import com.example.swipy.data.dao.KeptPhotoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "swipy_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDeletedPhotoDao(db: AppDatabase): DeletedPhotoDao = db.deletedPhotoDao()

    @Provides
    fun provideFavoritePhotoDao(db: AppDatabase): FavoritePhotoDao = db.favoritePhotoDao()

    @Provides
    fun provideKeptPhotoDao(db: AppDatabase): KeptPhotoDao = db.keptPhotoDao()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences =
        UserPreferences(context)

    @Provides
    @Singleton
    fun provideGalleryRepository(@ApplicationContext context: Context): GalleryRepository =
        GalleryRepository(context)
}
