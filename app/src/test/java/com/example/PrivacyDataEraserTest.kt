package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AegisDatabase
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.security.PrivacyDataEraser
import com.example.data.security.PrivacyEraseOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrivacyDataEraserTest {

    @Test
    fun `eraseData clears history and cookies successfully`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AegisDatabase.getDatabase(context)

        // Seed data
        db.historyDao().insertHistory(
            HistoryEntity(
                title = "Test Visit",
                url = "https://example.com",
                timestamp = System.currentTimeMillis()
            )
        )
        db.bookmarkDao().insertBookmark(
            BookmarkEntity(
                title = "My Bookmark",
                url = "https://example.com/bookmark"
            )
        )

        assertEquals(1, db.historyDao().getAllHistory().first().size)
        assertEquals(1, db.bookmarkDao().getAllBookmarks().first().size)

        // Erase only history, leave bookmarks intact
        val summary = PrivacyDataEraser.eraseData(
            context,
            PrivacyEraseOptions(
                clearHistory = true,
                clearCookies = true,
                clearCache = true,
                clearWebStorage = true,
                clearBookmarks = false
            )
        )

        assertTrue(summary.isHistoryCleared)
        assertTrue(summary.isCookiesCleared)
        assertEquals(0, db.historyDao().getAllHistory().first().size)
        // Bookmarks should be preserved
        assertEquals(1, db.bookmarkDao().getAllBookmarks().first().size)
    }
}
