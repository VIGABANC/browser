package com.example.data.security

import java.net.URI

class SafeModeFilter {

    private val safeDomains = setOf(
        "archive.org",
        "wikimedia.org",
        "commons.wikimedia.org",
        "wikipedia.org",
        "librivox.org",
        "gutenberg.org",
        "nasa.gov",
        "loc.gov"
    )

    private val restrictedDomains = setOf(
        "instagram.com",
        "tiktok.com",
        "facebook.com",
        "twitter.com",
        "x.com"
    )

    fun isSafeSource(mediaUrl: String, pageUrl: String? = null): Boolean {
        if (mediaUrl.isBlank()) return false

        val mediaHost = extractHost(mediaUrl)
        val pageHost = pageUrl?.let { extractHost(it) }

        // Whitelisted domain
        if (isHostInList(mediaHost, safeDomains) || (pageHost != null && isHostInList(pageHost, safeDomains))) {
            return true
        }

        // Restricted domains explicitly require extended mode
        if (isHostInList(mediaHost, restrictedDomains) || (pageHost != null && isHostInList(pageHost, restrictedDomains))) {
            return false
        }

        // Public domain / creative commons direct media formats allowed in Safe Mode
        val lowerUrl = mediaUrl.lowercase()
        return lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".ogg") || lowerUrl.endsWith(".flac") ||
                lowerUrl.endsWith(".wav") || lowerUrl.contains("/pub/") || lowerUrl.contains("/open/")
    }

    fun getBlockReason(mediaUrl: String): String {
        val host = extractHost(mediaUrl)
        return if (isHostInList(host, restrictedDomains)) {
            "Downloads from $host require Extended Mode attestation under platform guidelines."
        } else {
            "Source ($host) is not on the Safe Mode public domain whitelist. Switch to Extended Mode to download."
        }
    }

    fun categorizeSource(mediaUrl: String): String {
        val host = extractHost(mediaUrl)
        return when {
            host.contains("youtube") || host.contains("youtu.be") -> "YouTube"
            host.contains("vimeo") -> "Vimeo"
            host.contains("archive.org") -> "Internet Archive"
            host.contains("wikimedia") -> "Wikimedia Commons"
            host.contains("librivox") -> "LibriVox Audiobooks"
            host.contains("soundcloud") -> "SoundCloud"
            else -> "Web Stream"
        }
    }

    private fun extractHost(url: String): String {
        return try {
            val uri = URI(url)
            uri.host?.lowercase() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun isHostInList(host: String, domainList: Set<String>): Boolean {
        if (host.isBlank()) return false
        return domainList.any { domain ->
            host == domain || host.endsWith(".$domain")
        }
    }
}
