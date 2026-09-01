package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.AiTaskType
import com.example.data.model.DownloadStatus
import com.example.data.model.ReaderArticle
import com.example.navigation.BrowserNavigation
import com.example.ui.components.ActiveDownloadBottomBar
import com.example.ui.components.AutoFillPromptBanner
import com.example.ui.components.BottomToolbar
import com.example.ui.components.BrowserHomeDashboard
import com.example.ui.components.BrowserWebView
import com.example.ui.components.FeatureNavigationStrip
import com.example.ui.components.FindInPageBar
import com.example.ui.components.MediaGrabberBottomSheet
import com.example.ui.components.OmniboxBar
import com.example.ui.components.OpenTabsStrip
import com.example.ui.components.SafeModeConsentDialog
import com.example.ui.components.WebpageAiFloatingActionButton
import com.example.ui.navigation.AegisRoutes
import com.example.ui.navigation.Screen
import com.example.ui.pages.AiAssistantPage
import com.example.ui.pages.AutoFillVaultPage
import com.example.ui.pages.BookmarksHistoryPage
import com.example.ui.pages.DownloadsPage
import com.example.ui.pages.ReaderModePage
import com.example.ui.pages.SettingsPage
import com.example.ui.pages.ShieldDashboardPage
import com.example.ui.pages.TabManagerPage
import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.launch

