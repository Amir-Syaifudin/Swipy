package com.example.swipy.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class CleanReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "swipy_reminder"
        const val WORK_NAME = "clean_reminder_weekly"
    }

    override suspend fun doWork(): Result {
        showNotification(
            title = "Waktunya bersihkan galeri! 🧹",
            message = "Ada foto yang bisa kamu hapus hari ini. Swipe sekarang!"
        )
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Pengingat Bersih Galeri", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        nm.notify(1001, notif)
    }
}
