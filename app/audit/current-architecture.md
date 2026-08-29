# Aegis Browser — Current Architecture

## 1. Engine Reality
- **Browser Engine**: Android `android.webkit.WebView` configured with custom `WebViewClient`, `WebChromeClient`, JavaScript Bridge (`AegisJsBridge`), DOM sniffer injection (`aegis-sniffer.js`), and AdBlock request interception.
- **Privacy Layer**: In-memory Trie and bloom filters (`AdBlockManager`) for blocking tracker/ad domains and injecting cosmetic CSS filters.
- **Downloader Subsystem**:
  - Direct streams via `DownloadManager` and `DownloadWorker` (WorkManager).
  - HLS / DASH manifest parser (`NetworkTrafficMonitor`, `HlsManifestParser`).
  - Video stream extractor wrapper (`YtdlpWrapper`, `MediaExtractorEngine`).
- **Security & Attestation**:
  - Safe Mode domain whitelist & classifier (`SafeModeFilter`).
  - Signed opt-in attestation log with HMAC-SHA256 verification (`AttestationManager`).
  - AES-256 encrypted auto-fill vault (`AegisCryptoManager`).
- **Local Persistence**:
  - Room Database 2.6 (`AegisDatabase`) with DAOs for Bookmarks, History, and Encrypted AutoFill Credentials.
- **State Management**:
  - MVVM pattern using Jetpack `ViewModel` (`BrowserViewModel`), `StateFlow`, and Compose `collectAsStateWithLifecycle`.
- **Design System**:
  - Material Design 3 with custom Aegis semantic tokens (`AegisBrowserTheme`, `AegisThemeTokens`, `Typography`, `AegisShapes`).

## 2. Component Flow Diagram
```
[User Interface (Jetpack Compose)]
  ├── OmniboxBar & BrowserHomeDashboard
  ├── BrowserWebView (WebKit + AegisJsBridge + AdBlock)
  ├── BrowserOverflowMenuSheet (Brave/Chromium style 3-dot menu)
  ├── ShieldDashboardModal & MediaGrabberBottomSheet
  └── Fullscreen Pages (Tabs, History, Bookmarks, Downloads, Vault, Settings, AI)
         │
         ▼
[BrowserViewModel & MemoryWatchdog]
         │
         ├───► [BrowserRepository] ───► [Room DB (AegisDatabase)]
         ├───► [SafeModeFilter & AttestationManager]
         ├───► [AdBlockManager & Sniffer]
         ├───► [DownloadQueueManager & DownloadWorker]
         └───► [GeminiClient (AI Reasoning)]
```
