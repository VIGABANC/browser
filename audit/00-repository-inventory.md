# Aegis Browser — Repository Inventory (Phase 1)

## 1. Directory Structure Audit
- **`app/`**: PRESENT (`app/src/main/...`, `app/src/test/...`, `app/build.gradle.kts`)
- **`src/`**: PRESENT (Inside `app/src/`)
- **`browser/`**: NOT PRESENT
- **`components/`**: PRESENT AS COMPOSE UI COMPONENTS (`app/src/main/java/com/example/ui/components/`), NOT Chromium native components.
- **`chromium_src/`**: NOT PRESENT
- **`patches/`**: NOT PRESENT
- **`build/`**: PRESENT (Gradle output directory)

---

## 2. File Count & Metric Breakdown
- **Total Kotlin Source Files**: 74 (`app/src/main/java/`, `app/src/test/java/`, `app/src/androidTest/java/`)
- **Java Files**: 0
- **C / C++ Source Files**: 0
- **Rust Source Files**: 0
- **GN / GNI Build Files**: 0
- **Gradle Build Files**: 4 (`build.gradle.kts`, `app/build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`)
- **Android XML Resources**: 20 (`strings.xml`, `colors.xml`, `themes.xml`, vector drawables, `backup_rules.xml`, `data_extraction_rules.xml`)
- **Tests**: 4 test files (`ExampleUnitTest.kt`, `ExampleRobolectricTest.kt`, `GreetingScreenshotTest.kt`, `ExampleInstrumentedTest.kt`)
- **Assets**: 1 asset (`aegis-sniffer.js`)
- **Native Libraries**: None checked in (.so files generated during build runtime only)
- **Binaries**: None (Wrapper code exists for external tools like `yt-dlp` when provided on host/device)

---

## 3. Top Source Modules
1. `com.example.ui.components` — 23 Compose UI components (Omnibox, WebView container, Overflow sheet, Tab switcher, Shield modal, etc.)
2. `com.example.ui.pages` — 8 Top-level destination pages (TabManager, Downloads, ShieldDashboard, Vault, Settings, ReaderMode, AiAssistant, BookmarksHistory)
3. `com.example.viewmodel` — `BrowserViewModel.kt` (State management, tab lifecycle, downloader, bookmarks, history, Safe Mode, crypto)
4. `com.example.data.downloader` — Media extraction, HLS parser, network traffic monitor, WorkManager download worker
5. `com.example.data.security` — `AttestationManager.kt`, `SafeModeFilter.kt`, `AegisCryptoManager.kt`
6. `com.example.data.local` — Room Database (`AegisDatabase`, `BookmarkDao`, `HistoryDao`, `AutoFillDao`)
7. `com.example.data.adblock` — In-memory Trie and bloom filter blocker (`AdBlockManager.kt`, `AdBlockStructures.kt`)
8. `com.example.data.gemini` — On-device / cloud AI assistant reasoning client
