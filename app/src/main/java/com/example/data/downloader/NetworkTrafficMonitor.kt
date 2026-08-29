package com.example.data.downloader

import android.net.Uri
import android.webkit.WebResourceRequest
import com.example.data.model.DetectedMedia
import com.example.data.model.DetectionSource
import com.example.data.model.MediaType
import com.example.data.model.MediaVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object NetworkTrafficMonitor {
    private val MEDIA_EXTENSIONS = listOf(
        ".mp4", ".mp3", ".m4a", ".webm", ".m3u8", ".aac",
        ".ogg", ".opus", ".wav", ".flac", ".ts", ".m4s", ".mov", ".mkv", ".mpd"
    )

    private val MEDIA_URL_INDICATORS = listOf(
        "videoplayback", "audio/mp4", "video/mp4", "audio/mpeg",
        "audio/webm", "video/webm", "application/x-mpegurl",
        "application/vnd.apple.mpegurl", "/media/", "/streams/",
        "stream_url", "manifest.m3u8", "manifest.mpd"
    )

    fun isMediaUrl(url: String, headers: Map<String, String>? = null): Boolean {
        val lowerUrl = url.lowercase()
        if (lowerUrl.contains("google-analytics") || lowerUrl.contains("doubleclick") || lowerUrl.contains("pixel")) {
            return false
        }
        val hasMediaExtension = MEDIA_EXTENSIONS.any { ext ->
            val path = try { Uri.parse(url).path?.lowercase() ?: "" } catch (_: Exception) { "" }
            path.endsWith(ext) || lowerUrl.contains("$ext?")
        }
        val hasMediaIndicator = MEDIA_URL_INDICATORS.any { indicator -> lowerUrl.contains(indicator) }
        val acceptHeader = headers?.get("Accept")?.lowercase() ?: headers?.get("accept")?.lowercase() ?: ""
        val isMediaAccept = acceptHeader.contains("video/") || acceptHeader.contains("audio/")
        return hasMediaExtension || hasMediaIndicator || isMediaAccept
    }

    fun isHlsManifest(url: String): Boolean {
        return url.lowercase().contains(".m3u8") || url.lowercase().contains("application/x-mpegurl") || url.lowercase().contains("application/vnd.apple.mpegurl")
    }

    fun isDashManifest(url: String): Boolean {
        return url.lowercase().contains(".mpd")
    }

    fun inspectUrl(url: String, pageTitle: String, pageUrl: String): DetectedMedia? {
        if (!isMediaUrl(url)) return null
        val type = detectType(url)
        val lower = url.lowercase()
        val mime = when {
            lower.contains(".mp3") -> "audio/mp3"
            lower.contains(".mp4") -> "video/mp4"
            lower.contains(".m3u8") -> "application/x-mpegurl"
            else -> "video/mp4"
        }
        val formats = MediaExtractorEngine.generateAvailableFormats(pageTitle, url)

        return DetectedMedia(
            id = UUID.randomUUID().toString(),
            url = url,
            type = type,
            source = DetectionSource.NETWORK,
            title = pageTitle,
            pageUrl = pageUrl,
            mimeType = mime,
            formats = formats,
            domain = try { Uri.parse(pageUrl).host ?: "web" } catch (_: Exception) { "web" }
        )
    }

    fun detectType(url: String, headers: Map<String, String>? = null): MediaType {
        val lowerUrl = url.lowercase()
        val acceptHeader = headers?.get("Accept")?.lowercase() ?: headers?.get("accept")?.lowercase() ?: ""
        val isAudioOnly = lowerUrl.contains(".mp3") || lowerUrl.contains(".aac") ||
                lowerUrl.contains(".ogg") || lowerUrl.contains(".opus") ||
                lowerUrl.contains(".wav") || lowerUrl.contains(".flac") ||
                lowerUrl.contains("audio/") || acceptHeader.contains("audio/")

        return when {
            isHlsManifest(url) -> MediaType.HLS
            isDashManifest(url) -> MediaType.DASH
            url.startsWith("blob:") -> MediaType.BLOB
            isAudioOnly -> MediaType.AUDIO
            else -> MediaType.VIDEO
        }
    }

    suspend fun parseHlsManifest(url: String, headers: Map<String, String>, pageUrl: String, pageTitle: String): List<DetectedMedia> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
            val response = connection.inputStream.bufferedReader().readText()
            val lines = response.split("\n")

            val variants = lines.mapIndexedNotNull { index, line ->
                if (line.startsWith("#EXT-X-STREAM-INF")) {
                    val resolution = Regex("RESOLUTION=(\\d+x\\d+)").find(line)?.groupValues?.get(1)
                    val bandwidth = Regex("BANDWIDTH=(\\d+)").find(line)?.groupValues?.get(1)?.toLongOrNull()
                    val streamUrlLine = lines.getOrNull(index + 1)?.trim()
                    
                    val streamUrl = streamUrlLine?.let { 
                        if (it.startsWith("http")) it else resolveUrl(url, it) 
                    }
                    
                    streamUrl?.let {
                        MediaVariant(
                            url = it,
                            quality = resolution,
                            bandwidth = bandwidth,
                            codec = null
                        )
                    }
                } else null
            }
            
            if (variants.isNotEmpty()) {
                listOf(
                    DetectedMedia(
                        id = UUID.randomUUID().toString(),
                        url = url,
                        type = MediaType.HLS,
                        source = DetectionSource.NETWORK,
                        title = pageTitle,
                        pageUrl = pageUrl,
                        variants = variants,
                        mimeType = "application/x-mpegURL",
                        headers = headers,
                        domain = try { Uri.parse(pageUrl).host ?: "web" } catch (_: Exception) { "web" }
                    )
                )
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Very basic DASH manifest parser
    suspend fun parseDashManifest(url: String, headers: Map<String, String>, pageUrl: String, pageTitle: String): List<DetectedMedia> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
            val response = connection.inputStream.bufferedReader().readText()
            
            // Just return a top-level media item for now, parsing full DASH XML is complex
            listOf(
                DetectedMedia(
                    id = UUID.randomUUID().toString(),
                    url = url,
                    type = MediaType.DASH,
                    source = DetectionSource.NETWORK,
                    title = pageTitle,
                    pageUrl = pageUrl,
                    mimeType = "application/dash+xml",
                    headers = headers,
                    domain = try { Uri.parse(pageUrl).host ?: "web" } catch (_: Exception) { "web" }
                )
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun resolveUrl(baseUrl: String, relativeUrl: String): String {
        return try {
            val baseUri = Uri.parse(baseUrl)
            if (relativeUrl.startsWith("/")) {
                "${baseUri.scheme}://${baseUri.authority}$relativeUrl"
            } else {
                val path = baseUri.path ?: ""
                val lastSlash = path.lastIndexOf('/')
                val basePath = if (lastSlash >= 0) path.substring(0, lastSlash + 1) else "/"
                "${baseUri.scheme}://${baseUri.authority}$basePath$relativeUrl"
            }
        } catch (e: Exception) {
            relativeUrl
        }
    }
}
