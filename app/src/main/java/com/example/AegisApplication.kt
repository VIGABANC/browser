package com.example

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.Build
import android.webkit.WebView
import com.example.data.adblock.AdBlockManager
import com.example.data.downloader.DownloadQueueManager
import com.example.data.local.AegisDatabase
import com.example.data.security.AttestationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class AegisApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        // 1. Initialize cache and storage dirs synchronously
        initializeCacheDirs()

        // 2. Pre-load adblock rules on IO thread
        applicationScope.launch {
            AdBlockManager.warmup()
        }

        // 3. Initialize Room database on IO thread
        applicationScope.launch {
            AegisDatabase.getDatabase(this@AegisApplication)
        }
        
        // 4. Verify Attestation Log Integrity
        applicationScope.launch {
            val am = AttestationManager(this@AegisApplication)
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
        if (level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
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
