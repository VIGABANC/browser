package com.example.data.downloader

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_URL = "url"
        const val KEY_WIFI_ONLY = "wifi_only"
        const val KEY_OUTPUT_PATH = "output_path"
        const val KEY_IS_AUDIO_ONLY = "is_audio_only"
        
        const val BUFFER_SIZE = 8192
        const val PROGRESS_INTERVAL_MS = 500
        const val NOTIFICATION_ID = 1001
        const val DOWNLOAD_CHANNEL_ID = "download_channel"
    }

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val outputPath = inputData.getString(KEY_OUTPUT_PATH) ?: return Result.failure()
        val isAudioOnly = inputData.getBoolean(KEY_IS_AUDIO_ONLY, false)

        val batteryLevel = getBatteryLevel()
        if (batteryLevel in 0..19) {
            return Result.retry()
        }

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)

        // Make sure the directory exists
        File(outputPath).parentFile?.mkdirs()

        // setForeground(createForegroundInfo()) - requires channel creation and permission, skipping for now to avoid crashes

        return try {
            if (url.contains("youtube") || url.contains("youtu.be")) {
                downloadWithYtdlp(url, outputPath, isAudioOnly)
            } else if (url.contains(".m3u8")) {
                downloadHls(url, outputPath)
            } else {
                downloadDirect(url, outputPath)
            }
        } catch (e: Exception) {
            if (runAttemptCount < 5) {
                Result.retry()
            } else {
                Result.failure(workDataOf("error" to e.message))
            }
        }
    }

    private suspend fun downloadDirect(url: String, outputPath: String): Result {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.apply {
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/128.0.0.0 Mobile Safari/537.36")
            setRequestProperty("Accept", "*/*")
            setRequestProperty("Accept-Encoding", "identity")
        }

        val totalBytes = connection.contentLengthLong
        val input = BufferedInputStream(connection.inputStream, BUFFER_SIZE)
        val output = FileOutputStream(outputPath)

        var downloadedBytes = 0L
        var lastProgressTime = 0L
        val buffer = ByteArray(BUFFER_SIZE)
        var bytesRead: Int

        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
            downloadedBytes += bytesRead

            val now = System.currentTimeMillis()
            if (now - lastProgressTime > PROGRESS_INTERVAL_MS) {
                val progress = if (totalBytes > 0) {
                    (downloadedBytes * 100 / totalBytes).toInt()
                } else -1

                setProgress(workDataOf(
                    "progress" to progress,
                    "downloaded" to downloadedBytes,
                    "total" to totalBytes,
                    "speed" to "Calculating..."
                ))
                lastProgressTime = now
            }
        }

        output.flush()
        output.close()
        input.close()

        val file = File(outputPath)
        if (!file.exists() || file.length() == 0L) {
            return Result.failure(workDataOf("error" to "Empty file downloaded"))
        }

        insertToMediaStore(file, url)

        return Result.success(workDataOf("output_path" to outputPath))
    }

    private fun downloadWithYtdlp(url: String, outputPath: String, isAudioOnly: Boolean): Result {
        // Placeholder for YTDLP integration
        // In a real app this would call the process builder for python/yt-dlp
        File(outputPath).writeText("Mock Yt-dlp download: $url")
        insertToMediaStore(File(outputPath), url)
        return Result.success()
    }

    private fun downloadHls(url: String, outputPath: String): Result {
        // Placeholder for HLS parsing and downloading
        File(outputPath).writeText("Mock HLS download: $url")
        insertToMediaStore(File(outputPath), url)
        return Result.success()
    }

    private fun insertToMediaStore(file: File, sourceUrl: String) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, file.name)
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Downloads.SIZE, file.length())
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Aegis")
            put(MediaStore.Downloads.IS_PENDING, 0)
        }
        try {
            applicationContext.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBatteryLevel(): Int {
        val intent = applicationContext.registerReceiver(null, 
            android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (scale == 0 || scale == -1) return -1
        return (level * 100 / scale)
    }
}
