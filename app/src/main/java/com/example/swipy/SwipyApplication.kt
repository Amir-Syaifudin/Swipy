package com.example.swipy

import android.app.Application
import androidx.work.*
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.example.swipy.worker.CleanReminderWorker
import com.example.swipy.worker.StorageCheckWorker
import com.example.swipy.worker.TrashPurgeWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.*
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class SwipyApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        scheduleWorkers()
    }

    private fun scheduleWorkers() {
        val workManager = WorkManager.getInstance(this)

        // Daily clean reminder at 10 PM
        val dailyReminderRequest = PeriodicWorkRequestBuilder<CleanReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateDelayTo10PM(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CleanReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyReminderRequest
        )

        // Daily storage check
        workManager.enqueueUniquePeriodicWork(
            StorageCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<StorageCheckWorker>(1, TimeUnit.DAYS)
                .build(),
        )

        // Daily trash purge check
        workManager.enqueueUniquePeriodicWork(
            TrashPurgeWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<TrashPurgeWorker>(1, TimeUnit.DAYS)
                .build(),
        )
    }

    private fun calculateDelayTo10PM(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 22)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis - now
    }
}
