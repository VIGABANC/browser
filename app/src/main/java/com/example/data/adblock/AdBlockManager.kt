package com.example.data.adblock

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdBlockManager {
    // Known ad, telemetry, and tracker host patterns (inspired by Brave Shields & EasyList)
    private val BLOCKED_HOST_PATTERNS = listOf(
        "doubleclick.net", "google-analytics.com", "googlesyndication.com",
        "googleadservices.com", "scorecardresearch.com", "facebook.com/tr/",
        "connect.facebook.net", "analytics.twitter.com", "criteo.com",
        "taboola.com", "outbrain.com", "adnxs.com", "quantserve.com",
        "rubiconproject.com", "pubmatic.com", "popcash.net", "popads.net",
        "adcolony.com", "applovin.com", "vungle.com", "chartbeat.com",
        "hotjar.com", "crazyegg.com", "yandex.ru/metrika", "branch.io",
        "adjust.com", "appsflyer.com", "mixpanel.com", "amplitude.com",
        "segment.io", "beacon.", "telemetry.", "/ads.js", "/pagead/",
        "/adsense/", "tracking."
    )

    private val trie = AhoCorasickTrie()
    private val bloomFilter = BloomFilter(100_000, 0.01)

    // Cosmetic filtering CSS script injected into web pages to remove ad placeholders and banners
    val COSMETIC_FILTER_JS: String = """
        (function() {
            function removeAds() {
                var selectors = [
                    'ins.adsbygoogle', 'div[id^="google_ads_"]', 'div[id*="taboola"]',
                    'div[id*="outbrain"]', 'div.ad-container', 'div.ad-wrapper',
                    'div.banner-ad', 'div.sponsor-banner', 'div.advertisement',
                    'aside.ad-slot', 'iframe[src*="doubleclick"]',
                    'iframe[src*="adservice"]', 'iframe[src*="googlesyndication"]'
                ];
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

    // JS Media Sniffer script to detect video, audio, source tags, HLS, DASH, and custom web players
    val MEDIA_SNIFFER_JS: String = """
        (function() {
            if (window._aegisSnifferInjected) return;
            window._aegisSnifferInjected = true;
            function scanMedia() {
                try {
                    var mediaList = [];
                    var pageTitle = document.title || 'Web Media';
                    var pageUrl = window.location.href;
                    var videos = document.querySelectorAll('video');
                    videos.forEach(function(v, idx) {
                        var src = v.currentSrc || v.src;
                        if (!src && v.querySelector('source')) {
                            src = v.querySelector('source').src;
                        }
                        if (src && !src.startsWith('blob:') && !src.startsWith('data:')) {
                            mediaList.push({
                                title: pageTitle + ' (Video ' + (idx + 1) + ')',
                                streamUrl: src,
                                mimeType: 'video/mp4',
                                duration: Math.round(v.duration || 0),
                                poster: v.poster || ''
                            });
                        }
                    });
                    var audios = document.querySelectorAll('audio');
                    audios.forEach(function(a, idx) {
                        var src = a.currentSrc || a.src;
                        if (!src && a.querySelector('source')) {
                            src = a.querySelector('source').src;
                        }
                        if (src && !src.startsWith('blob:')) {
                            mediaList.push({
                                title: pageTitle + ' (Audio ' + (idx + 1) + ')',
                                streamUrl: src,
                                mimeType: 'audio/mp3',
                                duration: Math.round(a.duration || 0),
                                poster: ''
                            });
                        }
                    });
                    var ogVideo = document.querySelector('meta[property="og:video"]');
                    if (ogVideo && ogVideo.content) {
                        mediaList.push({
                            title: pageTitle,
                            streamUrl: ogVideo.content,
                            mimeType: 'video/mp4',
                            duration: 0,
                            poster: ''
                        });
                    }
                    if (mediaList.length > 0 && window.AegisBridge) {
                        window.AegisBridge.onMediaDetected(JSON.stringify(mediaList));
                    }
                } catch(e) {
                    console.error('Aegis sniffer error:', e);
                }
            }
            scanMedia();
            setTimeout(scanMedia, 1000);
            setTimeout(scanMedia, 3000);
            var observer = new MutationObserver(function() {
                scanMedia();
            });
            observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
        })();
    """.trimIndent()

    fun warmup() {
        if (BLOCKED_HOST_PATTERNS.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            BLOCKED_HOST_PATTERNS.forEach { pattern ->
                trie.insert(pattern)
                bloomFilter.add(pattern)
            }
        }
    }

    fun isAdOrTracker(url: String): Boolean {
        val lowerUrl = url.lowercase()
        // Fast path: Bloom filter negative = definitely not blocked (we'll check parts)
        // Since bloom filter checks exact match and trie checks substring, we actually 
        // rely on trie contains() primarily, but let's implement the logic.
        return trie.contains(lowerUrl)
    }

    fun intercept(request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        if (url.startsWith("http://") && !url.contains("localhost")) {
            // HTTPS upgrade placeholder
        }
        if (isAdOrTracker(url)) {
            return createEmptyResponse()
        }
        val cleanedUrl = stripTrackingParams(url)
        if (cleanedUrl != url) {
            // Return empty for now on trackers if you want, or we can redirect
        }
        return null
    }

    private fun stripTrackingParams(url: String): String {
        val trackingParams = setOf(
            "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
            "fbclid", "gclid", "ttclid", "wickedid", "msclkid",
            "si", "feature", "ab_channel"
        )
        try {
            val uri = Uri.parse(url)
            if (uri.query == null) return url
            
            val builder = uri.buildUpon().clearQuery()
            var modified = false
            uri.queryParameterNames.forEach { param ->
                if (param !in trackingParams) {
                    uri.getQueryParameter(param)?.let { value ->
                        builder.appendQueryParameter(param, value)
                    }
                } else {
                    modified = true
                }
            }
            if (modified) return builder.build().toString()
        } catch(e:Exception) {}
        return url
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
