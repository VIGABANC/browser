package com.example.data.downloader

import com.example.data.model.DetectedMedia
import com.example.data.model.MediaFormat
import java.util.UUID

object MediaExtractorEngine {

    fun deduplicateMedia(sources: List<DetectedMedia>): List<DetectedMedia> {
        return sources.groupBy { normalizeUrl(it.url) }
            .map { (_, group) ->
                group.maxByOrNull { it.variants?.maxOfOrNull { v -> v.bandwidth ?: 0 } ?: 0 }
                    ?: group.first()
            }
    }

    private fun normalizeUrl(url: String): String {
        return url.split("?").first()
    }

    fun filterBySafeMode(media: List<DetectedMedia>, safeMode: Boolean): List<DetectedMedia> {
        if (!safeMode) return media
        return media.filter { isSafeSource(it.pageUrl) }
    }

    private fun isSafeSource(url: String): Boolean {
        val safeDomains = listOf(
            "archive.org", "wikimedia.org", "librivox.org",
            "creativecommons.org", "khanacademy.org",
            "ted.com", "coursera.org"
        )
        return safeDomains.any { url.contains(it) } ||
               url.matches(Regex(".*\\.(mp4|mp3|webm|ogg)(\\?.*)?$"))
    }

    /**
     * Generates truthful media formats based strictly on the detected stream URL or verified variants.
     * Never invents fake bitrates, resolutions, or non-existent transcoded variants.
     */
    fun generateAvailableFormats(
        mediaTitle: String,
        streamUrl: String,
        baseDurationSeconds: Long = 0L
    ): List<MediaFormat> {
        val cleanUrl = streamUrl.lowercase().split("?").first()
        val isAudio = cleanUrl.endsWith(".mp3") || cleanUrl.endsWith(".aac") || cleanUrl.endsWith(".m4a") || cleanUrl.endsWith(".ogg") || cleanUrl.endsWith(".wav") || cleanUrl.endsWith(".opus")

        val ext = when {
            cleanUrl.endsWith(".mp3") -> "mp3"
            cleanUrl.endsWith(".aac") -> "aac"
            cleanUrl.endsWith(".m4a") -> "m4a"
            cleanUrl.endsWith(".ogg") -> "ogg"
            cleanUrl.endsWith(".opus") -> "opus"
            cleanUrl.endsWith(".webm") -> "webm"
            cleanUrl.endsWith(".mkv") -> "mkv"
            cleanUrl.endsWith(".m3u8") -> "m3u8"
            else -> "mp4"
        }

        val formats = mutableListOf<MediaFormat>()

        if (isAudio) {
            formats.add(
                MediaFormat(
                    id = UUID.randomUUID().toString(),
                    qualityLabel = "Original Audio Stream ($ext)",
                    resolution = "Audio Only",
                    container = ext,
                    isAudioOnly = true,
                    bitrateKbps = 0,
                    approximateSizeMb = 0f,
                    codec = "Direct Stream",
                    directUrl = streamUrl
                )
            )
        } else {
            formats.add(
                MediaFormat(
                    id = UUID.randomUUID().toString(),
                    qualityLabel = "Original Video Stream ($ext)",
                    resolution = "Direct Stream",
                    container = ext,
                    isAudioOnly = false,
                    bitrateKbps = 0,
                    approximateSizeMb = 0f,
                    codec = "Direct Stream",
                    directUrl = streamUrl
                )
            )
        }

        return formats
    }
}
