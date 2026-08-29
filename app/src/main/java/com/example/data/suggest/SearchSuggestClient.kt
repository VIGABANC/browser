package com.example.data.suggest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object SearchSuggestClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun fetchGoogleSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        try {
            val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
            val url = "https://suggestqueries.google.com/complete/search?client=firefox&q=$encodedQuery"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:129.0) Gecko/129.0 Firefox/129.0")
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext getFallbackPredictions(trimmed)

            val bodyString = response.body?.string() ?: return@withContext getFallbackPredictions(trimmed)
            parseSuggestionsJson(bodyString, trimmed)
        } catch (e: Exception) {
            getFallbackPredictions(trimmed)
        }
    }

    private fun parseSuggestionsJson(json: String, query: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(json)
            if (array.length() >= 2) {
                val suggestionsArray = array.getJSONArray(1)
                for (i in 0 until suggestionsArray.length()) {
                    val item = suggestionsArray.getString(i)
                    if (item.isNotBlank() && !list.contains(item)) {
                        list.add(item)
                    }
                }
            }
        } catch (e: Exception) {
            return getFallbackPredictions(query)
        }
        return if (list.isEmpty()) getFallbackPredictions(query) else list.take(8)
    }

    private fun getFallbackPredictions(query: String): List<String> {
        val queryLower = query.lowercase()
        val commonQueries = listOf(
            "$query news",
            "$query tutorial",
            "$query open source",
            "$query download",
            "$query wikipedia",
            "$query documentation",
            "$query github",
            "$query audio archive",
            "$query video stream"
        )
        return commonQueries.take(5)
    }
}
