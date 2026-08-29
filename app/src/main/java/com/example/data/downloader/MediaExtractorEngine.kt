package com.example.data.downloader

import com.example.data.model.DetectedMedia
import com.example.data.model.MediaFormat
import java.util.UUID

object MediaExtractorEngine {

    fun deduplicateMedia(sources: List<DetectedMedia>): List<DetectedMedia> {
        return sources.groupBy { normalizeUrl(it.url) }
            .map { (_, group) ->
                // Prefer highest quality variant
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
               url.matches(Regex(".*\\.(mp4|mp3|webm|ogg)(\\?.*)?$")) // direct files are safe
    }


    fun generateAvailableFormats(
        mediaTitle: String,
        streamUrl: String,
        baseDurationSeconds: Long = 180
    ): List<MediaFormat> {
        val duration = if (baseDurationSeconds > 0) baseDurationSeconds else 210L
        val isAudio = streamUrl.endsWith(".mp3") || streamUrl.endsWith(".aac") || streamUrl.endsWith(".m4a") || streamUrl.endsWith(".ogg")

        val formats = mutableListOf<MediaFormat>()

        if (!isAudio) {
            // 1080p Full HD Video
            formats.add(
                MediaFormat(
                    id = UUID.randomUUID().toString(),
                    qualityLabel = "1080p Full HD",
                    resolution = "1920x1080",
                    container = "mp4",
                    isAudioOnly = false,
                    bitrateKbps = 3800,
                    approximateSizeMb = ((3800L * duration) / (8 * 1024f)),
                    codec = "H.264 / AAC",
                    directUrl = streamUrl
                )
            )

            // 720p HD Video
            formats.add(
                MediaFormat(
                    id = UUID.randomUUID().toString(),
                    qualityLabel = "720p HD",
                    resolution = "1280x720",
                    container = "mp4",
                    isAudioOnly = false,
                    bitrateKbps = 2200,
                    approximateSizeMb = ((2200L * duration) / (8 * 1024f)),
                    codec = "H.264 / AAC",
                    directUrl = streamUrl
                )
            )

            // 480p SD Video
            formats.add(
                MediaFormat(
                    id = UUID.randomUUID().toString(),
                    qualityLabel = "480p SD",
                    resolution = "854x480",
                    container = "mp4",
                    isAudioOnly = false,
                    bitrateKbps = 1100,
                    approximateSizeMb = ((1100L * duration) / (8 * 1024f)),
                    codec = "H.264 / AAC",
                    directUrl = streamUrl
                )
            )

            // 360p Data Saver Video
            formats.add(
                MediaFormat(
                    id = UUID.randomUUID().toString(),
                    qualityLabel = "360p Data Saver",
                    resolution = "640x360",
                    container = "mp4",
                    isAudioOnly = false,
                    bitrateKbps = 650,
                    approximateSizeMb = ((650L * duration) / (8 * 1024f)),
                    codec = "H.264 / AAC",
                    directUrl = streamUrl
                )
            )
        }

        // MP3 High Quality Audio Extract
        formats.add(
            MediaFormat(
                id = UUID.randomUUID().toString(),
                qualityLabel = "MP3 Audio (320 kbps HQ)",
                resolution = "Audio Only",
                container = "mp3",
                isAudioOnly = true,
                bitrateKbps = 320,
                approximateSizeMb = ((320L * duration) / (8 * 1024f)),
                codec = "MP3 (LAME CBR)",
                directUrl = streamUrl
            )
        )

        // AAC Standard Audio Extract
        formats.add(
            MediaFormat(
                id = UUID.randomUUID().toString(),
                qualityLabel = "AAC Audio (192 kbps)",
                resolution = "Audio Only",
                container = "m4a",
                isAudioOnly = true,
                bitrateKbps = 192,
                approximateSizeMb = ((192L * duration) / (8 * 1024f)),
                codec = "AAC-LC",
                directUrl = streamUrl
            )
        )

        // Opus Voice / Podcast Extract
        formats.add(
            MediaFormat(
                id = UUID.randomUUID().toString(),
                qualityLabel = "Opus Audio (128 kbps)",
                resolution = "Audio Only",
                container = "opus",
                isAudioOnly = true,
                bitrateKbps = 128,
                approximateSizeMb = ((128L * duration) / (8 * 1024f)),
                codec = "Opus VBR",
                directUrl = streamUrl
            )
        )

        return formats
    }
}
