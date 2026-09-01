package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.ui.BrowserMainScreen
import com.example.ui.theme.AegisBrowserTheme
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {
    private val browserViewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)
        setContent {
            val isDarkTheme by browserViewModel.isDarkTheme.collectAsStateWithLifecycle()
            AegisBrowserTheme(darkTheme = isDarkTheme) {
                BrowserMainScreen(viewModel = browserViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.dataString?.let { url ->
                    if (url.isNotBlank()) {
                        browserViewModel.loadUrlOrQuery(url)
                    }
                }
            }
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrBlank()) {
                        browserViewModel.loadUrlOrQuery(sharedText)
                    }
                }
            }
            Intent.ACTION_WEB_SEARCH -> {
                val query = intent.getStringExtra("query")
                if (!query.isNullOrBlank()) {
                    browserViewModel.loadUrlOrQuery(query)
                }
            }
        }
    }

    override fun onDestroy() {
        if (browserViewModel.isClearOnCloseEnabled.value) {
            browserViewModel.clearBrowserCacheAndCookies(applicationContext, false)
        }
        super.onDestroy()
    }
}
