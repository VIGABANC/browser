# Aegis Browser — Architecture Gap & Engine Determination (Phases 2 & 3)

## 1. Real Browser Engine Classification

### **Classification: C — Android WebView Browser**

### Evidence (File + Line References):
- `app/src/main/java/com/example/ui/components/BrowserWebView.kt:1` — Uses `android.webkit.WebView`, `android.webkit.WebViewClient`, `android.webkit.WebChromeClient`, and `android.webkit.WebResourceRequest`.
- `app/src/main/java/com/example/ui/components/BrowserWebView.kt:28` — Uses `androidx.compose.ui.viewinterop.AndroidView` to embed the Android `WebView` into the Jetpack Compose tree.
- `app/src/main/java/com/example/ui/components/BrowserWebView.kt:180` — Implements `WebViewClient.shouldInterceptRequest()` for ad-blocking and `WebViewClient.shouldOverrideUrlLoading()` for navigation/intent safety.
- `app/src/main/java/com/example/ui/components/BrowserWebView.kt:320` — Implements `WebChromeClient.onProgressChanged()` and `onReceivedTitle()` for page progress tracking.
- `app/src/main/java/com/example/ui/components/BrowserWebView.kt:130` — Uses `addJavascriptInterface(AegisJsBridge(tab.id, onMediaDetected), "AegisBridge")` for DOM media communication.
- **Negative Evidence**:
  - Zero references to `org.chromium.chrome.*`, `ChromeTabbedActivity`, `content::WebContents`, `BraveActivity`, `brave-core`, `chromium_src`, or `BUILD.gn`.

---

## 2. Reusability Analysis (WebView Prototype vs. Brave Core Target)

### What CAN Be Reused Directly (100% Portable):
1. **Design System & Compose UI Layers**:
   - `com.example.ui.theme.*`: Color schemes (`Color.kt`), M3 Typography (`Type.kt`), Shapes (`Shape.kt`), and custom semantic tokens (`AegisThemeTokens.kt`).
   - `com.example.ui.components.*`: Omnibox (`OmniboxBar.kt`), Brave-style Overflow Menu Sheet (`BrowserOverflowMenuSheet.kt`), Tab Switcher Grid (`TabSwitcherView.kt`), Shield Dashboard Modal (`ShieldDashboardModal.kt`), Media Grabber Bottom Sheet (`MediaGrabberBottomSheet.kt`), and Reader Mode (`ReaderModeModal.kt`).
   - `com.example.ui.pages.*`: Fullscreen pages for Downloads, Bookmarks/History, AutoFill Vault, Settings, and AI Assistant.
2. **Local Persistence & Data Layer**:
   - `com.example.data.local.AegisDatabase`: Room 2.6 database with TypeConverters and schemas for Bookmarks, History, and Credentials.
   - `com.example.data.local.dao.*`: `BookmarkDao`, `HistoryDao`, `AutoFillDao`.
   - `com.example.data.local.entity.*`: `BookmarkEntity`, `HistoryEntity`, `AutoFillEntity`.
3. **Security, Cryptography & Attestation**:
   - `com.example.data.security.AegisCryptoManager`: AES-256 GCM encryption/decryption with Android KeyStore.
   - `com.example.data.security.AttestationManager`: HMAC-SHA256 log signing, verification, and tamper detection.
   - `com.example.data.security.SafeModeFilter`: Domain policy evaluation, classification, and override validation.
4. **Downloader Pipeline Subsystem**:
   - `com.example.data.downloader.DownloadQueueManager`: Memory queue with state machines (PENDING, RUNNING, PAUSED, COMPLETED, FAILED).
   - `com.example.data.downloader.DownloadWorker`: Jetpack WorkManager integration for persistent background downloading.
   - `com.example.data.downloader.HlsManifestParser`: M3U8 multi-variant parsing and resolution selection.
5. **AI Assistant Integration**:
   - `com.example.data.gemini.GeminiClient`: Chain-of-thought prompt engineering, page summarizer, media safety scanner.

### What CANNOT Be Reused (Tightly Coupled to WebView):
1. `BrowserWebView.kt`: Uses Android `WebViewClient`, `WebChromeClient`, and `WebView.evaluateJavascript`.
2. `aegis-sniffer.js`: DOM observer injected via `WebView.evaluateJavascript`.
3. `AdBlockManager.kt` request interception: Intercepts via `WebViewClient.shouldInterceptRequest()` returning `WebResourceResponse`.

---

## 3. Recommended Migration Strategy (If Brave Core is Mandated)

1. **Step 1 — Freeze Current Implementation as Reference Prototype**:
   - Keep the current Android WebView + Jetpack Compose app as the gold-standard reference prototype for UX, UI animations, styling, and business logic.
2. **Step 2 — Prove Vanilla Brave Android Build**:
   - Check out upstream Brave Android (`brave-browser` / `brave-core`), sync Chromium dependencies, and establish a successful reproducible build baseline.
3. **Step 3 — Pin Specific Chromium & Brave Revisions**:
   - Lock toolchains (NDK, depot_tools, JDK) to ensure build reproducibility.
4. **Step 4 — Add Aegis Subsystems as Modular Patches**:
   - Port `AegisCryptoManager`, `AttestationManager`, and `SafeModeFilter` into the C++ / JNI / Chromium Content layer.
   - Replace standard URL interception with Brave's native ad-block / Shields engine hooks.
5. **Step 5 — Re-skin Frontend with Aegis Design System**:
   - Apply Aegis M3 theme tokens, omnibox styles, and bottom sheet menus to the Chromium Android UI surface.
