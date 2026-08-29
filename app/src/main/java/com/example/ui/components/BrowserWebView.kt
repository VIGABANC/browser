package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import com.example.data.adblock.AdBlockManager
import com.example.data.downloader.NetworkTrafficMonitor
import com.example.data.model.BrowserTab
import com.example.data.model.DetectedMedia
import com.example.data.reader.ReaderModeExtractor
import java.io.File

class AegisJsBridge(
    private val onMediaDetectedCallback: (String) -> Unit,
    private val onReaderContentCallback: (String) -> Unit,
    private val onLoginFormDetectedCallback: (String, String, String) -> Unit
) {
    @JavascriptInterface
    fun onMediaDetected(jsonPayload: String) {
        onMediaDetectedCallback(jsonPayload)
    }

    @JavascriptInterface
    fun onReaderContentExtracted(jsonPayload: String) {
        onReaderContentCallback(jsonPayload)
    }

    @JavascriptInterface
    fun onLoginFormDetected(domain: String, username: String, pass: String) {
        onLoginFormDetectedCallback(domain, username, pass)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: BrowserTab,
    isShieldEnabled: Boolean,
    findQuery: String = "",
    findActionTrigger: Int = 0,
    findForward: Boolean = true,
    goBackTrigger: Int = 0,
    autoFillCredential: Pair<String, String>? = null,
    onPageStarted: (String) -> Unit,
    onPageFinished: (String, String?) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onNavigationStateChanged: (canGoBack: Boolean, canGoForward: Boolean) -> Unit = { _, _ -> },
    onMediaDetected: (String) -> Unit,
    onNetworkMediaDetected: (DetectedMedia) -> Unit,
    onPageTextExtracted: (String) -> Unit,
    onReaderContentExtracted: (String) -> Unit,
    onLoginFormDetected: (domain: String, user: String, pass: String) -> Unit,
    onFindMatchCounted: (activeMatchIndex: Int, totalMatches: Int) -> Unit,
    onBlockAd: () -> Unit = {},
    onBlockTracker: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onShowMediaGrabber: () -> Unit = {},
    onOpenInNewTab: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snifferScript = remember {
        try {
            context.assets.open("aegis-sniffer.js").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }
    val webView = remember(tab.id) {
        try {
            val cachePath = File(context.cacheDir, "webview_cache")
            if (!cachePath.exists()) cachePath.mkdirs()
        } catch (_: Exception) {}

        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = if (tab.isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            }

            setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                onFindMatchCounted(activeMatchOrdinal, numberOfMatches)
            }

            addJavascriptInterface(
                AegisJsBridge(
                    onMediaDetectedCallback = onMediaDetected,
                    onReaderContentCallback = onReaderContentExtracted,
                    onLoginFormDetectedCallback = onLoginFormDetected
                ),
                "AegisBridge"
            )
        }
    }

    // Handle Find In Page actions
    LaunchedEffect(findQuery) {
        if (findQuery.isNotBlank()) {
            webView.findAllAsync(findQuery)
        } else {
            webView.clearMatches()
            onFindMatchCounted(0, 0)
        }
    }

    LaunchedEffect(findActionTrigger) {
        if (findActionTrigger > 0 && findQuery.isNotBlank()) {
            webView.findNext(findForward)
        }
    }

    LaunchedEffect(goBackTrigger) {
        if (goBackTrigger > 0 && webView.canGoBack()) {
            webView.goBack()
        }
    }

    // Handle AutoFill Injection
    LaunchedEffect(autoFillCredential) {
        autoFillCredential?.let { (username, password) ->
            val safeUser = username.replace("'", "\\'")
            val safePass = password.replace("'", "\\'")
            val script = """
                (function() {
                    try {
                        var userInputs = document.querySelectorAll('input[type="text"], input[type="email"], input[name*="user"], input[name*="login"], input[id*="user"]');
                        var passInputs = document.querySelectorAll('input[type="password"]');
                        if (userInputs.length > 0) {
                            userInputs[0].value = '$safeUser';
                            userInputs[0].dispatchEvent(new Event('input', { bubbles: true }));
                            userInputs[0].dispatchEvent(new Event('change', { bubbles: true }));
                        }
                        if (passInputs.length > 0) {
                            passInputs[0].value = '$safePass';
                            passInputs[0].dispatchEvent(new Event('input', { bubbles: true }));
                            passInputs[0].dispatchEvent(new Event('change', { bubbles: true }));
                        }
                    } catch(e) {}
                })();
            """.trimIndent()
            webView.evaluateJavascript(script, null)
        }
    }

    // Set custom clients
    DisposableEffect(webView, isShieldEnabled) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if (request == null) return null
                val url = request.url?.toString() ?: return null

                // 1. Ad & Tracker Interception
                if (isShieldEnabled && AdBlockManager.isAdOrTracker(url)) {
                    if (url.contains("analytics") || url.contains("telemetry") || url.contains("beacon") || url.contains("track")) {
                        onBlockTracker()
                    } else {
                        onBlockAd()
                    }
                    return AdBlockManager.createEmptyResponse()
                }

                // 2. Network Traffic Media Sniffer for Downloader
                val pageUrl = tab.url
                val pageTitle = tab.title
                
                when {
                    NetworkTrafficMonitor.isHlsManifest(url) -> {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            val mediaItems = NetworkTrafficMonitor.parseHlsManifest(url, request.requestHeaders, pageUrl, pageTitle)
                            mediaItems.forEach { onNetworkMediaDetected(it) }
                        }
                    }
                    NetworkTrafficMonitor.isDashManifest(url) -> {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            val mediaItems = NetworkTrafficMonitor.parseDashManifest(url, request.requestHeaders, pageUrl, pageTitle)
                            mediaItems.forEach { onNetworkMediaDetected(it) }
                        }
                    }
                    NetworkTrafficMonitor.isMediaUrl(url, request.requestHeaders) -> {
                        val type = NetworkTrafficMonitor.detectType(url, request.requestHeaders)
                        onNetworkMediaDetected(
                            com.example.data.model.DetectedMedia(
                                id = java.util.UUID.randomUUID().toString(),
                                url = url,
                                type = type,
                                source = com.example.data.model.DetectionSource.NETWORK,
                                title = pageTitle,
                                pageUrl = pageUrl,
                                mimeType = request.requestHeaders["Accept"]?.split(",")?.firstOrNull(),
                                headers = request.requestHeaders,
                                domain = try { android.net.Uri.parse(pageUrl).host ?: "web" } catch (_: Exception) { "web" }
                            )
                        )
                    }
                }

                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { onPageStarted(it) }
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                onNavigationStateChanged(view?.canGoBack() == true, view?.canGoForward() == true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { onPageFinished(it, view?.title) }
                onNavigationStateChanged(view?.canGoBack() == true, view?.canGoForward() == true)

                // Inject Cosmetic Filter and DOM Media Sniffer
                if (isShieldEnabled) {
                    view?.evaluateJavascript(AdBlockManager.COSMETIC_FILTER_JS, null)
                }
                view?.evaluateJavascript(snifferScript, null)

                // Inject Reader Mode Content Extractor
                view?.evaluateJavascript(ReaderModeExtractor.READER_EXTRACTION_JS, null)

                // Extract visible page text content for Gemini AI High-Thinking summarizer
                view?.evaluateJavascript(
                    "(function(){ try { return (document.body ? document.body.innerText : '').substring(0, 5000); } catch(e){ return ''; } })()"
                ) { result ->
                    if (!result.isNullOrBlank() && result != "null" && result != "\"\"") {
                        val unquoted = if (result.startsWith("\"") && result.endsWith("\"") && result.length >= 2) {
                            result.substring(1, result.length - 1)
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"")
                        } else {
                            result
                        }
                        onPageTextExtracted(unquoted)
                    }
                }
            }

            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                return true
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onProgressChanged(newProgress)
                if (newProgress > 60) {
                    view?.evaluateJavascript(AdBlockManager.MEDIA_SNIFFER_JS, null)
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                onPageFinished(view?.url.orEmpty(), title)
            }
        }

        onDispose {
            try {
                webView.removeJavascriptInterface("AegisBridge")
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.clearHistory()
                webView.removeAllViews()
                webView.destroy()
            } catch (_: Exception) {}
        }
    }

    // Update User Agent dynamically based on userAgentMode or Desktop mode
    LaunchedEffect(tab.isDesktopMode, tab.userAgentMode) {
        val effectiveUserAgent = when {
            tab.userAgentMode.customString != null -> tab.userAgentMode.customString
            tab.isDesktopMode -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
            else -> null
        }
        webView.settings.userAgentString = effectiveUserAgent
        if (tab.url.isNotBlank() && tab.url != "about:blank" && tab.url != "about:home") {
            webView.reload()
        }
    }

    // Incognito mode effects: clear cache and prevent persistent cookies
    LaunchedEffect(tab.isIncognito) {
        if (tab.isIncognito) {
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            webView.clearCache(true)
            webView.clearHistory()
            try {
                android.webkit.CookieManager.getInstance().removeSessionCookies(null)
            } catch (_: Exception) {}
        } else {
            webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        }
    }

    // Navigate when tab URL changes
    LaunchedEffect(tab.url) {
        if (tab.url != "about:home" && tab.url != "about:blank") {
            if (webView.url != tab.url) {
                webView.loadUrl(tab.url)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
