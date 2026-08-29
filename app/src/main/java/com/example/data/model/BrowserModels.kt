package com.example.data.model

import java.util.UUID

enum class SearchEngine(val displayName: String, val searchUrl: String, val suggestUrl: String) {
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com/ac/?q="),
    BRAVE("Brave Search", "https://search.brave.com/search?q=", "https://search.brave.com/api/suggest?q="),
    GOOGLE("Google", "https://www.google.com/search?q=", "https://suggestqueries.google.com/complete/search?client=firefox&q=")
}

enum class UserAgentMode(val displayName: String, val shortLabel: String, val description: String, val customString: String?) {
    SYSTEM_DEFAULT(
        displayName = "System Default (Mobile)",
        shortLabel = "Mobile",
        description = "Standard Android Chrome browser identity",
        customString = null
    ),
    DESKTOP_CHROME(
        displayName = "Desktop Chrome (Windows 11)",
        shortLabel = "Desktop Win",
        description = "Bypass mobile redirects with full Windows x64 Chrome",
        customString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
    ),
    DESKTOP_MAC(
        displayName = "Desktop Safari (macOS Sequoia)",
        shortLabel = "Safari Mac",
        description = "Identifies as macOS Apple Safari desktop",
        customString = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_6_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15"
    ),
    IPAD_TABLET(
        displayName = "iPad Pro (Tablet iOS)",
        shortLabel = "iPad",
        description = "Identifies as Apple iPadOS tablet browser",
        customString = "Mozilla/5.0 (iPad; CPU OS 17_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1"
    ),
    FIREFOX_LINUX(
        displayName = "Firefox (Linux x86_64)",
        shortLabel = "Firefox",
        description = "Identifies as Mozilla Firefox desktop on Linux",
        customString = "Mozilla/5.0 (X11; Linux x86_64; rv:129.0) Gecko/20100101 Firefox/129.0"
    )
}

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = "about:blank",
    val isIncognito: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isDesktopMode: Boolean = false,
    val userAgentMode: UserAgentMode = UserAgentMode.SYSTEM_DEFAULT,
    val detectedMediaList: List<DetectedMedia> = emptyList(),
    val sslSecure: Boolean = true,
    val blockedTrackersCount: Int = 0,
    val blockedAdsCount: Int = 0,
    val lastVisitedTimestamp: Long = System.currentTimeMillis()
)

data class DetectedMedia(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val type: MediaType,
    val source: DetectionSource,
    val title: String? = null,
    val pageUrl: String,
    val thumbnailUrl: String? = null,
    val duration: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val sizeBytes: Long? = null,
    val mimeType: String? = null,
    val headers: Map<String, String>? = null,
    val variants: List<MediaVariant>? = null,
    val isSelected: Boolean = false,
    val formats: List<MediaFormat> = emptyList(), // Retaining this for compatibility if used elsewhere
    val domain: String = "",
    val detectedAt: Long = System.currentTimeMillis()
)

data class MediaVariant(
    val url: String,
    val quality: String?,
    val bandwidth: Long?,
    val codec: String?
)

enum class MediaType { VIDEO, AUDIO, HLS, DASH, BLOB, MEDIASOURCE }
enum class DetectionSource { DOM, NETWORK, MANIFEST, IFRAME, MEDIASOURCE_HOOK }

data class MediaFormat(
    val id: String,
    val qualityLabel: String,
    val resolution: String,
    val container: String,
    val isAudioOnly: Boolean,
    val bitrateKbps: Int,
    val approximateSizeMb: Float,
    val codec: String,
    val directUrl: String = ""
)

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    CONVERTING_AUDIO,
    COMPLETED,
    PAUSED,
    FAILED,
    CANCELLED
}

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val sourcePageUrl: String,
    val downloadUrl: String,
    val format: MediaFormat,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progressPercent: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBps: Long = 0L,
    val localFilePath: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null,
    val isSafeModeAttested: Boolean = true
)

data class Bookmark(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val favicon: String? = null,
    val folder: String = "Main",
    val createdAt: Long = System.currentTimeMillis()
)

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSecure: Boolean = true
)