@Composable
fun BrowserMainScreen(
    viewModel: BrowserViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe ViewModel States
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val shieldStats by viewModel.shieldStats.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val downloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
    val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
    val findInPageState by viewModel.findInPageState.collectAsStateWithLifecycle()
    val findActionTrigger by viewModel.findActionTrigger.collectAsStateWithLifecycle()
    val findForward by viewModel.findForward.collectAsStateWithLifecycle()
    val readerArticle by viewModel.readerArticle.collectAsStateWithLifecycle()
    val autoFillCredentials by viewModel.autoFillCredentials.collectAsStateWithLifecycle()
    val activeAutoFillPrompt by viewModel.activeAutoFillPrompt.collectAsStateWithLifecycle()
    val autoFillPayload by viewModel.autoFillPayload.collectAsStateWithLifecycle()
    val isMediaGrabberOpen by viewModel.isMediaGrabberOpen.collectAsStateWithLifecycle()
    val selectedMediaForGrabber by viewModel.selectedMediaForGrabber.collectAsStateWithLifecycle()
    val isAttestationDialogOpen by viewModel.isAttestationDialogOpen.collectAsStateWithLifecycle()
    val isScenicWallpaper by viewModel.isScenicWallpaper.collectAsStateWithLifecycle()
    val isPrivacyStatsVisible by viewModel.isPrivacyStatsVisible.collectAsStateWithLifecycle()
    val isDiscoverFeedVisible by viewModel.isDiscoverFeedVisible.collectAsStateWithLifecycle()
    val searchEngine by viewModel.searchEngine.collectAsStateWithLifecycle()
    val userAgentMode by viewModel.userAgentMode.collectAsStateWithLifecycle()
    val isIncognitoSession by viewModel.isIncognitoSession.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val safeModeState by viewModel.safeModeState.collectAsStateWithLifecycle()
    val currentlyPlayingMedia by viewModel.currentlyPlayingMedia.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val selectedAiTask by viewModel.selectedAiTask.collectAsStateWithLifecycle()
    val webGoBackTrigger by viewModel.webGoBackTrigger.collectAsStateWithLifecycle()

    // Strip states
    val tabStripState by viewModel.tabStripState.collectAsStateWithLifecycle()
    val featureStripVisible by viewModel.featureStripVisible.collectAsStateWithLifecycle()
    val featureChips by viewModel.featureChips.collectAsStateWithLifecycle()
    val activeFeatureId by viewModel.activeFeatureId.collectAsStateWithLifecycle()

    // Overflow Menu Open state (default false)
    var isOverflowMenuOpen by remember { mutableStateOf(false) }

    var activeWebView by remember { mutableStateOf<android.webkit.WebView?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val activeDownloadCount = remember(downloads) {
        downloads.count {
            it.status == DownloadStatus.DOWNLOADING ||
                    it.status == DownloadStatus.CONVERTING_AUDIO ||
                    it.status == DownloadStatus.QUEUED
        }
    }

    // Helper to navigate to a web URL using launchSingleTop = true
    val navigateToWeb = { targetUrl: String ->
        viewModel.clearSearchSuggestions()
        viewModel.loadUrlOrQuery(targetUrl)
        if (targetUrl == "about:home") {
            navController.navigate(Screen.NewTab.route) {
                launchSingleTop = true
            }
        } else {
            val route = Screen.Web.createRoute(targetUrl)
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    // Omnibox visibility logic: Visible on web and newtab, hidden on settings/history/bookmarks/downloads etc.
    val isOmniboxVisible = currentRoute == Screen.NewTab.route ||
            (currentRoute != null && currentRoute.startsWith("web"))

    // BackHandler prioritizing: 1. Overlays (Overflow Menu, Media Grabber, etc.), 2. WebView back history, 3. Compose Backstack, 4. Exit App
    BackHandler {
        when {
            isOverflowMenuOpen -> isOverflowMenuOpen = false
            findInPageState.isActive -> viewModel.closeFindInPage()
            isMediaGrabberOpen -> viewModel.dismissMediaGrabber()
            isAttestationDialogOpen -> viewModel.dismissAttestationDialog()
            activeTab.canGoBack && currentRoute?.startsWith("web") == true -> {
                viewModel.triggerWebGoBack()
            }
            currentRoute?.startsWith("web") == true && activeTab.url != "about:home" -> {
                viewModel.loadUrlOrQuery("about:home")
                navController.navigate(Screen.NewTab.route) {
                    popUpTo(Screen.NewTab.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            navController.previousBackStackEntry != null -> {
                navController.popBackStack()
            }
            else -> {
                (context as? Activity)?.finish()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isOmniboxVisible) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OmniboxBar(
                        tab = activeTab,
                        blockedCount = activeTab.blockedAdsCount + activeTab.blockedTrackersCount,
                        suggestions = searchSuggestions,
                        onQueryChange = { viewModel.onOmniboxQueryChange(it) },
                        onNavigate = { navigateToWeb(it) },
                        onReload = {
                            if (activeTab.url != "about:home") {
                                viewModel.updateActiveTab { it.copy(url = activeTab.url, isLoading = true) }
                            }
                        },
                        onShieldClick = {
                            navController.navigate(AegisRoutes.SHIELDS) { launchSingleTop = true }
                        },
                        onMediaSnifferClick = {
                            viewModel.openMediaGrabber()
                            navController.navigate(Screen.MediaGrabber.route) { launchSingleTop = true }
                        },
                        onAiClick = {
                            viewModel.openAiAssistant(AiTaskType.DEEP_REASONING)
                            navController.navigate(AegisRoutes.AI_ASSISTANT) { launchSingleTop = true }
                        },
                        onReaderModeClick = {
                            viewModel.openReaderMode()
                            navController.navigate(Screen.Reader.createRoute(activeTab.url)) { launchSingleTop = true }
                        },
                        onFindInPageClick = { viewModel.openFindInPage() },
                        onAutoFillClick = {
                            navController.navigate(AegisRoutes.AUTOFILL) { launchSingleTop = true }
                        },
                        onToggleMenu = { isOverflowMenuOpen = !isOverflowMenuOpen }
                    )

                    if (findInPageState.isActive) {
                        FindInPageBar(
                            query = findInPageState.query,
                            currentMatchIndex = findInPageState.currentMatchIndex,
                            totalMatches = findInPageState.totalMatches,
                            onQueryChange = { viewModel.setFindQuery(it) },
                            onFindNext = { viewModel.findNext() },
                            onFindPrevious = { viewModel.findPrevious() },
                            onClose = { viewModel.closeFindInPage() }
                        )
                    }

                    // FEATURE STRIP (like reference screenshots — dismissible)
                    if (featureStripVisible) {
                        FeatureNavigationStrip(
                            features = featureChips,
                            activeFeatureId = activeFeatureId,
                            onFeatureClick = { chip ->
                                viewModel.selectFeatureChip(chip.id)
                                when (chip.id) {
                                    "adblock" -> navController.navigate(AegisRoutes.SHIELDS) { launchSingleTop = true }
                                    "sniffer" -> {
                                        viewModel.openMediaGrabber()
                                        navController.navigate(Screen.MediaGrabber.route) { launchSingleTop = true }
                                    }
                                    "gemini" -> {
                                        viewModel.openAiAssistant(AiTaskType.DEEP_REASONING)
                                        navController.navigate(AegisRoutes.AI_ASSISTANT) { launchSingleTop = true }
                                    }
                                    "history" -> navController.navigate(Screen.Bookmarks.route) { launchSingleTop = true }
                                    "autofill" -> navController.navigate(AegisRoutes.AUTOFILL) { launchSingleTop = true }
                                    "reader" -> {
                                        viewModel.openReaderMode()
                                        navController.navigate(Screen.Reader.createRoute(activeTab.url)) { launchSingleTop = true }
                                    }
                                }
                            },
                            onDismiss = { viewModel.dismissFeatureStrip() }
                        )
                    } else if (tabStripState.isVisible) {
                        // OPEN TABS STRIP (when multiple tabs open and feature strip dismissed)
                        OpenTabsStrip(
                            tabs = tabs,
                            activeTabId = activeTabId,
                            onTabClick = { tab ->
                                viewModel.switchTab(tab.id)
                                if (tab.url == "about:home") {
                                    navController.navigate(Screen.NewTab.route) { launchSingleTop = true }
                                } else {
                                    navController.navigate(Screen.Web.createRoute(tab.url)) { launchSingleTop = true }
                                }
                            },
                            onTabClose = { tab -> viewModel.closeTab(tab.id) },
                            onNewTab = {
                                viewModel.addNewTab("about:home")
                                navController.navigate(Screen.NewTab.route) { launchSingleTop = true }
                            },
                            onDismiss = { viewModel.dismissTabStrip() }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                activeAutoFillPrompt?.let { promptCred ->
                    AutoFillPromptBanner(
                        domain = promptCred.domain,
                        credential = promptCred,
                        onAutoFill = { viewModel.triggerAutoFill(promptCred.username, promptCred.decryptedPassword) },
                        onDismiss = { viewModel.dismissAutoFillPrompt() }
                    )
                }

                ActiveDownloadBottomBar(
                    activeDownloads = downloads,
                    onPauseDownload = { viewModel.downloadManager.pauseDownload(it) },
                    onResumeDownload = { viewModel.downloadManager.resumeDownload(it) },
                    onCancelDownload = { viewModel.downloadManager.cancelDownload(it) },
                    onOpenDownloadCenter = {
                        navController.navigate(Screen.Downloads.route) { launchSingleTop = true }
                    }
                )

                BottomToolbar(
                    tabCount = tabs.size,
                    canGoBack = activeTab.canGoBack && currentRoute?.startsWith("web") == true,
                    canGoForward = false,
                    isMenuOpen = isOverflowMenuOpen,
                    onGoBack = {
                        if (activeTab.canGoBack && currentRoute?.startsWith("web") == true) {
                            viewModel.triggerWebGoBack()
                        } else if (currentRoute?.startsWith("web") == true && activeTab.url != "about:home") {
                            viewModel.loadUrlOrQuery("about:home")
                            navController.navigate(Screen.NewTab.route) {
                                popUpTo(Screen.NewTab.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        }
                    },
                    onGoForward = { /* Reserved for web forward */ },
                    onGoHome = {
                        viewModel.loadUrlOrQuery("about:home")
                        navController.navigate(Screen.NewTab.route) {
                            popUpTo(Screen.NewTab.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onOpenTabs = {
                        navController.navigate(AegisRoutes.TABS) { launchSingleTop = true }
                    },
                    onToggleMenu = {
                        isOverflowMenuOpen = !isOverflowMenuOpen
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BrowserNavigation(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding),
            snackbarHostState = snackbarHostState,
            onNavigateToWeb = { url -> navigateToWeb(url) }
        )
    }

    // Aegis Overflow Menu Overlay (AEGIS-UI-001 Reference Screen)
    if (isOverflowMenuOpen) {
        com.example.ui.components.BrowserOverflowMenuSheet(
            isSafeModeEnabled = safeModeState.isSafeModeActive,
            isDesktopMode = activeTab.isDesktopMode,
            onDismiss = { isOverflowMenuOpen = false },
            onNewTab = { isIncognito ->
                viewModel.addNewTab("about:home", isIncognito)
                navController.navigate(Screen.NewTab.route) { launchSingleTop = true }
            },
            onOpenHistory = {
                navController.navigate(Screen.Bookmarks.route) { launchSingleTop = true }
            },
            onOpenBookmarks = {
                navController.navigate(Screen.Bookmarks.route) { launchSingleTop = true }
            },
            onOpenDownloads = {
                navController.navigate(Screen.Downloads.route) { launchSingleTop = true }
            },
            onDownloadDetectedMedia = {
                viewModel.openMediaGrabber()
                navController.navigate(Screen.MediaGrabber.route) { launchSingleTop = true }
            },
            onOpenDownloadQueue = {
                navController.navigate(Screen.Downloads.route) { launchSingleTop = true }
            },
            onToggleSafeMode = { enabled ->
                viewModel.toggleSafeMode(enabled)
                scope.launch {
                    snackbarHostState.showSnackbar(if (enabled) "Safe Mode activé" else "Safe Mode désactivé")
                }
            },
            onOpenSitePermissions = {
                navController.navigate(AegisRoutes.SHIELDS) { launchSingleTop = true }
            },
            onOpenPrivacySettings = {
                navController.navigate(AegisRoutes.SHIELDS) { launchSingleTop = true }
            },
            onOpenSettings = {
                navController.navigate(Screen.Settings.route) { launchSingleTop = true }
            },
            onOpenHelp = {
                scope.launch {
                    snackbarHostState.showSnackbar("Aegis Browser — Navigation privée et sécurisée")
                }
            },
            onScreenshot = {
                coroutineScope.launch {
                    com.example.ui.utils.ScreenshotManager.captureAndShare(context, activeWebView)
                }
            },
            onToggleReaderMode = {
                val readerScript = """
                    (function() {
                        const article = document.querySelector('article') || document.body;
                        document.body.innerHTML = '';
                        document.body.appendChild(article);
                        document.querySelectorAll('header, footer, nav, aside, iframe, .ad, [id*="ad"], [class*="ad"]').forEach(el => el.remove());
                        document.body.style.padding = '5%';
                        document.body.style.fontFamily = 'sans-serif';
                        document.body.style.fontSize = '1.2rem';
                        document.body.style.lineHeight = '1.6';
                        document.body.style.color = '#222';
                        document.body.style.backgroundColor = '#FAFAFA';
                    })();
                """.trimIndent()
                activeWebView?.evaluateJavascript(readerScript, null)
            },
            onSummarizePage = {
                viewModel.summarizeCurrentPage()
            },
            onClearCacheAndCookies = {
                viewModel.clearBrowserCacheAndCookies(context, false)
                scope.launch {
                    snackbarHostState.showSnackbar("Cache et cookies supprimés avec succès")
                }
            }
        )
    }

    // Overlay Media Grabber Bottom Sheet if opened in-place
    if (isMediaGrabberOpen && currentRoute != Screen.MediaGrabber.route) {
        MediaGrabberBottomSheet(
            detectedMediaList = activeTab.detectedMediaList,
            onDismiss = { viewModel.dismissMediaGrabber() },
            onDownloadSelected = { selectedMedia ->
                viewModel.requestBatchDownload(selectedMedia)
            },
            onPreviewMedia = { /* Basic preview stub */ }
        )
    }

    // Safe Mode Consent & Attestation Dialog
    if (isAttestationDialogOpen) {
        SafeModeConsentDialog(
            onAcceptAndAttest = {
                viewModel.attestRightsAndAccept()
                selectedMediaForGrabber?.let { media ->
                    media.formats.firstOrNull()?.let { fmt ->
                        viewModel.requestDownload(fmt)
                    }
                }
            },
            onDismiss = { viewModel.dismissAttestationDialog() }
        )
    }
}
