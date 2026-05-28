package com.example.swipy.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TrashPurgeWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "trash_purge_daily"
    }

    override suspend fun doWork(): Result {
        // In a real implementation, purge photos older than 30 days
        // For now just a placeholder that succeeds
        return Result.success()
    }
}
