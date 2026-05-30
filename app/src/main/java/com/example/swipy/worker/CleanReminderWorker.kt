package com.example.swipy.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

import com.example.swipy.data.UserPreferences
import kotlinx.coroutines.flow.first

class CleanReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "swipy_reminder"
        const val WORK_NAME = "daily_reminder"
    }

    override suspend fun doWork(): Result {
        val userPrefs = UserPreferences(applicationContext)
        val enabled = userPrefs.dailyNotifEnabled.first()
        
        if (!enabled) return Result.success()

        showNotification(
            title = "Waktunya bersihkan galeri hari ini! 🧹",
            message = "Mari kita buang foto yang tidak perlu agar device tetap lega."
        )
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Pengingat Swipy", NotificationManager.IMPORTANCE_DEFAULT)
        )

        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        nm.notify(1001, notif)
    }
}
