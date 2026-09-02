package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaFormat

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val sourcePageUrl: String,
    val downloadUrl: String,
    val qualityLabel: String,
    val container: String,
    val isAudioOnly: Boolean,
    val status: String,
    val progressPercent: Float,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val localFilePath: String,
    val startedAt: Long,
    val completedAt: Long? = null,
    val errorMessage: String? = null,
    val isSafeModeAttested: Boolean = true
) {
    fun toModel(): DownloadItem {
        val parsedStatus = try {
            DownloadStatus.valueOf(status)
        } catch (_: Exception) {
            DownloadStatus.FAILED
        }
        val parts = downloadUrl.split("|##|")
        val mainUrl = parts[0]
        val audioUrl = if (parts.size > 1) parts[1] else null

        return DownloadItem(
            id = id,
            title = title,
            sourcePageUrl = sourcePageUrl,
            downloadUrl = mainUrl,
            format = MediaFormat(
                id = id,
                qualityLabel = qualityLabel,
                resolution = if (isAudioOnly) "Audio" else qualityLabel,
                container = container,
                isAudioOnly = isAudioOnly,
                bitrateKbps = 0,
                approximateSizeMb = if (totalBytes > 0) totalBytes / (1024f * 1024f) else 0f,
                codec = "Unknown",
                directUrl = mainUrl,
                audioDirectUrl = audioUrl
            ),
            status = parsedStatus,
            progressPercent = progressPercent,
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            speedBps = 0L,
            localFilePath = localFilePath,
            startedAt = startedAt,
            completedAt = completedAt,
            errorMessage = errorMessage,
            isSafeModeAttested = isSafeModeAttested
        )
    }

    companion object {
        fun fromModel(model: DownloadItem): DownloadEntity {
            val audioUrl = model.format.audioDirectUrl
            val combinedUrl = if (audioUrl != null) "${model.downloadUrl}|##|$audioUrl" else model.downloadUrl

            return DownloadEntity(
                id = model.id,
                title = model.title,
                sourcePageUrl = model.sourcePageUrl,
                downloadUrl = combinedUrl,
                qualityLabel = model.format.qualityLabel,
                container = model.format.container,
                isAudioOnly = model.format.isAudioOnly,
                status = model.status.name,
                progressPercent = model.progressPercent,
                downloadedBytes = model.downloadedBytes,
                totalBytes = model.totalBytes,
                localFilePath = model.localFilePath,
                startedAt = model.startedAt,
                completedAt = model.completedAt,
                errorMessage = model.errorMessage,
                isSafeModeAttested = model.isSafeModeAttested
            )
        }
    }
}
