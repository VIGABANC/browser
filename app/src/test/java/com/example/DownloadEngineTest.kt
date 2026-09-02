package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.downloader.DirectDownloadEngine
import com.example.data.downloader.DownloadProgress
import com.example.data.model.DownloadStatus
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DownloadEngineTest {

    private lateinit var server: MockWebServer
    private lateinit var context: Context
    private lateinit var tempDir: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        server = MockWebServer()
        server.start()
        tempDir = File(context.cacheDir, "test_downloads").apply { mkdirs() }
    }

    @After
    fun teardown() {
        server.shutdown()
        tempDir.deleteRecursively()
    }

    @Test
    fun `download transfers real stream and produces complete file via atomic rename`() = runBlocking {
        val testPayload = "Hello Aegis Real Download Engine! Verified stream bytes."
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Length", testPayload.toByteArray().size)
                .setHeader("Accept-Ranges", "bytes")
                .setBody(testPayload)
        )

        val destFile = File(tempDir, "test_file.txt")
        val partFile = File(tempDir, "test_file.txt.part")

        val client = OkHttpClient()
        val engine = DirectDownloadEngine(client)

        val downloadUrl = server.url("/download/test_file.txt").toString()
        val updates = mutableListOf<DownloadProgress>()

        engine.downloadFile(
            url = downloadUrl,
            destinationFile = destFile,
            startOffset = 0L
        ).collect { updates.add(it) }

        // Final state must be COMPLETED
        assertTrue(updates.isNotEmpty())
        val finalProgress = updates.last()
        assertEquals(DownloadStatus.COMPLETED, finalProgress.status)
        assertEquals(1f, finalProgress.progressPercent, 0.01f)

        // .part file should be cleanly renamed to destination file
        assertFalse(partFile.exists())
        assertTrue(destFile.exists())
        assertEquals(testPayload, destFile.readText())
    }

    @Test
    fun `download resumes cleanly using HTTP Range header`() = runBlocking {
        val fullData = "Part1Content___Part2ResumedContent"
        val part2Data = "Part2ResumedContent"

        server.enqueue(
            MockResponse()
                .setResponseCode(206)
                .setHeader("Content-Range", "bytes 15-${fullData.length - 1}/${fullData.length}")
                .setHeader("Content-Length", part2Data.toByteArray().size)
                .setBody(part2Data)
        )

        val destFile = File(tempDir, "resumed_file.txt")
        val partFile = File(tempDir, "resumed_file.txt.part")
        partFile.writeText("Part1Content___")

        val client = OkHttpClient()
        val engine = DirectDownloadEngine(client)

        val downloadUrl = server.url("/download/resumed_file.txt").toString()
        val updates = mutableListOf<DownloadProgress>()

        engine.downloadFile(
            url = downloadUrl,
            destinationFile = destFile,
            startOffset = 15L
        ).collect { updates.add(it) }

        val finalProgress = updates.last()
        assertEquals(DownloadStatus.COMPLETED, finalProgress.status)
        assertTrue(destFile.exists())
        assertEquals(fullData, destFile.readText())
    }
}
