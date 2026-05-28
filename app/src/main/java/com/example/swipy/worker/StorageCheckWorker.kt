package com.example.swipy.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class StorageCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "swipy_storage"
        const val WORK_NAME = "storage_check_daily"
        const val THRESHOLD_PERCENT = 80
    }

    override suspend fun doWork(): Result {
        val usedPercent = getStorageUsedPercent()
        if (usedPercent >= THRESHOLD_PERCENT) {
            showNotification(
                title = "Penyimpanan hampir penuh! 📦",
                message = "Penyimpanan kamu sudah ${usedPercent}% terpakai. Hapus foto dulu yuk!"
            )
        }
        return Result.success()
    }

    private fun getStorageUsedPercent(): Int {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val total = stat.totalBytes
        val free = stat.freeBytes
        val used = total - free
        return if (total > 0) ((used.toDouble() / total) * 100).toInt() else 0
    }

    private fun showNotification(title: String, message: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Peringatan Penyimpanan", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        nm.notify(1002, notif)
    }
}
