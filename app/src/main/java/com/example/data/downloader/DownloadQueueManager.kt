package com.example.data.downloader

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.*
import java.util.UUID
import java.util.concurrent.TimeUnit

class DownloadQueueManager private constructor(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        @Volatile
        private var instance: DownloadQueueManager? = null

        fun getInstance(context: Context): DownloadQueueManager {
            return instance ?: synchronized(this) {
                instance ?: DownloadQueueManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    fun enqueueDownload(url: String, outputPath: String, wifiOnly: Boolean, isAudioOnly: Boolean): String {
        val workId = "download_${UUID.randomUUID()}"

        val data = workDataOf(
            DownloadWorker.KEY_URL to url,
            DownloadWorker.KEY_OUTPUT_PATH to outputPath,
            DownloadWorker.KEY_IS_AUDIO_ONLY to isAudioOnly,
            DownloadWorker.KEY_WIFI_ONLY to wifiOnly
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .addTag("download")
            .build()

        workManager.enqueueUniqueWork(
            workId,
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        return workId
    }

    fun observeDownloadProgress(workId: String): Flow<WorkInfo?> {
        return workManager.getWorkInfosForUniqueWorkFlow(workId).map { it.firstOrNull() }
    }

    fun cancelDownload(workId: String) {
        workManager.cancelUniqueWork(workId)
    }

    fun pauseAll() {
        workManager.cancelAllWorkByTag("download")
    }
}
