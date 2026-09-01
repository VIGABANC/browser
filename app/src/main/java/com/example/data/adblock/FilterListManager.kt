package com.example.data.adblock

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class FilterSubscription(
    val id: String,
    val name: String,
    val description: String,
    val url: String,
    val isEnabled: Boolean = true,
    val ruleCount: Int = 0,
    val lastUpdated: Long = 0L
)

object FilterListManager {
    private const val TAG = "AegisFilterList"
    private const val FILTER_DIR = "adblock_filters"

    private val DEFAULT_SUBSCRIPTIONS = listOf(
        FilterSubscription(
            id = "easylist",
            name = "EasyList",
            description = "Primary filter list blocking advertisements, sponsored banners, and video ads.",
            url = "https://easylist.to/easylist/easylist.txt",
            isEnabled = true
        ),
        FilterSubscription(
            id = "easyprivacy",
            name = "EasyPrivacy",
            description = "Strict tracking protection blocking telemetry beacons, fingerprinting, and analytics scripts.",
            url = "https://easylist.to/easylist/easyprivacy.txt",
            isEnabled = true
        ),
        FilterSubscription(
            id = "peterlowe",
            name = "Peter Lowe's Ad & Tracking Server List",
            description = "Curated list of known ad servers, malvertising hosts, and tracking networks.",
            url = "https://pgl.yoyo.org/adservers/serverlist.php?hostformat=nohtml&showintro=0&mimetype=plaintext",
            isEnabled = true
        ),
        FilterSubscription(
            id = "fanboy_annoyance",
            name = "Fanboy's Annoyance & Cookie Popups",
            description = "Blocks cookie consent banners, push notification prompts, and in-page overlays.",
            url = "https://easylist.to/easylist/fanboy-annoyance.txt",
            isEnabled = true
        )
    )

    private val _subscriptions = MutableStateFlow<List<FilterSubscription>>(DEFAULT_SUBSCRIPTIONS)
    val subscriptions: StateFlow<List<FilterSubscription>> = _subscriptions.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _totalRulesLoaded = MutableStateFlow(0)
    val totalRulesLoaded: StateFlow<Int> = _totalRulesLoaded.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    fun initialize(context: Context) {
        val filterDir = File(context.filesDir, FILTER_DIR)
        if (!filterDir.exists()) {
            filterDir.mkdirs()
        }

        // Load cached rules from disk or bundled defaults
        loadStoredRules(context)
    }

    fun toggleSubscription(context: Context, id: String, enabled: Boolean) {
        _subscriptions.value = _subscriptions.value.map {
            if (it.id == id) it.copy(isEnabled = enabled) else it
        }
        loadStoredRules(context)
    }

    suspend fun updateAllFilters(context: Context): Boolean = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        var anySuccess = false
        val filterDir = File(context.filesDir, FILTER_DIR).apply { mkdirs() }

        val updatedList = _subscriptions.value.map { sub ->
            if (!sub.isEnabled) return@map sub

            try {
                Log.d(TAG, "Fetching filter list: ${sub.name} from ${sub.url}")
                val content = downloadFilterContent(sub.url)
                if (content.isNotBlank()) {
                    val targetFile = File(filterDir, "${sub.id}.txt")
                    targetFile.writeText(content)
                    val ruleCount = countValidRules(content)
                    anySuccess = true
                    sub.copy(ruleCount = ruleCount, lastUpdated = System.currentTimeMillis())
                } else {
                    sub
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to download filter list ${sub.name}: ${e.message}")
                sub
            }
        }

        _subscriptions.value = updatedList
        _lastSyncTimestamp.value = System.currentTimeMillis()
        _isSyncing.value = false

        // Reload into AdBlockManager
        loadStoredRules(context)
        anySuccess
    }

    private fun loadStoredRules(context: Context) {
        val filterDir = File(context.filesDir, FILTER_DIR)
        val loadedPatterns = mutableSetOf<String>()
        val cosmeticSelectors = mutableSetOf<String>()

        _subscriptions.value.forEach { sub ->
            if (sub.isEnabled) {
                val file = File(filterDir, "${sub.id}.txt")
                if (file.exists()) {
                    try {
                        file.bufferedReader().useLines { lines ->
                            lines.forEach { line ->
                                parseFilterLine(line, loadedPatterns, cosmeticSelectors)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading filter file ${file.name}", e)
                    }
                }
            }
        }

        // Merge with built-in patterns
        AdBlockManager.loadDynamicRules(loadedPatterns, cosmeticSelectors)
        _totalRulesLoaded.value = AdBlockManager.getTotalRuleCount()
    }

    private fun parseFilterLine(
        rawLine: String,
        outPatterns: MutableSet<String>,
        outCosmetic: MutableSet<String>
    ) {
        val line = rawLine.trim()
        if (line.isEmpty() || line.startsWith("!") || line.startsWith("[Adblock")) {
            return // Comment or header
        }

        if (line.contains("##")) {
            val selector = line.substringAfter("##").trim()
            if (selector.isNotEmpty() && selector.length < 100) {
                outCosmetic.add(selector)
            }
            return
        }

        if (line.startsWith("||")) {
            // Standard Adblock domain rule: ||domain.com^
            val clean = line.removePrefix("||").removeSuffix("^").substringBefore("$").trim()
            if (clean.isNotEmpty() && clean.length > 3) {
                outPatterns.add(clean.lowercase())
            }
        } else if (!line.startsWith("@@") && !line.startsWith("#")) {
            // Plain hostname or keyword pattern
            val clean = line.substringBefore("$").removePrefix("|").removeSuffix("|").removeSuffix("^").trim()
            if (clean.isNotEmpty() && clean.length > 3 && !clean.contains(" ")) {
                outPatterns.add(clean.lowercase())
            }
        }
    }

    private fun countValidRules(content: String): Int {
        return content.lineSequence()
            .map { it.trim() }
            .count { it.isNotEmpty() && !it.startsWith("!") && !it.startsWith("[") }
    }

    private fun downloadFilterContent(urlStr: String): String {
        val url = URL(urlStr)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("User-Agent", "AegisBrowser/2.4 (Android; Privacy Shield Engine)")
            instanceFollowRedirects = true
        }

        return try {
            if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                ""
            }
        } finally {
            connection.disconnect()
        }
    }
}
