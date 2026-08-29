package com.example.data.downloader

import android.content.Context
import android.os.Environment
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class DownloadManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _downloads = MutableStateFlow<List<DownloadItem>>(createSampleDownloads())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    private val activeJobs = mutableMapOf<String, Job>()

    fun enqueueDownload(
        title: String,
        sourcePageUrl: String,
        downloadUrl: String,
        format: MediaFormat,
        isSafeModeAttested: Boolean
    ): String {
        val id = UUID.randomUUID().toString()
        val totalBytes = (format.approximateSizeMb * 1024 * 1024).toLong().coerceAtLeast(1024 * 512)
        val cleanTitle = title.replace(Regex("[^a-zA-Z0-9._ -]"), "_").take(50)
        val fileName = "$cleanTitle.${format.container}"
        val destFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, fileName)

        val item = DownloadItem(
            id = id,
            title = title,
            sourcePageUrl = sourcePageUrl,
            downloadUrl = downloadUrl,
            format = format,
            status = DownloadStatus.DOWNLOADING,
            progressPercent = 0f,
            downloadedBytes = 0L,
            totalBytes = totalBytes,
            speedBps = 0L,
            localFilePath = destFile.absolutePath,
            startedAt = System.currentTimeMillis(),
            isSafeModeAttested = isSafeModeAttested
        )

        _downloads.update { listOf(item) + it }
        startDownloadJob(item)
        return id
    }

    private fun startDownloadJob(item: DownloadItem) {
        val job = scope.launch(Dispatchers.IO) {
            try {
                var currentBytes = item.downloadedBytes
                val total = item.totalBytes
                val targetDurationMs = (total / (1024 * 1024 * 2.5)).toLong().coerceIn(3000L, 8000L) // simulated ~2.5 MB/s
                val stepCount = 50
                val delayPerStep = targetDurationMs / stepCount
                val bytesPerStep = (total - currentBytes) / stepCount

                while (currentBytes < total) {
                    delay(delayPerStep)
                    currentBytes = (currentBytes + bytesPerStep + ((-50000..50000).random())).coerceAtMost(total)
                    val progress = (currentBytes.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    val currentSpeed = (bytesPerStep * (1000 / delayPerStep.coerceAtLeast(1))).coerceAtLeast(500_000)

                    _downloads.update { list ->
                        list.map {
                            if (it.id == item.id) {
                                it.copy(
                                    status = DownloadStatus.DOWNLOADING,
                                    progressPercent = progress,
                                    downloadedBytes = currentBytes,
                                    speedBps = currentSpeed
                                )
                            } else it
                        }
                    }
                }

                // If audio extraction is requested (e.g. MP3 / AAC), run audio conversion step
                if (item.format.isAudioOnly) {
                    _downloads.update { list ->
                        list.map {
                            if (it.id == item.id) {
                                it.copy(
                                    status = DownloadStatus.CONVERTING_AUDIO,
                                    speedBps = 0L
                                )
                            } else it
                        }
                    }
                    delay(1200) // Simulated FFmpeg audio transcoding & ID3 tagging
                }

                // Finalize to local destination file placeholder
                try {
                    val file = File(item.localFilePath)
                    file.parentFile?.mkdirs()
                    if (!file.exists()) {
                        file.writeText("Aegis Browser Downloaded Media: ${item.title}\nFormat: ${item.format.qualityLabel}\nSource: ${item.sourcePageUrl}\nTimestamp: ${System.currentTimeMillis()}")
                    }
                } catch (e: Exception) {
                    // ignore file write errors
                }

                _downloads.update { list ->
                    list.map {
                        if (it.id == item.id) {
                            it.copy(
                                status = DownloadStatus.COMPLETED,
                                progressPercent = 1f,
                                downloadedBytes = total,
                                speedBps = 0L,
                                completedAt = System.currentTimeMillis()
                            )
                        } else it
                    }
                }
            } catch (e: Exception) {
                _downloads.update { list ->
                    list.map {
                        if (it.id == item.id) {
                            it.copy(
                                status = DownloadStatus.FAILED,
                                errorMessage = e.localizedMessage ?: "Network interruption"
                            )
                        } else it
                    }
                }
            } finally {
                activeJobs.remove(item.id)
            }
        }
        activeJobs[item.id] = job
    }

    fun pauseDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        _downloads.update { list ->
            list.map {
                if (it.id == id && it.status == DownloadStatus.DOWNLOADING) {
                    it.copy(status = DownloadStatus.PAUSED, speedBps = 0L)
                } else it
            }
        }
    }

    fun resumeDownload(id: String) {
        val item = _downloads.value.firstOrNull { it.id == id } ?: return
        if (item.status == DownloadStatus.PAUSED || item.status == DownloadStatus.FAILED) {
            _downloads.update { list ->
                list.map {
                    if (it.id == id) it.copy(status = DownloadStatus.DOWNLOADING) else it
                }
            }
            startDownloadJob(item)
        }
    }

    fun cancelDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        _downloads.update { list ->
            list.filterNot { it.id == id }
        }
    }

    fun retryDownload(id: String) {
        val item = _downloads.value.firstOrNull { it.id == id } ?: return
        val resetItem = item.copy(
            status = DownloadStatus.DOWNLOADING,
            progressPercent = 0f,
            downloadedBytes = 0L,
            errorMessage = null
        )
        _downloads.update { list ->
            list.map { if (it.id == id) resetItem else it }
        }
        startDownloadJob(resetItem)
    }

    fun clearCompleted() {
        _downloads.update { list ->
            list.filterNot { it.status == DownloadStatus.COMPLETED }
        }
    }

    private fun createSampleDownloads(): List<DownloadItem> {
        return listOf(
            DownloadItem(
                id = "sample-1",
                title = "NASA James Webb Space Telescope Deep Field 4K",
                sourcePageUrl = "https://archive.org/details/nasa-jwst-deep-field",
                downloadUrl = "https://archive.org/download/nasa-jwst/jwst_1080p.mp4",
                format = MediaFormat(
                    id = "f-1",
                    qualityLabel = "1080p Full HD",
                    resolution = "1920x1080",
                    container = "mp4",
                    isAudioOnly = false,
                    bitrateKbps = 3800,
                    approximateSizeMb = 142.5f,
                    codec = "H.264 / AAC"
                ),
                status = DownloadStatus.COMPLETED,
                progressPercent = 1f,
                downloadedBytes = 142_500_000L,
                totalBytes = 142_500_000L,
                completedAt = System.currentTimeMillis() - 3600_000L
            ),
            DownloadItem(
                id = "sample-2",
                title = "LibriVox: The Art of War (Full Audiobook)",
                sourcePageUrl = "https://librivox.org/the-art-of-war-by-sun-tzu/",
                downloadUrl = "https://librivox.org/download/artofwar.mp3",
                format = MediaFormat(
                    id = "f-2",
                    qualityLabel = "MP3 Audio (320 kbps HQ)",
                    resolution = "Audio Only",
                    container = "mp3",
                    isAudioOnly = true,
                    bitrateKbps = 320,
                    approximateSizeMb = 48.2f,
                    codec = "MP3 (LAME CBR)"
                ),
                status = DownloadStatus.COMPLETED,
                progressPercent = 1f,
                downloadedBytes = 48_200_000L,
                totalBytes = 48_200_000L,
                completedAt = System.currentTimeMillis() - 7200_000L
            )
        )
    }
}
