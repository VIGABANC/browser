with open('app/src/main/java/com/example/AegisApplication.kt', 'r') as f:
    text = f.read()

import_target = "import java.io.File"
import_replacement = "import java.io.File\nimport android.content.ComponentCallbacks2\nimport android.content.res.Configuration\nimport com.example.data.downloader.DownloadManager"

if "import android.content.ComponentCallbacks2" not in text:
    text = text.replace(import_target, import_replacement)

trim_target = """    private fun initializeCacheDirs() {"""
trim_replacement = """    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // Trim memory when UI is hidden
            System.gc()
        }
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            // Emergency cleanup
            try {
                DownloadManager.getInstance(this).pauseAll()
                System.gc()
            } catch (e: Exception) {}
        }
    }

    private fun initializeCacheDirs() {"""

if "override fun onTrimMemory" not in text:
    text = text.replace(trim_target, trim_replacement)

with open('app/src/main/java/com/example/AegisApplication.kt', 'w') as f:
    f.write(text)
