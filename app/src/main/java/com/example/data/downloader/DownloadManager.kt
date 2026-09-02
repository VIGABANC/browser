package com.example.data.downloader

import android.content.Context
import android.os.Environment
import com.example.data.local.AegisDatabase
import com.example.data.local.entity.DownloadEntity
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class DownloadManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val downloadDao = AegisDatabase.getDatabase(context).downloadDao()
    private val directEngine = DirectDownloadEngine()

    // In-memory real-time state for active transfers (speed, instant progress)
    private val activeProgressFlow = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    private val activeJobs = mutableMapOf<String, Job>()

    val downloads: StateFlow<List<DownloadItem>> = combine(
        downloadDao.getAllDownloads(),
        activeProgressFlow
    ) { dbEntities, activeMap ->
        dbEntities.map { entity ->
            val baseModel = entity.toModel()
            val liveProgress = activeMap[entity.id]
            if (liveProgress != null) {
                baseModel.copy(
                    status = liveProgress.status,
                    progressPercent = if (liveProgress.progressPercent >= 0f) liveProgress.progressPercent else baseModel.progressPercent,
                    downloadedBytes = liveProgress.downloadedBytes,
                    totalBytes = if (liveProgress.totalBytes > 0) liveProgress.totalBytes else baseModel.totalBytes,
                    speedBps = liveProgress.speedBps,
                    errorMessage = liveProgress.errorMessage ?: baseModel.errorMessage
                )
            } else {
                baseModel
            }
        }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())

    fun enqueueDownload(
        title: String,
        sourcePageUrl: String,
        downloadUrl: String,
        format: MediaFormat,
        isSafeModeAttested: Boolean
    ): String {
        val id = UUID.randomUUID().toString()
        val cleanTitle = title.replace(Regex("[^a-zA-Z0-9._ -]"), "_").take(50).ifBlank { "download" }
        val ext = format.container.ifBlank { "mp4" }
        val fileName = "$cleanTitle.$ext"
        val destDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        val destFile = File(destDir, fileName)

        val item = DownloadItem(
            id = id,
            title = title.ifBlank { fileName },
            sourcePageUrl = sourcePageUrl,
            downloadUrl = downloadUrl,
            format = format,
            status = DownloadStatus.DOWNLOADING,
            progressPercent = 0f,
            downloadedBytes = 0L,
            totalBytes = (format.approximateSizeMb * 1024 * 1024).toLong().coerceAtLeast(0L),
            speedBps = 0L,
            localFilePath = destFile.absolutePath,
            startedAt = System.currentTimeMillis(),
            isSafeModeAttested = isSafeModeAttested
        )

        scope.launch(Dispatchers.IO) {
            downloadDao.insertOrUpdate(DownloadEntity.fromModel(item))
            startRealDownload(item, destFile, 0L)
        }

        return id
    }

    private fun startRealDownload(item: DownloadItem, destFile: File, startOffset: Long) {
        activeJobs[item.id]?.cancel()

        val job = scope.launch(Dispatchers.IO) {
            val targetUrl = item.format.directUrl.ifBlank { item.downloadUrl }
            val audioUrl = item.format.audioDirectUrl

            if (audioUrl.isNullOrBlank()) {
                // Single stream download
                directEngine.downloadFile(
                    url = targetUrl,
                    destinationFile = destFile,
                    startOffset = startOffset
                ).collect { progress ->
                    handleProgress(item, destFile, progress)
                }
            } else {
                // Dual stream download
                val videoDest = File(destFile.parentFile, "${destFile.name}.vid")
                val audioDest = File(destFile.parentFile, "${destFile.name}.aud")
                
                var videoDone = false
                var audioDone = false
                var lastVideoProg = DownloadProgress(DownloadStatus.DOWNLOADING, 0, 0, 0f)
                var lastAudioProg = DownloadProgress(DownloadStatus.DOWNLOADING, 0, 0, 0f)

                val videoJob = launch {
                    directEngine.downloadFile(targetUrl, videoDest, 0L).collect { p ->
                        lastVideoProg = p
                        if (p.status == DownloadStatus.COMPLETED) videoDone = true
                        emitCombinedProgress(item, lastVideoProg, lastAudioProg)
                    }
                }
                
                val audioJob = launch {
                    directEngine.downloadFile(audioUrl, audioDest, 0L).collect { p ->
                        lastAudioProg = p
                        if (p.status == DownloadStatus.COMPLETED) audioDone = true
                        emitCombinedProgress(item, lastVideoProg, lastAudioProg)
                    }
                }
                
                videoJob.join()
                audioJob.join()
                
                if (videoDone && audioDone) {
                    activeProgressFlow.value = activeProgressFlow.value + (item.id to DownloadProgress(
                        status = DownloadStatus.COMBINING_STREAMS,
                        downloadedBytes = videoDest.length() + audioDest.length(),
                        totalBytes = videoDest.length() + audioDest.length(),
                        progressPercent = 1f
                    ))
                    
                    val muxSuccess = MediaMuxerHelper.muxVideoAndAudio(videoDest, audioDest, destFile)
                    if (muxSuccess) {
                        videoDest.delete()
                        audioDest.delete()
                        
                        val updated = item.copy(
                            status = DownloadStatus.COMPLETED,
                            progressPercent = 1f,
                            downloadedBytes = destFile.length(),
                            totalBytes = destFile.length(),
                            completedAt = System.currentTimeMillis()
                        )
                        downloadDao.insertOrUpdate(DownloadEntity.fromModel(updated))
                        activeProgressFlow.value = activeProgressFlow.value - item.id
                    } else {
                        val updated = item.copy(status = DownloadStatus.FAILED, errorMessage = "Muxing failed")
                        downloadDao.insertOrUpdate(DownloadEntity.fromModel(updated))
                        activeProgressFlow.value = activeProgressFlow.value - item.id
                    }
                } else {
                    val err = lastVideoProg.errorMessage ?: lastAudioProg.errorMessage ?: "Dual download failed"
                    val updated = item.copy(status = DownloadStatus.FAILED, errorMessage = err)
                    downloadDao.insertOrUpdate(DownloadEntity.fromModel(updated))
                    activeProgressFlow.value = activeProgressFlow.value - item.id
                }
                activeJobs.remove(item.id)
            }
        }
        activeJobs[item.id] = job
    }
    
    private suspend fun emitCombinedProgress(item: DownloadItem, vp: DownloadProgress, ap: DownloadProgress) {
        val totalDled = vp.downloadedBytes + ap.downloadedBytes
        val totalTtl = (if (vp.totalBytes > 0) vp.totalBytes else 0) + (if (ap.totalBytes > 0) ap.totalBytes else 0)
        val speed = vp.speedBps + ap.speedBps
        val pct = if (totalTtl > 0) (totalDled.toFloat() / totalTtl).coerceIn(0f, 1f) else 0f
        
        activeProgressFlow.value = activeProgressFlow.value + (item.id to DownloadProgress(
            status = DownloadStatus.DOWNLOADING,
            downloadedBytes = totalDled,
            totalBytes = totalTtl,
            progressPercent = pct,
            speedBps = speed
        ))
    }

    private suspend fun handleProgress(item: DownloadItem, destFile: File, progress: DownloadProgress) {
        activeProgressFlow.value = activeProgressFlow.value + (item.id to progress)

        if (progress.status == DownloadStatus.COMPLETED) {
            val updated = item.copy(
                status = DownloadStatus.COMPLETED,
                progressPercent = 1f,
                downloadedBytes = destFile.length(),
                totalBytes = destFile.length(),
                completedAt = System.currentTimeMillis()
            )
            downloadDao.insertOrUpdate(DownloadEntity.fromModel(updated))
            activeProgressFlow.value = activeProgressFlow.value - item.id
            activeJobs.remove(item.id)
        } else if (progress.status == DownloadStatus.FAILED) {
            val updated = item.copy(
                status = DownloadStatus.FAILED,
                errorMessage = progress.errorMessage ?: "Network transfer interrupted"
            )
            downloadDao.insertOrUpdate(DownloadEntity.fromModel(updated))
            activeProgressFlow.value = activeProgressFlow.value - item.id
            activeJobs.remove(item.id)
        }
    }

    fun pauseDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        activeProgressFlow.value = activeProgressFlow.value - id

        scope.launch(Dispatchers.IO) {
            val entity = downloadDao.getDownloadById(id)
            if (entity != null && entity.status == DownloadStatus.DOWNLOADING.name) {
                downloadDao.updateDownload(entity.copy(status = DownloadStatus.PAUSED.name))
            }
        }
    }

    fun resumeDownload(id: String) {
        scope.launch(Dispatchers.IO) {
            val entity = downloadDao.getDownloadById(id) ?: return@launch
            val item = entity.toModel()
            val destFile = File(item.localFilePath)
            val partFile = File(destFile.parentFile, "${destFile.name}.part")
            val offset = if (partFile.exists()) partFile.length() else 0L

            downloadDao.updateDownload(entity.copy(status = DownloadStatus.DOWNLOADING.name, errorMessage = null))
            startRealDownload(item, destFile, offset)
        }
    }

    fun cancelDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        activeProgressFlow.value = activeProgressFlow.value - id

        scope.launch(Dispatchers.IO) {
            val entity = downloadDao.getDownloadById(id)
            if (entity != null) {
                val destFile = File(entity.localFilePath)
                val partFile = File(destFile.parentFile, "${destFile.name}.part")
                if (partFile.exists()) partFile.delete()
                downloadDao.deleteById(id)
            }
        }
    }

    fun retryDownload(id: String) {
        scope.launch(Dispatchers.IO) {
            val entity = downloadDao.getDownloadById(id) ?: return@launch
            val item = entity.toModel().copy(
                status = DownloadStatus.DOWNLOADING,
                progressPercent = 0f,
                downloadedBytes = 0L,
                errorMessage = null
            )
            val destFile = File(item.localFilePath)
            val partFile = File(destFile.parentFile, "${destFile.name}.part")
            if (partFile.exists()) partFile.delete()

            downloadDao.insertOrUpdate(DownloadEntity.fromModel(item))
            startRealDownload(item, destFile, 0L)
        }
    }

    fun clearCompleted() {
        scope.launch(Dispatchers.IO) {
            downloadDao.clearCompleted()
        }
    }
}
