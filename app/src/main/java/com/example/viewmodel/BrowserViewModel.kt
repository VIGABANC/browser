package com.example.viewmodel

import android.app.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.adblock.AdBlockManager
import com.example.data.downloader.DownloadManager
import com.example.data.downloader.MediaExtractorEngine
import com.example.data.gemini.GeminiClient
import com.example.data.local.AegisDatabase
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.model.AegisAiMessage
import com.example.data.model.AiTaskType
import com.example.data.model.AutoFillCredential
import com.example.data.model.Bookmark
import com.example.data.model.BrowserTab
import com.example.data.model.DetectedMedia
import com.example.data.model.DownloadItem
import com.example.data.model.FindInPageState
import com.example.data.model.HistoryItem
import com.example.data.model.MediaFormat
import com.example.data.model.MessageRole
import com.example.data.model.ReaderArticle
import com.example.data.model.SafeModeState
import com.example.data.model.SearchEngine
import com.example.data.model.ShieldStats
import com.example.data.model.UserAgentMode
import com.example.data.reader.ReaderModeExtractor
import com.example.data.repository.BrowserRepository
import com.example.data.suggest.SearchSuggestClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.data.model.TabChip
import com.example.data.model.ChipType
import com.example.data.model.TabStripState
import com.example.data.model.StripType
import org.json.JSONArray
import java.net.URLEncoder
import java.util.UUID
import java.security.MessageDigest

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AegisDatabase.getDatabase(application.applicationContext)
    private val repository = BrowserRepository(database.historyDao(), database.bookmarkDao(), database.autoFillDao())

    val attestationManager = com.example.data.security.AttestationManager(application.applicationContext)
    val safeModeFilter = com.example.data.security.SafeModeFilter()

    val downloadManager = DownloadManager(application.applicationContext, viewModelScope)
    val activeDownloads: StateFlow<List<DownloadItem>> = downloadManager.downloads

    // User-Agent setting
    private val _userAgentMode = MutableStateFlow(UserAgentMode.SYSTEM_DEFAULT)
    val userAgentMode: StateFlow<UserAgentMode> = _userAgentMode.asStateFlow()

    // Incognito mode session toggle
    private val _isIncognitoSession = MutableStateFlow(false)
    val isIncognitoSession: StateFlow<Boolean> = _isIncognitoSession.asStateFlow()

    private val _webGoBackTrigger = MutableStateFlow(0)
    val webGoBackTrigger: StateFlow<Int> = _webGoBackTrigger.asStateFlow()

    fun triggerWebGoBack() {
        _webGoBackTrigger.update { it + 1 }
    }

    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        updateActiveTab { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
    }

    // Initial default tab
    private val initialTab = BrowserTab(
        id = UUID.randomUUID().toString(),
        title = "Aegis Home",
        url = "about:home",
        isIncognito = false,
        userAgentMode = UserAgentMode.SYSTEM_DEFAULT,
        sslSecure = true
    )

    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    val activeTab: StateFlow<BrowserTab> = combine(_tabs, _activeTabId) { tabList, currentId ->
        tabList.firstOrNull { it.id == currentId } ?: tabList.firstOrNull() ?: initialTab
    }.stateIn(viewModelScope, SharingStarted.Eagerly, initialTab)

    // Tab strip state for open tabs
    private val _tabStripUserDismissed = MutableStateFlow(false)
    val tabStripUserDismissed: StateFlow<Boolean> = _tabStripUserDismissed.asStateFlow()

    private val _tabStripState = MutableStateFlow(TabStripState())
    val tabStripState: StateFlow<TabStripState> = combine(_tabs, _activeTabId, _tabStripUserDismissed) { tabList, currentId, dismissed ->
        TabStripState(
            chips = tabList.map { tab ->
                val displayTitle = when {
                    tab.title.isNotBlank() && tab.title != "Loading..." -> tab.title
                    tab.url == "about:home" -> if (tab.isIncognito) "Onglet Privé" else "Nouvel onglet"
                    else -> tab.url.removePrefix("https://").removePrefix("http://").removePrefix("www.").take(24).ifBlank { "Onglet" }
                }
                TabChip(
                    id = tab.id,
                    title = displayTitle,
                    url = tab.url,
                    isActive = tab.id == currentId,
                    isCloseable = true,
                    type = ChipType.TAB
                )
            },
            activeChipId = currentId,
            isVisible = !dismissed && tabList.size > 1,
            stripType = StripType.TABS
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TabStripState())

    // Feature strip state (matches screenshots navigation chips)
    private val _featureStripVisible = MutableStateFlow(true)
    val featureStripVisible: StateFlow<Boolean> = _featureStripVisible.asStateFlow()

    private val _featureChips = MutableStateFlow(
        listOf(
            TabChip("sniffer", "Media Sniffer API", type = ChipType.FEATURE, isActive = true),
            TabChip("adblock", "Add Ad-Blocker Logic", type = ChipType.FEATURE),
            TabChip("gemini", "Gemini Privacy Toggle", type = ChipType.FEATURE),
            TabChip("history", "Clear History Dialog", type = ChipType.FEATURE),
            TabChip("autofill", "Password Vault", type = ChipType.FEATURE),
            TabChip("reader", "Reader Mode", type = ChipType.FEATURE)
        )
    )
    val featureChips: StateFlow<List<TabChip>> = _featureChips.asStateFlow()

    val activeFeatureId: StateFlow<String?> = _featureChips
        .map { chips -> chips.firstOrNull { it.isActive }?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "sniffer")

    fun selectFeatureChip(chipId: String) {
        _featureChips.update { chips ->
            chips.map { it.copy(isActive = it.id == chipId) }
        }
    }

    fun dismissFeatureStrip() {
        _featureStripVisible.value = false
    }

    fun showFeatureStrip() {
        _featureStripVisible.value = true
    }

    fun dismissTabStrip() {
        _tabStripUserDismissed.value = true
    }

    fun showTabStrip() {
        _tabStripUserDismissed.value = false
    }

    fun updateTabStrip(tabs: List<BrowserTab>, activeId: String) {
        _tabStripState.value = TabStripState(
            chips = tabs.map { tab ->
                TabChip(
                    id = tab.id,
                    title = tab.title.ifBlank { "New Tab" },
                    url = tab.url,
                    isActive = tab.id == activeId,
                    isCloseable = true,
                    type = ChipType.TAB
                )
            },
            activeChipId = activeId,
            isVisible = !_tabStripUserDismissed.value && tabs.size > 1,
            stripType = StripType.TABS
        )
    }

    // Sheet / Modal visibility states
    private val _isTabSwitcherOpen = MutableStateFlow(false)
    val isTabSwitcherOpen: StateFlow<Boolean> = _isTabSwitcherOpen.asStateFlow()

    private val _isMediaGrabberOpen = MutableStateFlow(false)
    val isMediaGrabberOpen: StateFlow<Boolean> = _isMediaGrabberOpen.asStateFlow()

    private val _selectedMediaForGrabber = MutableStateFlow<DetectedMedia?>(null)
    val selectedMediaForGrabber: StateFlow<DetectedMedia?> = _selectedMediaForGrabber.asStateFlow()

    private val _isDownloadCenterOpen = MutableStateFlow(false)
    val isDownloadCenterOpen: StateFlow<Boolean> = _isDownloadCenterOpen.asStateFlow()

    private val _isShieldDashboardOpen = MutableStateFlow(false)
    val isShieldDashboardOpen: StateFlow<Boolean> = _isShieldDashboardOpen.asStateFlow()

    private val _isAiSheetOpen = MutableStateFlow(false)
    val isAiSheetOpen: StateFlow<Boolean> = _isAiSheetOpen.asStateFlow()

    private val _isBookmarksHistoryOpen = MutableStateFlow(false)
    val isBookmarksHistoryOpen: StateFlow<Boolean> = _isBookmarksHistoryOpen.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isAttestationDialogOpen = MutableStateFlow(false)
    val isAttestationDialogOpen: StateFlow<Boolean> = _isAttestationDialogOpen.asStateFlow()

    // Find In Page State
    private val _findInPageState = MutableStateFlow(FindInPageState())
    val findInPageState: StateFlow<FindInPageState> = _findInPageState.asStateFlow()

    private val _findActionTrigger = MutableStateFlow(0)
    val findActionTrigger: StateFlow<Int> = _findActionTrigger.asStateFlow()

    private val _findForward = MutableStateFlow(true)
    val findForward: StateFlow<Boolean> = _findForward.asStateFlow()

    // Reader Mode State
    private val _isReaderModeOpen = MutableStateFlow(false)
    val isReaderModeOpen: StateFlow<Boolean> = _isReaderModeOpen.asStateFlow()

    private val _readerArticle = MutableStateFlow<ReaderArticle?>(null)
    val readerArticle: StateFlow<ReaderArticle?> = _readerArticle.asStateFlow()

    // Encrypted AutoFill State
    val autoFillCredentials: StateFlow<List<AutoFillCredential>> = repository.allCredentials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAutoFillModalOpen = MutableStateFlow(false)
    val isAutoFillModalOpen: StateFlow<Boolean> = _isAutoFillModalOpen.asStateFlow()

    private val _activeAutoFillPrompt = MutableStateFlow<AutoFillCredential?>(null)
    val activeAutoFillPrompt: StateFlow<AutoFillCredential?> = _activeAutoFillPrompt.asStateFlow()

    private val _autoFillPayload = MutableStateFlow<Pair<String, String>?>(null)
    val autoFillPayload: StateFlow<Pair<String, String>?> = _autoFillPayload.asStateFlow()

    // Google Search Suggestions State
    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()
    private var suggestJob: Job? = null

    // Shields State with Recharts-Style Session Trend Data
    private val _shieldStats = MutableStateFlow(
        ShieldStats(
            sessionTrendPoints = com.example.ui.components.generateDefaultSessionTrend()
        )
    )
    val shieldStats: StateFlow<ShieldStats> = _shieldStats.asStateFlow()

    // Gemini Webpage Summary Bottom Sheet State
    private val _isSummaryBottomSheetOpen = MutableStateFlow(false)
    val isSummaryBottomSheetOpen: StateFlow<Boolean> = _isSummaryBottomSheetOpen.asStateFlow()

    private val _latestSummaryMessage = MutableStateFlow<AegisAiMessage?>(null)
    val latestSummaryMessage: StateFlow<AegisAiMessage?> = _latestSummaryMessage.asStateFlow()

    // Safe Mode State
    private val _safeModeState = MutableStateFlow(SafeModeState())
    val safeModeState: StateFlow<SafeModeState> = _safeModeState.asStateFlow()

    // Search Engine
    private val _searchEngine = MutableStateFlow(SearchEngine.DUCKDUCKGO)
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    // Theme Mode (Dark / Light)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Bookmarks & History via Room Database
    val history: StateFlow<List<HistoryItem>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<Bookmark>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gemini High-Thinking Assistant State
    private val _aiMessages = MutableStateFlow<List<AegisAiMessage>>(createInitialAiMessages())
    val aiMessages: StateFlow<List<AegisAiMessage>> = _aiMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _selectedAiTask = MutableStateFlow(AiTaskType.DEEP_REASONING)
    val selectedAiTask: StateFlow<AiTaskType> = _selectedAiTask.asStateFlow()

    // In-App Audio/Video Player State
    private val _currentlyPlayingMedia = MutableStateFlow<String?>(null)
    val currentlyPlayingMedia: StateFlow<String?> = _currentlyPlayingMedia.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _latestPageText = MutableStateFlow("")
    val latestPageText: StateFlow<String> = _latestPageText.asStateFlow()

    // Home Dashboard Customization State (from Brave / Chrome inspiration)
    private val _isScenicWallpaper = MutableStateFlow(true)
    val isScenicWallpaper: StateFlow<Boolean> = _isScenicWallpaper.asStateFlow()

    private val _isPrivacyStatsVisible = MutableStateFlow(true)
    val isPrivacyStatsVisible: StateFlow<Boolean> = _isPrivacyStatsVisible.asStateFlow()

    private val _isDiscoverFeedVisible = MutableStateFlow(true)
    val isDiscoverFeedVisible: StateFlow<Boolean> = _isDiscoverFeedVisible.asStateFlow()

    fun toggleScenicWallpaper() {
        _isScenicWallpaper.update { !it }
    }

    fun togglePrivacyStatsVisible() {
        _isPrivacyStatsVisible.update { !it }
    }

    fun toggleDiscoverFeedVisible() {
        _isDiscoverFeedVisible.update { !it }
    }

    fun addCustomShortcut(title: String, url: String) {
        val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
        addBookmark(title.ifBlank { cleanUrl }, cleanUrl)
    }

    fun clearBrowsingData() {
        clearHistory()
    }

    private val memoryWatchdog = MemoryWatchdog(application.applicationContext)

    init {
        // Memory Watchdog loop
        viewModelScope.launch {
            while (kotlinx.coroutines.currentCoroutineContext().isActive) {
                memoryWatchdog.checkMemoryPressure()
                delay(30_000)
            }
        }
        
        // Seed default bookmarks and history in Room DB if empty
        viewModelScope.launch {
            val existingBookmarks = repository.allBookmarks.first()
            if (existingBookmarks.isEmpty()) {
                createDefaultBookmarks().forEach { b ->
                    repository.addBookmark(b.title, b.url, b.folder)
                }
            }
            val existingHistory = repository.allHistory.first()
            if (existingHistory.isEmpty()) {
                createDefaultHistory().forEach { h ->
                    repository.recordHistory(h.title, h.url, h.isSecure)
                }
            }
        }
    }

    // -------------------------------------------------------------
    // User-Agent & Incognito Settings
    // -------------------------------------------------------------
    fun setUserAgentMode(mode: UserAgentMode) {
        _userAgentMode.value = mode
        updateActiveTab { it.copy(userAgentMode = mode) }
    }

    fun toggleIncognitoSession(enabled: Boolean, context: android.content.Context? = null) {
        val wasIncognito = _isIncognitoSession.value
        _isIncognitoSession.value = enabled
        updateActiveTab { it.copy(isIncognito = enabled) }

        if (!enabled && wasIncognito && context != null) {
            // Automatically wipe cookies, webstorage, and session cache when exiting incognito mode
            clearBrowserCacheAndCookies(context, alsoClearHistory = false)
            _tabs.update { tabs ->
                val remaining = tabs.filter { !it.isIncognito }
                if (remaining.isEmpty()) {
                    listOf(BrowserTab(id = UUID.randomUUID().toString(), title = "Aegis Home", url = "about:home", isIncognito = false))
                } else {
                    remaining
                }
            }
            _activeTabId.value = _tabs.value.first().id
        }
    }

    // -------------------------------------------------------------
    // Tab Management
    // -------------------------------------------------------------
    fun addNewTab(url: String = "about:home", isIncognito: Boolean = _isIncognitoSession.value) {
        val newTab = BrowserTab(
            id = UUID.randomUUID().toString(),
            title = if (url == "about:home") (if (isIncognito) "Incognito Tab" else "Aegis Home") else "Loading...",
            url = url,
            isIncognito = isIncognito,
            userAgentMode = _userAgentMode.value,
            sslSecure = true
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        _isTabSwitcherOpen.value = false
    }

    fun switchTab(tabId: String) {
        _activeTabId.value = tabId
        _isTabSwitcherOpen.value = false
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            val freshTab = BrowserTab(
                title = "Aegis Home",
                url = "about:home",
                isIncognito = _isIncognitoSession.value,
                userAgentMode = _userAgentMode.value
            )
            _tabs.value = listOf(freshTab)
            _activeTabId.value = freshTab.id
            return
        }

        val updated = currentList.filterNot { it.id == tabId }
        _tabs.value = updated
        if (_activeTabId.value == tabId) {
            _activeTabId.value = updated.last().id
        }
    }

    fun closeAllTabs() {
        val freshTab = BrowserTab(
            title = "Aegis Home",
            url = "about:home",
            isIncognito = _isIncognitoSession.value,
            userAgentMode = _userAgentMode.value
        )
        _tabs.value = listOf(freshTab)
        _activeTabId.value = freshTab.id
        _isTabSwitcherOpen.value = false
    }

    // -------------------------------------------------------------
    // Web Navigation & Room History Tracking
    // -------------------------------------------------------------
    fun loadUrlOrQuery(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return

        val targetUrl = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("about:") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> {
                val encoded = URLEncoder.encode(trimmed, "UTF-8")
                "${_searchEngine.value.searchUrl}$encoded"
            }
        }

        updateActiveTab {
            it.copy(
                url = targetUrl,
                title = "Loading...",
                isLoading = true,
                progress = 0.1f,
                sslSecure = targetUrl.startsWith("https://"),
                detectedMediaList = emptyList()
            )
        }

        // Only record to Room history if NOT incognito
        if (!activeTab.value.isIncognito && !_isIncognitoSession.value && !targetUrl.startsWith("about:")) {
            recordHistoryVisit(targetUrl, targetUrl, targetUrl.startsWith("https://"))
        }
    }

    fun updateActiveTab(transform: (BrowserTab) -> BrowserTab) {
        val currentId = _activeTabId.value
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == currentId) transform(tab) else tab
            }
        }
    }

    fun onPageStarted(url: String) {
        updateActiveTab {
            it.copy(
                url = url,
                isLoading = true,
                sslSecure = url.startsWith("https://")
            )
        }
    }

    fun onTitleReceived(title: String) {
        if (title.isNotBlank() && title != "about:blank") {
            updateActiveTab { current ->
                if (current.title != title) current.copy(title = title) else current
            }
        }
    }

    fun onPageFinished(url: String, title: String?) {
        val cleanTitle = if (!title.isNullOrBlank()) title else url
        updateActiveTab {
            it.copy(
                url = url,
                title = if (cleanTitle.isNotBlank()) cleanTitle else it.title,
                isLoading = false,
                progress = 1f,
                sslSecure = url.startsWith("https://")
            )
        }
        // Room history insertion (skip if incognito)
        if (!activeTab.value.isIncognito && !_isIncognitoSession.value && !url.startsWith("about:")) {
            recordHistoryVisit(cleanTitle, url, url.startsWith("https://"))
        }
    }

    fun onProgressChanged(progress: Int) {
        val p = progress / 100f
        updateActiveTab { current ->
            if (Math.abs(current.progress - p) >= 0.05f || (p >= 1f && current.isLoading)) {
                current.copy(progress = p, isLoading = p < 1f)
            } else {
                current
            }
        }
    }

    fun toggleDesktopMode() {
        updateActiveTab { it.copy(isDesktopMode = !it.isDesktopMode) }
    }

    fun incrementBlockedAds(count: Int = 1) {
        _shieldStats.update {
            it.copy(
                adsBlockedTotal = it.adsBlockedTotal + count,
                bandwidthSavedMb = it.bandwidthSavedMb + (0.12f * count)
            )
        }
        updateActiveTab { it.copy(blockedAdsCount = it.blockedAdsCount + count) }
    }

    fun incrementBlockedTrackers(count: Int = 1) {
        _shieldStats.update {
            it.copy(
                trackersBlockedTotal = it.trackersBlockedTotal + count,
                fingerprintAttemptsBlocked = it.fingerprintAttemptsBlocked + count
            )
        }
        updateActiveTab { it.copy(blockedTrackersCount = it.blockedTrackersCount + count) }
    }

    // -------------------------------------------------------------
    // Media Sniffer & Grabber Integration
    // -------------------------------------------------------------
    fun onMediaDetectedFromJs(jsonPayload: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val detected = com.example.data.downloader.MediaDetectionEngine.processDomMediaPayload(
                jsonPayload,
                activeTab.value.url
            )
            
            if (detected != null) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    updateActiveTab { tab ->
                        val updatedList = tab.detectedMediaList.toMutableList()
                        val existing = updatedList.indexOfFirst { it.url == detected.url }
                        if (existing != -1) {
                            updatedList[existing] = detected
                        } else {
                            updatedList.add(detected)
                        }
                        
                        val finalDeduplicated = com.example.data.downloader.MediaDetectionEngine.deduplicateAndRank(updatedList)
                        tab.copy(detectedMediaList = finalDeduplicated)
                    }
                }
            }
        }
    }

    fun onNetworkMediaDetected(media: DetectedMedia) {
        updateActiveTab { tab ->
            val updated = (tab.detectedMediaList + media).distinctBy { it.url }
            tab.copy(detectedMediaList = com.example.data.downloader.MediaDetectionEngine.deduplicateAndRank(updated))
        }
    }

    fun onPageTextExtracted(text: String) {
        if (text.isNotBlank()) {
            _latestPageText.value = text.trim()
        }
    }

    fun addManualMediaForPage(title: String, streamUrl: String, isAudio: Boolean = false) {
        val formats = com.example.data.downloader.MediaExtractorEngine.generateAvailableFormats(title, streamUrl, 240L)
        val media = DetectedMedia(
            id = java.util.UUID.randomUUID().toString(),
            url = streamUrl,
            type = if (isAudio) com.example.data.model.MediaType.AUDIO else com.example.data.model.MediaType.VIDEO,
            source = com.example.data.model.DetectionSource.DOM,
            title = title,
            pageUrl = activeTab.value.url,
            mimeType = if (isAudio) "audio/mp3" else "video/mp4",
            duration = 240L,
            formats = formats,
            domain = activeTab.value.url.substringAfter("://").substringBefore("/")
        )
        updateActiveTab { tab ->
            val updated = (tab.detectedMediaList + media).distinctBy { it.url }
            tab.copy(detectedMediaList = com.example.data.downloader.MediaExtractorEngine.deduplicateMedia(updated))
        }
    }

    fun openMediaGrabber(media: DetectedMedia? = null) {
        val target = media ?: activeTab.value.detectedMediaList.firstOrNull() ?: createFallbackMediaForCurrentPage()
        _selectedMediaForGrabber.value = target
        _isMediaGrabberOpen.value = true
    }

    fun dismissMediaGrabber() {
        _isMediaGrabberOpen.value = false
    }

    fun requestDownload(format: MediaFormat) {
        val media = _selectedMediaForGrabber.value ?: return
        
        // 1. Safe mode check
        if (attestationManager.isSafeMode) {
            if (!safeModeFilter.isSafeSource(media.url, media.pageUrl)) {
                _isAttestationDialogOpen.value = true
                return
            }
        }

        val attested = !attestationManager.isSafeMode

        downloadManager.enqueueDownload(
            title = media.title ?: "Unknown Media",
            sourcePageUrl = media.pageUrl,
            downloadUrl = format.directUrl.ifBlank { media.url },
            format = format,
            isSafeModeAttested = attested
        )

        _isMediaGrabberOpen.value = false
    }
    
    fun requestBatchDownload(mediaItems: List<DetectedMedia>) {
        val isSafe = _safeModeState.value.isSafeModeActive
        val attested = _safeModeState.value.hasUserAttested
        
        if (isSafe && !attested) {
            _isAttestationDialogOpen.value = true
            return
        }
        
        mediaItems.forEach { media ->
            val format = media.formats.firstOrNull() ?: return@forEach
            
            downloadManager.enqueueDownload(
                title = media.title ?: "Unknown Media",
                sourcePageUrl = media.pageUrl,
                downloadUrl = format.directUrl.ifBlank { media.url },
                format = format,
                isSafeModeAttested = attested
            )
        }
        
        _isMediaGrabberOpen.value = false
    }

    private fun createFallbackMediaForCurrentPage(): DetectedMedia {
        val currentTab = activeTab.value
        val title = currentTab.title.ifBlank { "Web Media Stream" }
        val streamUrl = currentTab.url
        val formats = com.example.data.downloader.MediaExtractorEngine.generateAvailableFormats(title, streamUrl, 180L)
        return DetectedMedia(
            id = java.util.UUID.randomUUID().toString(),
            url = streamUrl,
            type = com.example.data.model.MediaType.VIDEO,
            source = com.example.data.model.DetectionSource.DOM,
            title = title,
            pageUrl = currentTab.url,
            mimeType = "video/mp4",
            duration = 180L,
            formats = formats,
            domain = currentTab.url.substringAfter("://").substringBefore("/")
        )
    }

    // -------------------------------------------------------------
    // Safe Mode & Attestation
    // -------------------------------------------------------------
    fun attestRightsAndAccept() {
        val fingerprint = android.provider.Settings.Secure.getString(
            getApplication<Application>().contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "aegis_device_default"

        val success = attestationManager.attestExtendedMode(fingerprint)
        if (success) {
            _safeModeState.update {
                it.copy(
                    isSafeModeActive = false,
                    hasUserAttested = true,
                    attestationTimestamp = System.currentTimeMillis()
                )
            }
        }
        _isAttestationDialogOpen.value = false
    }

    fun dismissAttestationDialog() {
        _isAttestationDialogOpen.value = false
    }

    fun toggleSafeMode(enabled: Boolean) {
        if (enabled) {
            attestationManager.revertToSafeMode()
            _safeModeState.update {
                it.copy(
                    isSafeModeActive = true,
                    hasUserAttested = false
                )
            }
        } else {
            if (!attestationManager.isExtendedMode) {
                _isAttestationDialogOpen.value = true
            } else {
                _safeModeState.update {
                    it.copy(
                        isSafeModeActive = false,
                        hasUserAttested = true
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Gemini 3.1 Pro (High Thinking) AI Assistant
    // -------------------------------------------------------------
    fun openAiAssistant(taskType: AiTaskType = AiTaskType.DEEP_REASONING, customPrompt: String? = null) {
        _selectedAiTask.value = taskType
        _isAiSheetOpen.value = true
        if (!customPrompt.isNullOrBlank()) {
            submitAiPrompt(customPrompt, taskType)
        }
    }

    fun dismissAiAssistant() {
        _isAiSheetOpen.value = false
    }

    fun submitAiPrompt(prompt: String, taskType: AiTaskType = _selectedAiTask.value) {
        if (prompt.isBlank()) return

        val userMessage = AegisAiMessage(
            role = MessageRole.USER,
            content = prompt,
            taskType = taskType
        )
        _aiMessages.update { it + userMessage }
        _isAiThinking.value = true

        val currentTab = activeTab.value
        val detectedMediaText = currentTab.detectedMediaList.joinToString("\n") {
            "• ${it.title} | ${it.mimeType} | ${it.formats.size} formats available (${it.url})"
        }

        val pageContent = if (_latestPageText.value.isNotBlank()) {
            "Viewport Title: ${currentTab.title}\nDomain: ${currentTab.url}\nShield Status: ${if (shieldStats.value.isShieldEnabled) "Protected" else "Standard"}\n\nWebpage Text Content:\n${_latestPageText.value.take(3500)}"
        } else {
            "Viewport Title: ${currentTab.title}\nDomain: ${currentTab.url}\nShield Status: ${if (shieldStats.value.isShieldEnabled) "Protected" else "Standard"}"
        }

        viewModelScope.launch {
            val response = GeminiClient.executeDeepReasoning(
                prompt = prompt,
                taskType = taskType,
                pageUrl = currentTab.url,
                pageTitle = currentTab.title,
                pageContentSnippet = pageContent,
                detectedMediaContext = detectedMediaText.ifBlank { null }
            )

            _aiMessages.update { it + response }
            _isAiThinking.value = false
        }
    }

    fun toggleThinkingTrace(messageId: String) {
        _aiMessages.update { list ->
            list.map {
                if (it.id == messageId) it.copy(isThinkingExpanded = !it.isThinkingExpanded) else it
            }
        }
    }

    fun clearAiChat() {
        _aiMessages.value = createInitialAiMessages()
    }

    // -------------------------------------------------------------
    // Bookmarks & History via Room Database
    // -------------------------------------------------------------
    fun addBookmark(title: String, url: String) {
        viewModelScope.launch {
            repository.addBookmark(title, url)
        }
    }

    fun deleteBookmark(id: String) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    fun addCurrentPageToBookmarks() {
        val tab = activeTab.value
        if (tab.url.isNotBlank() && tab.url != "about:blank" && tab.url != "about:home") {
            addBookmark(tab.title, tab.url)
        }
    }

    private fun recordHistoryVisit(title: String, url: String, isSecure: Boolean = true) {
        viewModelScope.launch {
            repository.recordHistory(title, url, isSecure)
        }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private val _isClearOnCloseEnabled = MutableStateFlow(false)
    val isClearOnCloseEnabled: StateFlow<Boolean> = _isClearOnCloseEnabled.asStateFlow()

    fun toggleClearOnClose(enabled: Boolean) {
        _isClearOnCloseEnabled.value = enabled
    }

    fun clearBrowserCacheAndCookies(context: android.content.Context, alsoClearHistory: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Clear WebKit Cookies
                val cookieManager = android.webkit.CookieManager.getInstance()
                cookieManager.removeAllCookies(null)
                cookieManager.removeSessionCookies(null)
                cookieManager.flush()

                // 2. Clear Web Storage (LocalStorage, IndexedDB, WebSQL)
                android.webkit.WebStorage.getInstance().deleteAllData()

                // 3. Clear WebView Cache files
                val cacheDir = context.cacheDir
                val webviewCache = java.io.File(cacheDir, "webview_cache")
                if (webviewCache.exists()) {
                    webviewCache.deleteRecursively()
                }
                val appWebviewDir = java.io.File(context.dataDir, "app_webview")
                if (appWebviewDir.exists()) {
                    java.io.File(appWebviewDir, "Default/Cache").deleteRecursively()
                    java.io.File(appWebviewDir, "Default/Cookies").delete()
                    java.io.File(appWebviewDir, "Default/Web Data").delete()
                }

                // 4. Clear blocked telemetry events from memory
                AdBlockManager.clearSessionBlockedEvents()

                // 5. Clear repository data if requested
                if (alsoClearHistory) {
                    repository.clearHistory()
                }
            } catch (e: Exception) {
                android.util.Log.e("BrowserViewModel", "Error clearing cache and cookies", e)
            }
        }
    }

    fun summarizeCurrentPage() {
        val currentTab = activeTab.value
        summarizeActiveUrl(currentTab.url, currentTab.title)
    }

    fun summarizeActiveUrl(url: String = activeTab.value.url, title: String = activeTab.value.title) {
        val pageTitle = title.ifBlank { "Current Webpage" }
        _isSummaryBottomSheetOpen.value = true
        _isAiThinking.value = true

        val prompt = "Provide a comprehensive, structured executive summary of this webpage: '$pageTitle' ($url). Highlight key insights, core arguments, facts, and actionable conclusions."

        val currentTab = activeTab.value
        val pageContent = if (_latestPageText.value.isNotBlank()) {
            "Viewport Title: $pageTitle\nDomain: $url\nShield Status: ${if (shieldStats.value.isShieldEnabled) "Protected" else "Standard"}\n\nWebpage Text Content:\n${_latestPageText.value.take(3500)}"
        } else {
            "Viewport Title: $pageTitle\nDomain: $url\nShield Status: ${if (shieldStats.value.isShieldEnabled) "Protected" else "Standard"}"
        }

        viewModelScope.launch {
            val response = GeminiClient.executeDeepReasoning(
                prompt = prompt,
                taskType = AiTaskType.PAGE_SUMMARY,
                pageUrl = url,
                pageTitle = pageTitle,
                pageContentSnippet = pageContent,
                detectedMediaContext = null
            )
            _latestSummaryMessage.value = response
            _aiMessages.update { it + response }
            _isAiThinking.value = false
        }
    }

    fun dismissSummaryBottomSheet() {
        _isSummaryBottomSheetOpen.value = false
    }

    fun syncFilterLists(context: android.content.Context) {
        viewModelScope.launch {
            com.example.data.adblock.FilterListManager.updateAllFilters(context)
        }
    }

    fun toggleFilterSubscription(context: android.content.Context, subscriptionId: String, enabled: Boolean) {
        com.example.data.adblock.FilterListManager.toggleSubscription(context, subscriptionId, enabled)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.clearBookmarks()
        }
        clearBrowserCacheAndCookies(getApplication(), true)
    }

    // -------------------------------------------------------------
    // UI Panels / Modals Controls
    // -------------------------------------------------------------
    fun setTabSwitcherOpen(open: Boolean) { _isTabSwitcherOpen.value = open }
    fun setDownloadCenterOpen(open: Boolean) { _isDownloadCenterOpen.value = open }
    fun setShieldDashboardOpen(open: Boolean) { _isShieldDashboardOpen.value = open }
    fun setBookmarksHistoryOpen(open: Boolean) { _isBookmarksHistoryOpen.value = open }
    fun setSettingsOpen(open: Boolean) { _isSettingsOpen.value = open }
    fun setSearchEngine(engine: SearchEngine) { _searchEngine.value = engine }
    fun toggleDarkTheme() { _isDarkTheme.value = !_isDarkTheme.value }

    fun playMediaPreview(title: String) {
        _currentlyPlayingMedia.value = title
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun stopMediaPreview() {
        _currentlyPlayingMedia.value = null
        _isPlaying.value = false
    }

    // -------------------------------------------------------------
    // Google Search Suggestion Predictions
    // -------------------------------------------------------------
    fun onOmniboxQueryChange(query: String) {
        suggestJob?.cancel()
        if (query.trim().length < 2) {
            _searchSuggestions.value = emptyList()
            return
        }
        suggestJob = viewModelScope.launch {
            delay(150) // Debounce typing
            
            // Local history & pattern matching for autocomplete
            val localHistory = history.value
            val historyMatches = localHistory
                .map { it.url.replace("https://", "").replace("http://", "").removeSuffix("/") }
                .filter { it.contains(query, ignoreCase = true) }
                .distinct()
                .map { "https://$it" }
                .take(3)
                
            val webPatterns = if (!query.contains(" ") && !query.contains(".")) {
                listOf("https://www.${query.trim()}.com", "https://${query.trim()}.org")
            } else emptyList()
            
            // Remote fallback
            val predictions = if (!query.startsWith("http://") && !query.startsWith("https://")) {
                SearchSuggestClient.fetchGoogleSuggestions(query).take(4)
            } else emptyList()

            _searchSuggestions.value = (historyMatches + webPatterns + predictions).distinct()
        }
    }

    fun clearSearchSuggestions() {
        _searchSuggestions.value = emptyList()
    }

    // -------------------------------------------------------------
    // Find In Page Actions
    // -------------------------------------------------------------
    fun openFindInPage() {
        _findInPageState.value = FindInPageState(isActive = true)
    }

    fun closeFindInPage() {
        _findInPageState.value = FindInPageState(isActive = false, query = "", currentMatchIndex = 0, totalMatches = 0)
    }

    fun setFindQuery(query: String) {
        _findInPageState.update { it.copy(query = query) }
    }

    fun findNext() {
        _findForward.value = true
        _findActionTrigger.update { it + 1 }
    }

    fun findPrevious() {
        _findForward.value = false
        _findActionTrigger.update { it + 1 }
    }

    fun onFindMatchCounted(activeMatchIndex: Int, totalMatches: Int) {
        _findInPageState.update {
            it.copy(
                currentMatchIndex = activeMatchIndex,
                totalMatches = totalMatches
            )
        }
    }

    // -------------------------------------------------------------
    // Reader Mode Actions
    // -------------------------------------------------------------
    fun openReaderMode() {
        val tab = activeTab.value
        if (tab.url == "about:home" || tab.url == "about:blank") return

        if (_readerArticle.value == null || _readerArticle.value?.sourceUrl != tab.url) {
            _readerArticle.value = ReaderModeExtractor.parseArticle(
                pageTitle = tab.title,
                pageUrl = tab.url,
                rawPageText = _latestPageText.value
            )
        }
        _isReaderModeOpen.value = true
    }

    fun closeReaderMode() {
        _isReaderModeOpen.value = false
    }

    fun onReaderContentExtracted(jsonPayload: String) {
        val tab = activeTab.value
        _readerArticle.value = ReaderModeExtractor.parseArticle(
            pageTitle = tab.title,
            pageUrl = tab.url,
            rawPageText = _latestPageText.value,
            jsonPayload = jsonPayload
        )
    }

    // -------------------------------------------------------------
    // Encrypted Local Auto-Fill Vault Actions
    // -------------------------------------------------------------
    fun setAutoFillModalOpen(open: Boolean) {
        _isAutoFillModalOpen.value = open
    }

    fun saveAutoFillCredential(domain: String, siteName: String, user: String, pass: String) {
        viewModelScope.launch {
            repository.saveCredential(domain, siteName, user, pass)
            _activeAutoFillPrompt.value = null
        }
    }

    fun deleteAutoFillCredential(id: Long) {
        viewModelScope.launch {
            repository.deleteCredential(id)
        }
    }

    fun clearAllAutoFillCredentials() {
        viewModelScope.launch {
            repository.clearCredentials()
        }
    }

    fun onLoginFormDetected(domain: String, username: String, pass: String) {
        viewModelScope.launch {
            val existing = repository.getCredentialsForDomain(domain)
            if (existing.isNotEmpty()) {
                _activeAutoFillPrompt.value = existing.first()
            }
        }
    }

    fun triggerAutoFill(username: String, pass: String) {
        _autoFillPayload.value = Pair(username, pass)
        _activeAutoFillPrompt.value = null
    }

    fun dismissAutoFillPrompt() {
        _activeAutoFillPrompt.value = null
    }

    // Initial Seed Data
    private fun createDefaultBookmarks(): List<Bookmark> = listOf(
        Bookmark(title = "WitAnime", url = "https://witanime.pics"),
        Bookmark(title = "YouTube", url = "https://m.youtube.com"),
        Bookmark(title = "Facebook", url = "https://m.facebook.com"),
        Bookmark(title = "Kooora", url = "https://m.kooora.com"),
        Bookmark(title = "Wikipedia", url = "https://en.m.wikipedia.org"),
        Bookmark(title = "Internet Archive", url = "https://archive.org/details/movies"),
        Bookmark(title = "Wikimedia Commons", url = "https://commons.wikimedia.org"),
        Bookmark(title = "LibriVox Audiobooks", url = "https://librivox.org")
    )

    private fun createDefaultHistory(): List<HistoryItem> = listOf(
        HistoryItem(title = "Internet Archive: NASA Space Archives", url = "https://archive.org/details/nasa-jwst-deep-field", timestamp = System.currentTimeMillis() - 1000 * 60 * 30),
        HistoryItem(title = "LibriVox: The Art of War", url = "https://librivox.org/the-art-of-war-by-sun-tzu/", timestamp = System.currentTimeMillis() - 1000 * 60 * 90),
        HistoryItem(title = "Wikipedia: Privacy-enhancing technologies", url = "https://en.wikipedia.org/wiki/Privacy-enhancing_technologies", timestamp = System.currentTimeMillis() - 1000 * 60 * 180)
    )

    private fun createInitialAiMessages(): List<AegisAiMessage> = listOf(
        AegisAiMessage(
            role = MessageRole.ASSISTANT,
            content = "Welcome to Aegis AI Assistant powered by **Gemini 3.1 Pro (High Thinking Mode)**. \n\nI can deeply analyze visited pages, deconstruct detected video/audio streams, verify copyright & Creative Commons licensing, or solve complex multi-step reasoning tasks.",
            thinkingTrace = "Initialized Aegis High-Thinking Engine. Ready to ingest viewport DOM, media extractors, and privacy security vectors.",
            isThinkingExpanded = false,
            taskType = AiTaskType.DEEP_REASONING
        )
    )
}

    private fun hashUrl(url: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(url.toByteArray())
            .take(8)
            .joinToString("") { "%02x".format(it) }
    }
