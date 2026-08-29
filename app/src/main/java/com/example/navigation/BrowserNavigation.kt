package com.example.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.data.model.AiTaskType
import com.example.data.model.ReaderArticle
import com.example.ui.components.BrowserHomeDashboard
import com.example.ui.components.BrowserWebView
import com.example.ui.components.MediaGrabberBottomSheet
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
fun BrowserNavigation(
    navController: NavHostController,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    onNavigateToWeb: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val activeTab by viewModel.activeTab.collectAsState()
    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val shieldStats by viewModel.shieldStats.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()
    val downloads by viewModel.activeDownloads.collectAsState()
    val findInPageState by viewModel.findInPageState.collectAsState()
    val findActionTrigger by viewModel.findActionTrigger.collectAsState()
    val findForward by viewModel.findForward.collectAsState()
    val readerArticle by viewModel.readerArticle.collectAsState()
    val autoFillCredentials by viewModel.autoFillCredentials.collectAsState()
    val autoFillPayload by viewModel.autoFillPayload.collectAsState()
    val isScenicWallpaper by viewModel.isScenicWallpaper.collectAsState()
    val isPrivacyStatsVisible by viewModel.isPrivacyStatsVisible.collectAsState()
    val isDiscoverFeedVisible by viewModel.isDiscoverFeedVisible.collectAsState()
    val searchEngine by viewModel.searchEngine.collectAsState()
    val userAgentMode by viewModel.userAgentMode.collectAsState()
    val isIncognitoSession by viewModel.isIncognitoSession.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val safeModeState by viewModel.safeModeState.collectAsState()
    val currentlyPlayingMedia by viewModel.currentlyPlayingMedia.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val aiMessages by viewModel.aiMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val selectedAiTask by viewModel.selectedAiTask.collectAsState()
    val webGoBackTrigger by viewModel.webGoBackTrigger.collectAsState()

    fun navigateToWebInternal(url: String) {
        viewModel.loadUrlOrQuery(url)
        navController.navigate(Screen.Web.createRoute(url)) {
            popUpTo(Screen.NewTab.route) { inclusive = false }
            launchSingleTop = true
        }
        onNavigateToWeb(url)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.NewTab.route,
        modifier = modifier
    ) {
        // New Tab / Speed Dial Homepage
        composable(
            route = Screen.NewTab.route,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            BrowserHomeDashboard(
                isIncognito = activeTab.isIncognito,
                shieldStats = shieldStats,
                bookmarks = bookmarks,
                recentHistory = history,
                isScenicWallpaper = isScenicWallpaper,
                isPrivacyStatsVisible = isPrivacyStatsVisible,
                isDiscoverFeedVisible = isDiscoverFeedVisible,
                onNavigate = { navigateToWebInternal(it) },
                onToggleIncognito = { viewModel.toggleIncognitoSession(!activeTab.isIncognito) },
                onToggleScenicWallpaper = { viewModel.toggleScenicWallpaper() },
                onTogglePrivacyStatsVisible = { viewModel.togglePrivacyStatsVisible() },
                onToggleDiscoverFeedVisible = { viewModel.toggleDiscoverFeedVisible() },
                onAddCustomShortcut = { title, url ->
                    viewModel.addCustomShortcut(title, url)
                    scope.launch {
                        snackbarHostState?.showSnackbar("Shortcut added: $title")
                    }
                },
                onSimulateMediaStream = { title, streamUrl, isAudio ->
                    viewModel.addManualMediaForPage(title, streamUrl, isAudio)
                    viewModel.openMediaGrabber()
                    navController.navigate(Screen.MediaGrabber.route) { launchSingleTop = true }
                },
                onOpenAiAssistant = {
                    viewModel.openAiAssistant(AiTaskType.DEEP_REASONING)
                    navController.navigate(AegisRoutes.AI_ASSISTANT) { launchSingleTop = true }
                },
                onOpenShields = {
                    navController.navigate(AegisRoutes.SHIELDS) { launchSingleTop = true }
                },
                onOpenDownloads = {
                    navController.navigate(Screen.Downloads.route) { launchSingleTop = true }
                }
            )
        }

        // Web Page Content (WebView)
        composable(
            route = Screen.Web.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType }),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) { backStackEntry ->
            val rawUrlArg = backStackEntry.arguments?.getString("url")
            val decodedUrl = Screen.Web.parseUrl(rawUrlArg)

            if (activeTab.url != decodedUrl && decodedUrl.isNotBlank() && decodedUrl != "about:home") {
                viewModel.loadUrlOrQuery(decodedUrl)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                BrowserWebView(
                    tab = activeTab,
                    isShieldEnabled = shieldStats.isShieldEnabled,
                    findQuery = findInPageState.query,
                    findActionTrigger = findActionTrigger,
                    findForward = findForward,
                    goBackTrigger = webGoBackTrigger,
                    autoFillCredential = autoFillPayload,
                    onPageStarted = { viewModel.onPageStarted(it) },
                    onPageFinished = { url, title -> viewModel.onPageFinished(url, title) },
                    onProgressChanged = { viewModel.onProgressChanged(it) },
                    onNavigationStateChanged = { back, fwd -> viewModel.updateNavigationState(back, fwd) },
                    onMediaDetected = { viewModel.onMediaDetectedFromJs(it) },
                    onNetworkMediaDetected = { viewModel.onNetworkMediaDetected(it) },
                    onPageTextExtracted = { viewModel.onPageTextExtracted(it) },
                    onReaderContentExtracted = { viewModel.onReaderContentExtracted(it) },
                    onLoginFormDetected = { domain, user, pass -> viewModel.onLoginFormDetected(domain, user, pass) },
                    onFindMatchCounted = { cur, tot -> viewModel.onFindMatchCounted(cur, tot) },
                    onBlockAd = { viewModel.incrementBlockedAds(1) },
                    onBlockTracker = { viewModel.incrementBlockedTrackers(1) },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } },
                    onOpenHistory = { navController.navigate(Screen.History.route) { launchSingleTop = true } },
                    onShowMediaGrabber = {
                        viewModel.openMediaGrabber()
                        navController.navigate(Screen.MediaGrabber.route) { launchSingleTop = true }
                    },
                    onOpenInNewTab = { newUrl ->
                        viewModel.addNewTab(newUrl)
                        navigateToWebInternal(newUrl)
                    }
                )

                WebpageAiFloatingActionButton(
                    pageTitle = activeTab.title,
                    hasDetectedMedia = activeTab.detectedMediaList.isNotEmpty(),
                    onTriggerAiTask = { taskType, prompt ->
                        viewModel.openAiAssistant(taskType, prompt)
                        navController.navigate(AegisRoutes.AI_ASSISTANT) { launchSingleTop = true }
                    },
                    onOpenMediaGrabber = {
                        viewModel.openMediaGrabber()
                        navController.navigate(Screen.MediaGrabber.route) { launchSingleTop = true }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }

        // Native Settings Screen
        composable(
            route = Screen.Settings.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) }
        ) {
            SettingsPage(
                searchEngine = searchEngine,
                userAgentMode = userAgentMode,
                isDarkTheme = isDarkTheme,
                isSafeMode = safeModeState.isSafeModeActive,
                isIncognito = isIncognitoSession,
                onSelectSearchEngine = { viewModel.setSearchEngine(it) },
                onSelectUserAgentMode = { viewModel.setUserAgentMode(it) },
                onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                onToggleSafeMode = { viewModel.toggleSafeMode(it) },
                onToggleIncognito = { viewModel.toggleIncognitoSession(it) },
                onClearAllData = {
                    viewModel.clearAllData()
                    viewModel.clearAiChat()
                    viewModel.clearAllAutoFillCredentials()
                    viewModel.downloadManager.clearCompleted()
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToShields = { navController.navigate(AegisRoutes.SHIELDS) { launchSingleTop = true } },
                onNavigateToAutoFill = { navController.navigate(AegisRoutes.AUTOFILL) { launchSingleTop = true } }
            )
        }

        // Native History Screen
        composable(
            route = Screen.History.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) }
        ) {
            BookmarksHistoryPage(
                initialTab = 1,
                currentTabUrl = activeTab.url,
                currentTabTitle = activeTab.title,
                bookmarks = bookmarks,
                history = history,
                onSelectUrl = { url -> navigateToWebInternal(url) },
                onAddBookmark = { title, url -> viewModel.addBookmark(title, url) },
                onDeleteBookmark = { viewModel.deleteBookmark(it) },
                onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                onClearHistory = { viewModel.clearHistory() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Native Bookmarks Screen
        composable(
            route = Screen.Bookmarks.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) }
        ) {
            BookmarksHistoryPage(
                initialTab = 0,
                currentTabUrl = activeTab.url,
                currentTabTitle = activeTab.title,
                bookmarks = bookmarks,
                history = history,
                onSelectUrl = { url -> navigateToWebInternal(url) },
                onAddBookmark = { title, url -> viewModel.addBookmark(title, url) },
                onDeleteBookmark = { viewModel.deleteBookmark(it) },
                onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                onClearHistory = { viewModel.clearHistory() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Native Downloads Screen
        composable(
            route = Screen.Downloads.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) }
        ) {
            DownloadsPage(
                downloads = downloads,
                currentlyPlayingTitle = currentlyPlayingMedia,
                isPlaying = isPlaying,
                onPause = { viewModel.downloadManager.pauseDownload(it) },
                onResume = { viewModel.downloadManager.resumeDownload(it) },
                onCancel = { viewModel.downloadManager.cancelDownload(it) },
                onRetry = { viewModel.downloadManager.retryDownload(it) },
                onClearCompleted = { viewModel.downloadManager.clearCompleted() },
                onPlayMedia = { viewModel.playMediaPreview(it.title) },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onStopMedia = { viewModel.stopMediaPreview() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Native Reader Mode Screen
        composable(
            route = Screen.Reader.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) }
        ) {
            val currentArticle = readerArticle ?: ReaderArticle(
                title = activeTab.title,
                byline = "Reader Mode",
                siteName = activeTab.url.removePrefix("https://").removePrefix("http://"),
                paragraphs = listOf("Extracted content for optimized reading."),
                wordCount = 100,
                estimatedReadingTimeMinutes = 3,
                rawText = activeTab.title
            )
            ReaderModePage(
                article = currentArticle,
                onOpenAiSummary = {
                    viewModel.openAiAssistant(
                        taskType = AiTaskType.PAGE_SUMMARY,
                        customPrompt = "Summarize the key points of this article: ${currentArticle.title}"
                    )
                    navController.navigate(AegisRoutes.AI_ASSISTANT) { launchSingleTop = true }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Detected Media Bottom Sheet Destination
        composable(
            route = Screen.MediaGrabber.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(250)) }
        ) {
            MediaGrabberBottomSheet(
                detectedMediaList = activeTab.detectedMediaList,
                onDismiss = {
                    viewModel.dismissMediaGrabber()
                    navController.popBackStack()
                },
                onDownloadSelected = { selectedMedia ->
                    viewModel.requestBatchDownload(selectedMedia)
                },
                onPreviewMedia = { /* Basic preview stub */ }
            )
        }

        // Tab Manager Page
        composable(
            route = AegisRoutes.TABS,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(250)) }
        ) {
            TabManagerPage(
                tabs = tabs,
                activeTabId = activeTabId,
                onSelectTab = { tabId ->
                    viewModel.switchTab(tabId)
                    navController.popBackStack()
                },
                onCloseTab = { viewModel.closeTab(it) },
                onNewTab = { isIncognito ->
                    viewModel.addNewTab("about:home", isIncognito)
                    navController.popBackStack()
                },
                onCloseAll = { viewModel.closeAllTabs() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Shield Dashboard Page
        composable(
            route = AegisRoutes.SHIELDS,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) }
        ) {
            ShieldDashboardPage(
                stats = shieldStats,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // AutoFill Vault Page
        composable(
            route = AegisRoutes.AUTOFILL,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) }
        ) {
            AutoFillVaultPage(
                credentials = autoFillCredentials,
                onSaveCredential = { dom, site, usr, pwd -> viewModel.saveAutoFillCredential(dom, site, usr, pwd) },
                onDeleteCredential = { viewModel.deleteAutoFillCredential(it) },
                onClearAll = { viewModel.clearAllAutoFillCredentials() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Gemini AI Assistant Page
        composable(
            route = AegisRoutes.AI_ASSISTANT,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(250)) }
        ) {
            AiAssistantPage(
                messages = aiMessages,
                isThinking = isAiThinking,
                selectedTask = selectedAiTask,
                onSelectTask = { viewModel.openAiAssistant(it) },
                onSubmitPrompt = { prompt, task -> viewModel.submitAiPrompt(prompt, task) },
                onToggleThinkingTrace = { viewModel.toggleThinkingTrace(it) },
                onClearChat = { viewModel.clearAiChat() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
