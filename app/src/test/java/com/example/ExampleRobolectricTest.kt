package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.adblock.AdBlockManager
import com.example.data.downloader.MediaExtractorEngine
import com.example.data.downloader.NetworkTrafficMonitor
import com.example.data.local.AegisDatabase
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.model.UserAgentMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AegisDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AegisDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Aegis Browser", appName)
  }

  @Test
  fun `verify adblock detector blocks known tracker domains`() {
    assertTrue(AdBlockManager.isAdOrTracker("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"))
    assertTrue(AdBlockManager.isAdOrTracker("https://connect.facebook.net/en_US/fbevents.js"))
    assertTrue(AdBlockManager.isAdOrTracker("https://www.google-analytics.com/analytics.js"))
  }

  @Test
  fun `verify media extractor engine produces multiple stream formats`() {
    val videoFormats = MediaExtractorEngine.generateAvailableFormats("Test Video", "https://example.com/video.mp4")
    assertTrue(videoFormats.isNotEmpty())
    assertEquals("mp4", videoFormats.first().container)
    assertFalse(videoFormats.first().isAudioOnly)

    val audioFormats = MediaExtractorEngine.generateAvailableFormats("Test Audio", "https://example.com/audio.mp3")
    assertTrue(audioFormats.isNotEmpty())
    assertEquals("mp3", audioFormats.first().container)
    assertTrue(audioFormats.first().isAudioOnly)
  }

  @Test
  fun `verify network traffic monitor identifies audio and video streams`() {
    val videoMedia = NetworkTrafficMonitor.inspectUrl(
      url = "https://cdn.example.com/media/sample_clip.mp4?token=123",
      pageTitle = "Sample Page",
      pageUrl = "https://example.com"
    )
    assertNotNull(videoMedia)
    assertEquals("video/mp4", videoMedia?.mimeType)

    val audioMedia = NetworkTrafficMonitor.inspectUrl(
      url = "https://cdn.example.com/podcasts/episode_42.mp3",
      pageTitle = "Podcast Page",
      pageUrl = "https://example.com/podcast"
    )
    assertNotNull(audioMedia)
    assertEquals("audio/mp3", audioMedia?.mimeType)
    assertTrue(audioMedia?.formats?.any { it.isAudioOnly } == true)
  }

  @Test
  fun `verify user agent modes provide custom strings for desktop bypass`() {
    val desktopChrome = UserAgentMode.DESKTOP_CHROME
    assertNotNull(desktopChrome.customString)
    assertTrue(desktopChrome.customString!!.contains("Windows NT 10.0"))

    val safariMac = UserAgentMode.DESKTOP_MAC
    assertNotNull(safariMac.customString)
    assertTrue(safariMac.customString!!.contains("Macintosh"))

    val ipad = UserAgentMode.IPAD_TABLET
    assertNotNull(ipad.customString)
    assertTrue(ipad.customString!!.contains("iPad"))
  }

  @Test
  fun `verify room history and bookmark dao operations`() = runBlocking {
    val historyDao = db.historyDao()
    val bookmarkDao = db.bookmarkDao()

    historyDao.insertHistory(
      HistoryEntity(
        title = "Open Source Space",
        url = "https://archive.org/space",
        timestamp = System.currentTimeMillis(),
        isSecure = true
      )
    )

    val historyList = historyDao.getAllHistory().first()
    assertEquals(1, historyList.size)
    assertEquals("Open Source Space", historyList[0].title)

    bookmarkDao.insertBookmark(
      BookmarkEntity(
        title = "Wikipedia Home",
        url = "https://en.wikipedia.org"
      )
    )

    val bookmarks = bookmarkDao.getAllBookmarks().first()
    assertEquals(1, bookmarks.size)
    assertEquals("Wikipedia Home", bookmarks[0].title)

    assertTrue(bookmarkDao.isBookmarked("https://en.wikipedia.org") > 0)
    assertEquals(0, bookmarkDao.isBookmarked("https://nonexistent.org"))
  }

  @Test
  fun `verify AttestationManager records HMAC signed consent log`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val attestationManager = com.example.data.security.AttestationManager(context)

    assertTrue(attestationManager.isSafeMode)
    assertFalse(attestationManager.isExtendedMode)

    val success = attestationManager.attestExtendedMode("device_fingerprint_test_123")
    assertTrue(success)
    assertTrue(attestationManager.isExtendedMode)
    assertFalse(attestationManager.isSafeMode)

    assertTrue(attestationManager.verifyLogIntegrity())
  }

  @Test
  fun `verify SafeModeFilter allows whitelisted public domain sources`() {
    val filter = com.example.data.security.SafeModeFilter()

    assertTrue(filter.isSafeSource("https://archive.org/download/sample/sample.mp4"))
    assertTrue(filter.isSafeSource("https://commons.wikimedia.org/wiki/File:Sample.ogg"))
    assertFalse(filter.isSafeSource("https://instagram.com/reel/12345"))
    assertEquals("Internet Archive", filter.categorizeSource("https://archive.org/details/test"))
  }

  @Test
  fun `verify HlsManifestParser parses master playlist variants`() {
    val parser = com.example.data.downloader.HlsManifestParser()
    val sampleManifest = """
      #EXTM3U
      #EXT-X-STREAM-INF:BANDWIDTH=5000000,RESOLUTION=1920x1080,CODECS="avc1.640028,mp4a.40.2"
      1080p.m3u8
      #EXT-X-STREAM-INF:BANDWIDTH=2500000,RESOLUTION=1280x720,CODECS="avc1.4d401f,mp4a.40.2"
      720p.m3u8
    """.trimIndent()

    val playlist = parser.parseMasterPlaylistContent("https://example.com/master.m3u8", sampleManifest)
    assertEquals(2, playlist.variants.size)
    assertEquals("1080p", playlist.variants[0].qualityLabel)
    assertEquals("https://example.com/1080p.m3u8", playlist.variants[0].url)
  }
}
