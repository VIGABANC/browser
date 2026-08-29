package com.example

import android.app.Application
import android.os.Build
import android.webkit.WebView
import java.io.File
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import com.example.data.downloader.DownloadQueueManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import com.example.data.local.AegisDatabase
import com.example.data.adblock.AdBlockManager

class AegisApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // In Android 9+ (Pie), if running in a non-default process, set a unique suffix
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val currentProcess = getProcessName()
                if (packageName != currentProcess) {
                    WebView.setDataDirectorySuffix(currentProcess)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 1. Pre-warm WebView on background thread
        GlobalScope.launch(Dispatchers.IO) {
            try {
                WebView(this@AegisApplication).destroy()
            } catch (e: Exception) { /* ignore */ }
        }

        // 2. Initialize cache dirs synchronously
        initializeCacheDirs()

        // 3. Pre-load adblock rules on IO thread
        GlobalScope.launch(Dispatchers.IO) {
            AdBlockManager.warmup()
        }

        // 4. Initialize Room database on IO thread
        GlobalScope.launch(Dispatchers.IO) {
            AegisDatabase.getDatabase(this@AegisApplication)
        }
        
        // 5. Verify Attestation Log Integrity
        GlobalScope.launch(Dispatchers.IO) {
            val am = com.example.data.security.AttestationManager(this@AegisApplication)
            if (!am.verifyLogIntegrity()) {
                // Revert to safe mode if log is tampered
                am.revertToSafeMode()
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // Trim memory when UI is hidden
            System.gc()
        }
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            // Emergency cleanup
            try {
                DownloadQueueManager.getInstance(this).pauseAll()
                System.gc()
            } catch (e: Exception) {}
        }
    }

    private fun initializeCacheDirs() {
        // Ensure all dirs exist before WebView needs them
        listOf(cacheDir, filesDir, 
            File(cacheDir, "webview_cache"),
            File(filesDir, "app_webview"),
            File(filesDir, "ytdlp"),
            File(filesDir, "downloads"),
            File(filesDir, "attestation")
        ).forEach { it.mkdirs() }
    }
}
