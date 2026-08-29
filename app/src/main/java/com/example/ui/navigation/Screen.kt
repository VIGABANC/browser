package com.example.ui.navigation

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object NewTab : Screen("newtab")
    object Web : Screen("web/{url}") {
        fun createRoute(url: String): String {
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.name())
            return "web/$encodedUrl"
        }
        fun parseUrl(encodedUrl: String?): String {
            if (encodedUrl.isNullOrEmpty()) return "about:home"
            return try {
                URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.name())
            } catch (e: Exception) {
                encodedUrl
            }
        }
    }
    object Settings : Screen("settings")
    object History : Screen("history")
    object Bookmarks : Screen("bookmarks")
    object Downloads : Screen("downloads")
    object Reader : Screen("reader/{url}") {
        fun createRoute(url: String): String {
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.name())
            return "reader/$encodedUrl"
        }
        fun parseUrl(encodedUrl: String?): String {
            if (encodedUrl.isNullOrEmpty()) return ""
            return try {
                URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.name())
            } catch (e: Exception) {
                encodedUrl
            }
        }
    }
    object MediaGrabber : Screen("media_grabber")
}