data class ShieldStats(
    val adsBlockedTotal: Int = 142,
    val trackersBlockedTotal: Int = 89,
    val httpsUpgradesTotal: Int = 34,
    val bandwidthSavedMb: Float = 18.5f,
    val fingerprintAttemptsBlocked: Int = 19,
    val isShieldEnabled: Boolean = true,
    val blockFingerprinting: Boolean = true,
    val blockThirdPartyCookies: Boolean = true,
    val blockScripts: Boolean = false
)

data class SafeModeState(
    val isSafeModeActive: Boolean = true,
    val hasUserAttested: Boolean = false,
    val attestationTimestamp: Long = 0L,
    val whitelistedDomains: Set<String> = setOf(
        "archive.org",
        "wikimedia.org",
        "wikipedia.org",
        "creativecommons.org",
        "github.com",
        "pixabay.com",
        "pexels.com",
        "unsplash.com",
        "freemusicarchive.org",
        "librivox.org"
    ),
    val userCustomAllowedDomains: Set<String> = emptySet()
)

enum class AiTaskType(val title: String, val iconDesc: String) {
    DEEP_REASONING("Deep Reasoning", "Explore complex questions with step-by-step logic"),
    PAGE_SUMMARY("Summarize Page", "Distill article into key structured insights"),
    MEDIA_ANALYSIS("Media & Video Analysis", "Analyze video takeaways, structure & topics"),
    COPYRIGHT_AUDIT("Copyright & Legal Audit", "Analyze content licensing, CC terms & fair use"),
    PRIVACY_SCAN("Privacy Threat Scan", "Audit site cookies, trackers & permissions")
}

data class AegisAiMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val thinkingTrace: String? = null,
    val isThinkingExpanded: Boolean = true,
    val taskType: AiTaskType = AiTaskType.DEEP_REASONING,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class AutoFillCredential(
    val id: Long = 0,
    val domain: String,
    val siteTitle: String = "",
    val username: String,
    val decryptedPassword: String = "",
    val isDecrypted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class ReaderArticle(
    val title: String,
    val byline: String? = null,
    val siteName: String? = null,
    val excerpt: String? = null,
    val paragraphs: List<String> = emptyList(),
    val wordCount: Int = 0,
    val estimatedReadingTimeMinutes: Int = 1,
    val leadImageUrl: String? = null,
    val sourceUrl: String = "",
    val rawText: String = ""
)

enum class ReaderTheme(val label: String, val bgHex: Long, val textHex: Long) {
    PAPER("Paper", 0xFFFAFAFA, 0xFF1C1B1F),
    SEPIA("Sepia", 0xFFFBF0D9, 0xFF43302B),
    DARK("Slate", 0xFF1E293B, 0xFFE2E8F0),
    NIGHT("OLED", 0xFF080C14, 0xFFE0E0E0)
}

data class FindInPageState(
    val isActive: Boolean = false,
    val query: String = "",
    val currentMatchIndex: Int = 0,
    val totalMatches: Int = 0
)

data class TabChip(
    val id: String,              // unique identifier
    val title: String,           // display text (page title or feature name)
    val url: String? = null,     // associated URL (for tab chips)
    val faviconUrl: String? = null,
    val isActive: Boolean = false,
    val isCloseable: Boolean = true,  // show X on individual chip?
    val type: ChipType = ChipType.TAB
)

enum class ChipType {
    TAB,           // Represents an open browser tab
    FEATURE,       // Represents a browser feature/module
    SUGGESTION,    // AI suggestion or recommendation
    FILTER         // Filter/category chip
}

data class TabStripState(
    val chips: List<TabChip> = emptyList(),
    val activeChipId: String? = null,
    val isVisible: Boolean = true,
    val showLeftArrow: Boolean = false,
    val showRightArrow: Boolean = false,
    val stripType: StripType = StripType.TABS
)

enum class StripType {
    TABS,        // Open tabs strip (like Chrome desktop tabs)
    FEATURES,    // Feature navigation (like the screenshots)
    SUGGESTIONS  // AI/Content suggestions
}

