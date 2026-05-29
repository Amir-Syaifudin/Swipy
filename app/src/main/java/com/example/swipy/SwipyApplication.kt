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

        // Weekly clean reminder — every 7 days
        val weeklyConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CleanReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<CleanReminderWorker>(7, TimeUnit.DAYS)
                .setConstraints(weeklyConstraints)
                .build(),
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
}
