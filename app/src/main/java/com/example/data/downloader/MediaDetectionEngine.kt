package com.example.data.downloader

import android.net.Uri
import com.example.data.model.DetectedMedia
import com.example.data.model.DetectionSource
import com.example.data.model.MediaFormat
import com.example.data.model.MediaType
import com.example.data.model.MediaVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

object MediaDetectionEngine {

    // Thresholds and configuration
    private const val MIN_VIDEO_DURATION_SEC = 3
    private const val AD_KEYWORD_REGEX = "(?i)(ad|advert|tracker|pixel|analytics|telemetry|beacon)"

    fun processDomMediaPayload(jsonPayload: String, pageUrl: String): DetectedMedia? {
        return try {
            val json = JSONObject(jsonPayload)
            val typeStr = json.optString("type", "VIDEO")
            val type = if (typeStr == "AUDIO") MediaType.AUDIO else MediaType.VIDEO
            
            val url = json.optString("url")
            if (url.isBlank() || url.startsWith("blob:")) return null

            val title = json.optString("title").takeIf { it.isNotBlank() }
            val duration = json.optDouble("duration", 0.0).toLong()
            val width = json.optInt("width", 0)
            val height = json.optInt("height", 0)
            val isPlaying = json.optBoolean("isPlaying", false)
            val isVisible = json.optBoolean("isVisible", false)
            val area = json.optDouble("area", 0.0)
            val poster = json.optString("poster", "")

            if (duration in 1 until MIN_VIDEO_DURATION_SEC) return null
            if (url.matches(Regex(AD_KEYWORD_REGEX))) return null

            val formats = MediaExtractorEngine.generateAvailableFormats(title ?: "Unknown", url, duration)

            DetectedMedia(
                id = UUID.randomUUID().toString(),
                url = url,
                type = type,
                source = DetectionSource.DOM,
                title = title,
                pageUrl = pageUrl,
                thumbnailUrl = poster.takeIf { it.isNotBlank() },
                duration = duration,
                width = width,
                height = height,
                mimeType = null,
                formats = formats,
                domain = try { Uri.parse(pageUrl).host ?: "web" } catch (_: Exception) { "web" },
                detectedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    fun deduplicateAndRank(mediaList: List<DetectedMedia>): List<DetectedMedia> {
        val grouped = mediaList.groupBy { normalizeUrl(it.url) }
        return grouped.map { (url, group) ->
            // Merge metadata
            val bestCandidate = group.maxByOrNull { calculateScore(it) } ?: group.first()
            val allFormats = group.flatMap { it.formats }.distinctBy { it.qualityLabel }
            val allVariants = group.flatMap { it.variants ?: emptyList() }.distinctBy { it.quality }
            
            bestCandidate.copy(
                formats = if (allFormats.isNotEmpty()) allFormats else bestCandidate.formats,
                variants = if (allVariants.isNotEmpty()) allVariants else bestCandidate.variants
            )
        }.sortedByDescending { calculateScore(it) }
    }

    private fun normalizeUrl(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.buildUpon().clearQuery().build().toString()
        } catch (e: Exception) {
            url
        }
    }

    private fun calculateScore(media: DetectedMedia): Int {
        var score = 0
        if (media.source == DetectionSource.DOM) score += 20
        if (media.source == DetectionSource.NETWORK) score += 10
        if (media.source == DetectionSource.MANIFEST) score += 30
        
        if (media.type == MediaType.HLS || media.type == MediaType.DASH) score += 40

        // Bonus for actual dimensions
        if ((media.width ?: 0) > 0 && (media.height ?: 0) > 0) score += 10
        if ((media.width ?: 0) >= 1280) score += 20
        
        if ((media.duration ?: 0) > 30) score += 15

        // Penalize likely ads
        if (media.url.contains("ad", ignoreCase = true)) score -= 30
        
        return score
    }
}
