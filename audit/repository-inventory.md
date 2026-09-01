# Aegis Browser — Repository Inventory

## 1. Project Overview
- **Application Name**: Aegis Browser (Privacy & Downloader Browser)
- **Package / Namespace**: `com.example`
- **Application ID**: `com.aistudio.aegisbrowser.kxmpzq`
- **Target Branch**: `test`

## 2. Directory Structure
```
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── assets/
│   │   │   └── aegis-sniffer.js
│   │   ├── java/com/example/
│   │   │   ├── AegisApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── adblock/
│   │   │   │   │   ├── AdBlockManager.kt
│   │   │   │   │   └── AdBlockStructures.kt
│   │   │   │   ├── downloader/
│   │   │   │   │   ├── DownloadManager.kt
│   │   │   │   │   ├── DownloadQueueManager.kt
│   │   │   │   │   ├── DownloadWorker.kt
│   │   │   │   │   ├── HlsManifestParser.kt
│   │   │   │   │   ├── MediaExtractorEngine.kt
│   │   │   │   │   ├── NetworkTrafficMonitor.kt
│   │   │   │   │   └── YtdlpWrapper.kt
│   │   │   │   ├── gemini/
│   │   │   │   │   └── GeminiClient.kt
│   │   │   │   ├── local/
│   │   │   │   │   ├── AegisDatabase.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── AutoFillDao.kt
│   │   │   │   │   │   ├── BookmarkDao.kt
│   │   │   │   │   │   └── HistoryDao.kt
│   │   │   │   │   └── entity/
│   │   │   │   │       ├── AutoFillEntity.kt
│   │   │   │   │       ├── BookmarkEntity.kt
│   │   │   │   │       └── HistoryEntity.kt
│   │   │   │   ├── model/
│   │   │   │   │   └── BrowserModels.kt
│   │   │   │   ├── reader/
│   │   │   │   │   └── ReaderModeExtractor.kt
│   │   │   │   ├── repository/
│   │   │   │   │   └── BrowserRepository.kt
│   │   │   │   ├── security/
│   │   │   │   │   ├── AegisCryptoManager.kt
│   │   │   │   │   ├── AttestationManager.kt
│   │   │   │   │   └── SafeModeFilter.kt
│   │   │   │   └── suggest/
│   │   │   │       └── SearchSuggestClient.kt
│   │   │   ├── navigation/
│   │   │   │   └── BrowserNavigation.kt
│   │   │   ├── ui/
│   │   │   │   ├── AegisNavApp.kt
│   │   │   │   ├── BrowserMainScreen.kt
│   │   │   │   ├── components/ (23 Composables)
│   │   │   │   ├── navigation/ (AegisRoutes.kt, Screen.kt)
│   │   │   │   ├── pages/ (8 Page Composables)
│   │   │   │   ├── theme/ (Color.kt, Theme.kt, Type.kt, Shape.kt, AegisThemeTokens.kt)
│   │   │   │   └── utils/ (AccessibilityUtils.kt)
│   │   │   └── viewmodel/
│   │   │       ├── BrowserViewModel.kt
│   │   │       └── MemoryWatchdog.kt
│   │   └── res/
│   └── test/
│       └── java/com/example/
│           ├── ExampleRobolectricTest.kt
│           ├── ExampleUnitTest.kt
│           └── GreetingScreenshotTest.kt
```

## 3. Metrics & File Counts
- **Kotlin Source Files**: 45
- **Resource Files (XML & Drawables)**: 25
- **Test Files**: 3
- **Largest Files**:
  - `BrowserViewModel.kt` (~1050 LOC)
  - `BrowserMainScreen.kt` (~420 LOC)
  - `BrowserWebView.kt` (~360 LOC)
  - `GeminiClient.kt` (~310 LOC)
