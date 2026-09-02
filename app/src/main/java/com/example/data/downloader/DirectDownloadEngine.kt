package com.example.data.downloader

import com.example.data.model.DownloadStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit
import kotlin.math.max

data class DownloadProgress(
    val status: DownloadStatus,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val progressPercent: Float,
    val speedBps: Long = 0L,
    val etaSeconds: Long? = null,
    val errorMessage: String? = null
)

class DirectDownloadEngine(
    private val client: OkHttpClient = defaultClient()
) {
    companion object {
        fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
                .build()
        }
    }

    fun downloadFile(
        url: String,
        destinationFile: File,
        startOffset: Long = 0L,
        requestHeaders: Map<String, String> = emptyMap()
    ): Flow<DownloadProgress> = flow {
        val partFile = File(destinationFile.parentFile, "${destinationFile.name}.part")
        partFile.parentFile?.mkdirs()
        
        // Use HEAD to probe for size and range support
        val headReq = Request.Builder().url(url).head()
        requestHeaders.forEach { (k, v) -> headReq.header(k, v) }
        
        var totalBytes = -1L
        var supportsRange = false
        
        try {
            val headRes = client.newCall(headReq.build()).execute()
            totalBytes = headRes.header("Content-Length")?.toLongOrNull() ?: -1L
            supportsRange = headRes.header("Accept-Ranges")?.contains("bytes") == true
            headRes.close()
        } catch (e: Exception) {
            // Ignore, try GET
        }

        if (totalBytes > 10 * 1024 * 1024 && supportsRange) {
            // Segmented Download Strategy for files > 10MB
            emitAllProgress(downloadSegmented(url, partFile, destinationFile, totalBytes, requestHeaders))
        } else {
            // Single Stream Strategy
            emitAllProgress(downloadSingleStream(url, partFile, destinationFile, startOffset, requestHeaders))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun kotlinx.coroutines.flow.FlowCollector<DownloadProgress>.emitAllProgress(flow: Flow<DownloadProgress>) {
        flow.collect { emit(it) }
    }

    private fun downloadSegmented(
        url: String,
        partFile: File,
        destinationFile: File,
        totalBytes: Long,
        headers: Map<String, String>
    ): Flow<DownloadProgress> = flow {
        val numSegments = if (totalBytes > 50 * 1024 * 1024) 4 else 2
        val segmentSize = totalBytes / numSegments
        
        // Initialize RAF with full size
        val raf = RandomAccessFile(partFile, "rw")
        raf.setLength(totalBytes)
        raf.close()

        val progressArray = LongArray(numSegments)
        var globalDownloaded = 0L
        val startTime = System.currentTimeMillis()
        var lastEmitTime = startTime
        var emaSpeed = 0L
        var lastGlobalDownloaded = 0L

        coroutineScope {
            val jobs = (0 until numSegments).map { i ->
                async(Dispatchers.IO) {
                    val start = i * segmentSize
                    val end = if (i == numSegments - 1) totalBytes - 1 else start + segmentSize - 1
                    
                    val req = Request.Builder().url(url).header("Range", "bytes=$start-$end")
                    headers.forEach { (k, v) -> req.header(k, v) }
                    
                    val res = client.newCall(req.build()).execute()
                    val body = res.body ?: throw IllegalStateException("Empty body")
                    val stream = body.byteStream()
                    
                    val fileRaf = RandomAccessFile(partFile, "rw")
                    fileRaf.seek(start)
                    
                    val buffer = ByteArray(32768) // 32KB Dynamic Chunk
                    var read = 0
                    while (isActive && stream.read(buffer).also { read = it } != -1) {
                        fileRaf.write(buffer, 0, read)
                        progressArray[i] += read
                        
                        // We do not emit progress inside the fast loop, we'll poll it
                    }
                    fileRaf.close()
                    stream.close()
                    body.close()
                }
            }
            
            // Progress Poller
            while (jobs.any { it.isActive }) {
                delay(300)
                globalDownloaded = progressArray.sum()
                val now = System.currentTimeMillis()
                val elapsed = max(1L, now - lastEmitTime)
                val bytesSinceLast = globalDownloaded - lastGlobalDownloaded
                val currentSpeed = (bytesSinceLast * 1000L) / elapsed
                
                // EMA Alpha = 0.2
                emaSpeed = if (emaSpeed == 0L) currentSpeed else (0.2 * currentSpeed + 0.8 * emaSpeed).toLong()
                
                lastEmitTime = now
                lastGlobalDownloaded = globalDownloaded
                
                val remaining = totalBytes - globalDownloaded
                val eta = if (emaSpeed > 0) remaining / emaSpeed else null
                
                emit(DownloadProgress(
                    status = DownloadStatus.DOWNLOADING,
                    downloadedBytes = globalDownloaded,
                    totalBytes = totalBytes,
                    progressPercent = globalDownloaded.toFloat() / totalBytes,
                    speedBps = emaSpeed,
                    etaSeconds = eta
                ))
            }
        }
        
        finishDownload(partFile, destinationFile, totalBytes)
    }

    private fun downloadSingleStream(
        url: String,
        partFile: File,
        destinationFile: File,
        startOffset: Long,
        headers: Map<String, String>
    ): Flow<DownloadProgress> = flow {
        var currentOffset = startOffset
        if (currentOffset > 0L && partFile.exists()) {
            currentOffset = partFile.length()
        } else {
            currentOffset = 0L
            if (partFile.exists()) partFile.delete()
        }

        val requestBuilder = Request.Builder().url(url)
        headers.forEach { (k, v) -> requestBuilder.header(k, v) }
        if (currentOffset > 0L) requestBuilder.header("Range", "bytes=$currentOffset-")

        try {
            val response = client.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            
            val body = response.body ?: throw Exception("Empty body")
            val isPartial = response.code == 206
            val effectiveTotal = if (body.contentLength() > 0L) {
                if (isPartial) currentOffset + body.contentLength() else body.contentLength()
            } else -1L

            if (!isPartial && currentOffset > 0L) {
                currentOffset = 0L
                if (partFile.exists()) partFile.delete()
            }

            val raf = RandomAccessFile(partFile, "rw")
            raf.seek(currentOffset)
            
            val stream = body.byteStream()
            val buffer = ByteArray(16384)
            var read = 0
            
            var lastEmitTime = System.currentTimeMillis()
            var bytesSinceLast = 0L
            var emaSpeed = 0L

            while (currentCoroutineContext().isActive && stream.read(buffer).also { read = it } != -1) {
                raf.write(buffer, 0, read)
                currentOffset += read
                bytesSinceLast += read
                
                val now = System.currentTimeMillis()
                val elapsed = now - lastEmitTime
                if (elapsed >= 300) {
                    val currentSpeed = (bytesSinceLast * 1000L) / elapsed
                    emaSpeed = if (emaSpeed == 0L) currentSpeed else (0.2 * currentSpeed + 0.8 * emaSpeed).toLong()
                    
                    val remaining = if (effectiveTotal > 0L) effectiveTotal - currentOffset else 0L
                    val eta = if (emaSpeed > 0 && remaining > 0) remaining / emaSpeed else null
                    
                    emit(DownloadProgress(
                        status = DownloadStatus.DOWNLOADING,
                        downloadedBytes = currentOffset,
                        totalBytes = effectiveTotal,
                        progressPercent = if (effectiveTotal > 0) currentOffset.toFloat() / effectiveTotal else -1f,
                        speedBps = emaSpeed,
                        etaSeconds = eta
                    ))
                    
                    lastEmitTime = now
                    bytesSinceLast = 0L
                }
            }
            
            raf.close()
            stream.close()
            body.close()
            
            finishDownload(partFile, destinationFile, currentOffset)
        } catch (e: Exception) {
            emit(DownloadProgress(DownloadStatus.FAILED, currentOffset, -1L, 0f, 0L, null, e.message))
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<DownloadProgress>.finishDownload(partFile: File, destinationFile: File, finalSize: Long) {
        if (destinationFile.exists()) destinationFile.delete()
        if (!partFile.renameTo(destinationFile)) {
            partFile.copyTo(destinationFile, overwrite = true)
            partFile.delete()
        }
        emit(DownloadProgress(DownloadStatus.COMPLETED, finalSize, finalSize, 1f, 0L, 0L))
    }
}
