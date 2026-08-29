package com.example.data.downloader

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class YtdlpFormat(
    val formatId: String,
    val extension: String,
    val resolution: String,
    val qualityLabel: String,
    val filesizeBytes: Long,
    val isAudioOnly: Boolean
)

data class YtdlpMediaInfo(
    val title: String,
    val url: String,
    val durationSeconds: Long,
    val thumbnailUrl: String?,
    val formats: List<YtdlpFormat>
)

data class YtdlpProgress(
    val percent: Float,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val speed: String,
    val eta: String
)

class YtdlpWrapper(private val context: Context) {

    private val binaryFile: File by lazy {
        File(context.filesDir, "yt-dlp")
    }

    fun isAvailable(): Boolean {
        return binaryFile.exists() && binaryFile.canExecute()
    }

    private fun isValidHttpUrl(url: String): Boolean {
        val lower = url.trim().lowercase()
        return lower.startsWith("http://") || lower.startsWith("https://")
    }

    fun resolveFormats(url: String): Result<YtdlpMediaInfo> {
        if (!isAvailable()) {
            return Result.failure(IllegalStateException("yt-dlp binary is not installed in app files directory"))
        }
        if (!isValidHttpUrl(url)) {
            return Result.failure(IllegalArgumentException("Invalid URL protocol. Only HTTP and HTTPS are permitted."))
        }

        return try {
            val process = ProcessBuilder(
                binaryFile.absolutePath,
                "--dump-json",
                "--no-playlist",
                "--",
                url.trim()
            ).redirectErrorStream(true).start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val jsonBuilder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                jsonBuilder.append(line)
            }

            process.waitFor()

            val json = JSONObject(jsonBuilder.toString())
            val title = json.optString("title", "Media Download")
            val duration = json.optLong("duration", 0L)
            val thumbnail = json.optString("thumbnail").ifEmpty { null }

            val rawFormats = json.optJSONArray("formats")
            val parsedFormats = mutableListOf<YtdlpFormat>()

            if (rawFormats != null) {
                for (i in 0 until rawFormats.length()) {
                    val fmtObj = rawFormats.getJSONObject(i)
                    val fmtId = fmtObj.optString("format_id", "")
                    val ext = fmtObj.optString("ext", "mp4")
                    val height = fmtObj.optInt("height", 0)
                    val formatNote = fmtObj.optString("format_note", "")
                    val vcodec = fmtObj.optString("vcodec", "none")
                    val acodec = fmtObj.optString("acodec", "none")
                    val size = fmtObj.optLong("filesize", 0L)

                    val isAudio = vcodec == "none" && acodec != "none"
                    val label = if (isAudio) "Audio ($ext)" else if (height > 0) "${height}p" else formatNote.ifEmpty { fmtId }

                    if (fmtId.isNotEmpty()) {
                        parsedFormats.add(
                            YtdlpFormat(
                                formatId = fmtId,
                                extension = ext,
                                resolution = if (height > 0) "${height}p" else "audio",
                                qualityLabel = label,
                                filesizeBytes = size,
                                isAudioOnly = isAudio
                            )
                        )
                    }
                }
            }

            Result.success(
                YtdlpMediaInfo(
                    title = title,
                    url = url,
                    durationSeconds = duration,
                    thumbnailUrl = thumbnail,
                    formats = parsedFormats
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun download(url: String, formatId: String, outputPath: String): Flow<YtdlpProgress> = flow {
        if (!isAvailable() || !isValidHttpUrl(url)) {
            emit(YtdlpProgress(0f, 0L, 0L, "0 KB/s", "N/A"))
            return@flow
        }

        val process = ProcessBuilder(
            binaryFile.absolutePath,
            "-f", formatId,
            "-o", outputPath,
            "--newline",
            "--",
            url.trim()
        ).redirectErrorStream(true).start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?

        val progressRegex = Regex("\\[download\\]\\s+(\\d+(?:\\.\\d+)?)%\\s+of\\s+~?(\\d+(?:\\.\\d+)?[KMGT]?i?B)\\s+at\\s+(\\d+(?:\\.\\d+)?[KMGT]?i?B/s)\\s+ETA\\s+([\\d:]+)")

        while (reader.readLine().also { line = it } != null) {
            line?.let { logLine ->
                val match = progressRegex.find(logLine)
                if (match != null) {
                    val percent = match.groupValues[1].toFloatOrNull() ?: 0f
                    val speed = match.groupValues[3]
                    val eta = match.groupValues[4]

                    emit(
                        YtdlpProgress(
                            percent = percent,
                            downloadedBytes = (percent * 1000).toLong(),
                            totalBytes = 100000L,
                            speed = speed,
                            eta = eta
                        )
                    )
                }
            }
        }

        process.waitFor()
        emit(YtdlpProgress(100f, 100000L, 100000L, "Done", "00:00"))
    }.flowOn(Dispatchers.IO)

    fun extractAudio(url: String, outputPath: String, audioQuality: String = "192K"): Flow<YtdlpProgress> = flow {
        if (!isAvailable() || !isValidHttpUrl(url)) {
            emit(YtdlpProgress(0f, 0L, 0L, "0 KB/s", "N/A"))
            return@flow
        }

        val process = ProcessBuilder(
            binaryFile.absolutePath,
            "-x",
            "--audio-format", "mp3",
            "--audio-quality", audioQuality,
            "-o", outputPath,
            "--newline",
            "--",
            url.trim()
        ).redirectErrorStream(true).start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            line?.let {
                emit(YtdlpProgress(50f, 50000L, 100000L, "Extracting", "N/A"))
            }
        }

        process.waitFor()
        emit(YtdlpProgress(100f, 100000L, 100000L, "Done", "00:00"))
    }.flowOn(Dispatchers.IO)
}
