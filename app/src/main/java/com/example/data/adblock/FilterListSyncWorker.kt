package com.example.data.adblock

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class FilterListSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "aegis_filter_list_sync"
        private const val TAG = "FilterListSyncWorker"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<FilterListSyncWorker>(
                24, TimeUnit.HOURS,
                6, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            Log.d(TAG, "Scheduled periodic filter list sync worker.")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Executing background filter list sync...")
        return try {
            val success = FilterListManager.updateAllFilters(context)
            if (success) {
                Log.d(TAG, "Filter lists successfully synced in background.")
                Result.success()
            } else {
                Log.w(TAG, "Filter list sync completed with no updates.")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync filter lists in background", e)
            Result.retry()
        }
    }
}
