package com.example.data.downloader

import java.net.URI
import java.net.URL

data class HlsVariant(
    val url: String,
    val bandwidth: Long,
    val resolution: String?,
    val qualityLabel: String,
    val codecs: String?
)

data class HlsMasterPlaylist(
    val masterUrl: String,
    val variants: List<HlsVariant>,
    val isLive: Boolean
)

class HlsManifestParser {

    fun parseMasterPlaylistContent(masterUrl: String, manifestContent: String): HlsMasterPlaylist {
        val variants = mutableListOf<HlsVariant>()
        val lines = manifestContent.lines()

        var currentBandwidth = 0L
        var currentResolution: String? = null
        var currentCodecs: String? = null
        var isLive = true

        for (i in lines.indices) {
            val line = lines[i].trim()

            if (line.startsWith("#EXT-X-STREAM-INF:")) {
                currentBandwidth = parseAttributeLong(line, "BANDWIDTH")
                currentResolution = parseAttributeString(line, "RESOLUTION")
                currentCodecs = parseAttributeString(line, "CODECS")
            } else if (!line.startsWith("#") && line.isNotEmpty()) {
                val resolvedUrl = resolveUrl(masterUrl, line)
                val label = buildQualityLabel(currentResolution, currentBandwidth)

                variants.add(
                    HlsVariant(
                        url = resolvedUrl,
                        bandwidth = currentBandwidth,
                        resolution = currentResolution,
                        qualityLabel = label,
                        codecs = currentCodecs
                    )
                )

                currentBandwidth = 0L
                currentResolution = null
                currentCodecs = null
            } else if (line.contains("#EXT-X-ENDLIST")) {
                isLive = false
            }
        }

        val sortedVariants = variants.sortedByDescending { it.bandwidth }
        return HlsMasterPlaylist(
            masterUrl = masterUrl,
            variants = sortedVariants,
            isLive = isLive
        )
    }

    fun parseMasterPlaylist(masterUrl: String): HlsMasterPlaylist? {
        return try {
            val content = URL(masterUrl).readText()
            parseMasterPlaylistContent(masterUrl, content)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseAttributeLong(line: String, key: String): Long {
        val pattern = Regex("$key=(\\d+)")
        return pattern.find(line)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
    }

    private fun parseAttributeString(line: String, key: String): String? {
        val patternQuoted = Regex("$key=\"([^\"]+)\"")
        val quotedMatch = patternQuoted.find(line)?.groupValues?.get(1)
        if (quotedMatch != null) return quotedMatch

        val patternUnquoted = Regex("$key=([^,\\s]+)")
        return patternUnquoted.find(line)?.groupValues?.get(1)
    }

    private fun buildQualityLabel(resolution: String?, bandwidth: Long): String {
        if (resolution != null && resolution.contains("x")) {
            val height = resolution.split("x").getOrNull(1)
            if (height != null) return "${height}p"
        }
        return when {
            bandwidth >= 5_000_000 -> "1080p HD"
            bandwidth >= 2_500_000 -> "720p HD"
            bandwidth >= 1_000_000 -> "480p SD"
            bandwidth > 0 -> "360p"
            else -> "Auto Quality"
        }
    }

    private fun resolveUrl(baseUrl: String, relativeOrAbsolute: String): String {
        return try {
            if (relativeOrAbsolute.startsWith("http://") || relativeOrAbsolute.startsWith("https://")) {
                relativeOrAbsolute
            } else {
                val baseUri = URI(baseUrl)
                baseUri.resolve(relativeOrAbsolute).toString()
            }
        } catch (e: Exception) {
            relativeOrAbsolute
        }
    }
}
