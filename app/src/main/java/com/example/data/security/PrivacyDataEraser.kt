package com.example.data.security

import android.content.Context
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.WebStorage
import android.webkit.WebViewDatabase
import com.example.data.local.AegisDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class PrivacyEraseOptions(
    val clearHistory: Boolean = true,
    val clearCookies: Boolean = true,
    val clearCache: Boolean = true,
    val clearWebStorage: Boolean = true,
    val clearCredentials: Boolean = false,
    val clearBookmarks: Boolean = false
)

data class PrivacyEraseSummary(
    val isHistoryCleared: Boolean,
    val isCookiesCleared: Boolean,
    val isCacheCleared: Boolean,
    val isWebStorageCleared: Boolean,
    val isCredentialsCleared: Boolean,
    val isBookmarksCleared: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

object PrivacyDataEraser {

    suspend fun eraseData(
        context: Context,
        options: PrivacyEraseOptions = PrivacyEraseOptions()
    ): PrivacyEraseSummary = withContext(Dispatchers.IO) {
        var historyCleared = false
        var cookiesCleared = false
        var cacheCleared = false
        var webStorageCleared = false
        var credentialsCleared = false
        var bookmarksCleared = false

        val database = AegisDatabase.getDatabase(context)

        // 1. History
        if (options.clearHistory) {
            try {
                database.historyDao().clearAllHistory()
                historyCleared = true
            } catch (_: Exception) {}
        }

        // 2. Credentials
        if (options.clearCredentials) {
            try {
                database.autoFillDao().clearAll()
                credentialsCleared = true
            } catch (_: Exception) {}
        }

        // 3. Bookmarks
        if (options.clearBookmarks) {
            try {
                database.bookmarkDao().clearAllBookmarks()
                bookmarksCleared = true
            } catch (_: Exception) {}
        }

        // 4. Cookies
        if (options.clearCookies) {
            try {
                withContext(Dispatchers.Main) {
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.removeAllCookies(null)
                    cookieManager.removeSessionCookies(null)
                    cookieManager.flush()
                }
                cookiesCleared = true
            } catch (_: Exception) {}
        }

        // 5. WebStorage & Geolocation & Form Data
        if (options.clearWebStorage) {
            try {
                withContext(Dispatchers.Main) {
                    WebStorage.getInstance().deleteAllData()
                    GeolocationPermissions.getInstance().clearAll()
                    val webViewDb = WebViewDatabase.getInstance(context)
                    webViewDb.clearHttpAuthUsernamePassword()
                    @Suppress("DEPRECATION")
                    webViewDb.clearFormData()
                }
                webStorageCleared = true
            } catch (_: Exception) {}
        }

        // 6. Disk & Memory Cache
        if (options.clearCache) {
            try {
                val cacheDir = context.cacheDir
                val webviewCache = File(cacheDir, "webview_cache")
                if (webviewCache.exists()) {
                    webviewCache.deleteRecursively()
                }
                File(context.filesDir, "app_webview").listFiles()?.forEach { file ->
                    if (file.name.contains("Cache") || file.name.contains("GPUCache")) {
                        file.deleteRecursively()
                    }
                }
                cacheCleared = true
            } catch (_: Exception) {}
        }

        PrivacyEraseSummary(
            isHistoryCleared = historyCleared,
            isCookiesCleared = cookiesCleared,
            isCacheCleared = cacheCleared,
            isWebStorageCleared = webStorageCleared,
            isCredentialsCleared = credentialsCleared,
            isBookmarksCleared = bookmarksCleared
        )
    }
}
