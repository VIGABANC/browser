package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.webkit.WebView
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ScreenshotManager {
    suspend fun captureAndShare(context: Context, webView: WebView?) {
        if (webView == null) return
        withContext(Dispatchers.IO) {
            try {
                val bitmap = Bitmap.createBitmap(
                    webView.width,
                    webView.height,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                withContext(Dispatchers.Main) {
                    webView.draw(canvas)
                }

                val cachePath = File(context.cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "screenshot_${System.currentTimeMillis()}.png")
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                withContext(Dispatchers.Main) {
                    context.startActivity(Intent.createChooser(shareIntent, "Share Screenshot"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
