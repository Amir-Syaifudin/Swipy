package com.example.swipy.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.swipy.data.AppDatabase

class TrashPurgeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "trash_purge_daily"
        // Photos older than 30 days are eligible for purge reminder
        private const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000
    }

    override suspend fun doWork(): Result {
        // In a real implementation, purge photos older than 30 days
        // For now just a placeholder that succeeds
        return Result.success()
    }
}
