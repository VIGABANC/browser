package com.example.data.adblock

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BlockedResourceEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val domain: String,
    val isTracker: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

object AdBlockManager {
    // Known ad, telemetry, and tracker host patterns (inspired by Brave Shields & EasyList)
    private val BUILTIN_BLOCKED_PATTERNS = listOf(
        "doubleclick.net", "google-analytics.com", "googlesyndication.com",
        "googleadservices.com", "scorecardresearch.com", "facebook.com/tr/",
        "connect.facebook.net", "analytics.twitter.com", "criteo.com",
        "taboola.com", "outbrain.com", "adnxs.com", "quantserve.com",
        "rubiconproject.com", "pubmatic.com", "popcash.net", "popads.net",
        "adcolony.com", "applovin.com", "vungle.com", "chartbeat.com",
        "hotjar.com", "crazyegg.com", "yandex.ru/metrika", "branch.io",
        "adjust.com", "appsflyer.com", "mixpanel.com", "amplitude.com",
        "segment.io", "beacon.", "telemetry.", "/ads.js", "/pagead/",
        "/adsense/", "tracking.", "adservice.google.", "stats.wp.com",
        "matomo.", "statcounter.com", "moatads.com", "ads-twitter.com",
        "ads.tiktok.com", "analytics.tiktok.com", "adroll.com"
    )

    private val dynamicPatterns = mutableSetOf<String>()
    private val dynamicCosmeticSelectors = mutableSetOf<String>()

    private var trie = AhoCorasickTrie()
    private var bloomFilter = BloomFilter(200_000, 0.01)

    private val _recentBlockedEvents = MutableStateFlow<List<BlockedResourceEvent>>(emptyList())
    val recentBlockedEvents: StateFlow<List<BlockedResourceEvent>> = _recentBlockedEvents.asStateFlow()

    // Cosmetic filtering CSS script injected into web pages to remove ad placeholders and banners
    val COSMETIC_FILTER_JS: String
        get() {
            val baseSelectors = listOf(
                "ins.adsbygoogle", "div[id^=\"google_ads_\"]", "div[id*=\"taboola\"]",
                "div[id*=\"outbrain\"]", "div.ad-container", "div.ad-wrapper",
                "div.banner-ad", "div.sponsor-banner", "div.advertisement",
                "aside.ad-slot", "iframe[src*=\"doubleclick\"]",
                "iframe[src*=\"adservice\"]", "iframe[src*=\"googlesyndication\"]",
                ".trc_rbox_container", ".trc_related_container", "#cookie-notice",
                "#cookie-banner", ".cookie-popup", "#onetrust-consent-sdk"
            )
            val allSelectors = (baseSelectors + dynamicCosmeticSelectors.take(50))
                .joinToString("\", \"") { it.replace("\"", "\\\"") }

            return """
                (function() {
                    function removeAds() {
                        var selectors = ["$allSelectors"];
                        var elements = document.querySelectorAll(selectors.join(','));
                        elements.forEach(function(el) {
                            el.style.display = 'none';
                            el.style.visibility = 'hidden';
                            el.style.height = '0px';
                        });
                    }
                    if (document.readyState === 'loading') {
                        document.addEventListener('DOMContentLoaded', removeAds);
                    } else {
                        removeAds();
                    }
                    setInterval(removeAds, 1500);
                })();
            """.trimIndent()
        }

    const val MEDIA_SNIFFER_JS: String = """
        (function() {
            function sniffMedia() {
                try {
                    var elements = document.querySelectorAll('video, audio');
                    elements.forEach(function(el) {
                        var src = el.src || el.currentSrc;
                        if (src && typeof AegisBridge !== 'undefined') {
                            var type = el.tagName.toLowerCase() === 'audio' ? 'AUDIO' : 'VIDEO';
                            AegisBridge.onMediaFound(src, type, document.title || '');
                        }
                        var sources = el.querySelectorAll('source');
                        sources.forEach(function(s) {
                            if (s.src && typeof AegisBridge !== 'undefined') {
                                var type = el.tagName.toLowerCase() === 'audio' ? 'AUDIO' : 'VIDEO';
                                AegisBridge.onMediaFound(s.src, type, document.title || '');
                            }
                        });
                    });
                } catch(e) {}
            }
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', sniffMedia);
            } else {
                sniffMedia();
            }
        })();
    """

    fun warmup() {
        CoroutineScope(Dispatchers.IO).launch {
            val newTrie = AhoCorasickTrie()
            val newBloom = BloomFilter(200_000, 0.01)

            BUILTIN_BLOCKED_PATTERNS.forEach { pattern ->
                newTrie.insert(pattern)
                newBloom.add(pattern)
            }

            synchronized(this@AdBlockManager) {
                trie = newTrie
                bloomFilter = newBloom
            }
        }
    }

    fun loadDynamicRules(context: android.content.Context) {
        FilterListManager.initialize(context)
    }

    fun loadDynamicRules(patterns: Set<String>, cosmeticSelectors: Set<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            synchronized(this@AdBlockManager) {
                dynamicPatterns.clear()
                dynamicPatterns.addAll(patterns)

                dynamicCosmeticSelectors.clear()
                dynamicCosmeticSelectors.addAll(cosmeticSelectors)

                val newTrie = AhoCorasickTrie()
                val newBloom = BloomFilter(300_000, 0.01)

                BUILTIN_BLOCKED_PATTERNS.forEach { pattern ->
                    newTrie.insert(pattern)
                    newBloom.add(pattern)
                }

                dynamicPatterns.forEach { pattern ->
                    newTrie.insert(pattern)
                    newBloom.add(pattern)
                }

                trie = newTrie
                bloomFilter = newBloom
            }
        }
    }

    fun getTotalRuleCount(): Int {
        return BUILTIN_BLOCKED_PATTERNS.size + dynamicPatterns.size + dynamicCosmeticSelectors.size
    }

    fun isAdOrTracker(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return synchronized(this) {
            trie.contains(lowerUrl)
        }
    }

    fun recordBlockedItem(url: String) {
        val isTracker = url.contains("analytics") || url.contains("telemetry") ||
                url.contains("beacon") || url.contains("track") || url.contains("metric") ||
                url.contains("pixel") || url.contains("hotjar")
        val domain = try {
            Uri.parse(url).host ?: "unknown"
        } catch (_: Exception) {
            "unknown"
        }

        val event = BlockedResourceEvent(
            url = url,
            domain = domain,
            isTracker = isTracker
        )

        _recentBlockedEvents.value = (listOf(event) + _recentBlockedEvents.value).take(50)
    }

    fun clearSessionBlockedEvents() {
        _recentBlockedEvents.value = emptyList()
    }

    fun intercept(request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        if (isAdOrTracker(url)) {
            recordBlockedItem(url)
            return createEmptyResponse()
        }
        return null
    }

    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            200,
            "OK",
            mapOf("Access-Control-Allow-Origin" to "*"),
            ByteArrayInputStream("".toByteArray())
        )
    }
}
