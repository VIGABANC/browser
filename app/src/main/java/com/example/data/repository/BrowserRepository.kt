package com.example.data.repository

import com.example.data.local.dao.AutoFillDao
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.entity.AutoFillEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.model.AutoFillCredential
import com.example.data.model.Bookmark
import com.example.data.model.HistoryItem
import com.example.data.security.AegisCryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserRepository(
    private val historyDao: HistoryDao,
    private val bookmarkDao: BookmarkDao,
    private val autoFillDao: AutoFillDao? = null
) {
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory().map { list ->
        list.map { it.toModel() }
    }

    val allBookmarks: Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks().map { list ->
        list.map { it.toModel() }
    }

    val allCredentials: Flow<List<AutoFillCredential>> = (autoFillDao?.getAllCredentials() ?: kotlinx.coroutines.flow.flowOf(emptyList())).map { list ->
        list.map { entity ->
            val decrypted = if (entity.encryptedPassword.startsWith("v2:")) {
                AegisCryptoManager.decrypt(entity.encryptedPassword)
            } else {
                AegisCryptoManager.decryptLegacy(entity.encryptedPassword, entity.iv)
            }
            AutoFillCredential(
                id = entity.id,
                domain = entity.domain,
                siteTitle = entity.siteTitle,
                username = entity.username,
                decryptedPassword = decrypted,
                isDecrypted = true,
                createdAt = entity.createdAt
            )
        }
    }

    suspend fun saveCredential(
        domain: String,
        siteTitle: String,
        username: String,
        plainPassword: String
    ): Long {
        if (autoFillDao == null || domain.isBlank() || username.isBlank() || plainPassword.isBlank()) return -1L
        val cleanDomain = domain.removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore("/")
        val cipherPayload = AegisCryptoManager.encrypt(plainPassword)
        return autoFillDao.insertCredential(
            AutoFillEntity(
                domain = cleanDomain,
                siteTitle = siteTitle.ifBlank { cleanDomain },
                username = username,
                encryptedPassword = cipherPayload,
                iv = "v2",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getCredentialsForDomain(domain: String): List<AutoFillCredential> {
        if (autoFillDao == null) return emptyList()
        val cleanDomain = domain.removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore("/")
        val entities = autoFillDao.getCredentialsForDomain(cleanDomain)
        return entities.map { entity ->
            val decrypted = if (entity.encryptedPassword.startsWith("v2:")) {
                AegisCryptoManager.decrypt(entity.encryptedPassword)
            } else {
                val legacyDec = AegisCryptoManager.decryptLegacy(entity.encryptedPassword, entity.iv)
                // If legacy decryption succeeded, re-encrypt to v2
                if (legacyDec.isNotBlank()) {
                    val newCipher = AegisCryptoManager.encrypt(legacyDec)
                    autoFillDao.insertCredential(entity.copy(encryptedPassword = newCipher, iv = "v2"))
                }
                legacyDec
            }
            AutoFillCredential(
                id = entity.id,
                domain = entity.domain,
                siteTitle = entity.siteTitle,
                username = entity.username,
                decryptedPassword = decrypted,
                isDecrypted = true,
                createdAt = entity.createdAt
            )
        }
    }

    suspend fun deleteCredential(id: Long) {
        autoFillDao?.deleteById(id)
    }

    suspend fun clearCredentials() {
        autoFillDao?.clearAll()
    }

    suspend fun recordHistory(title: String, url: String, isSecure: Boolean = true) {
        if (url.isBlank() || url == "about:blank" || url == "about:home") return
        historyDao.insertHistory(
            HistoryEntity(
                title = title.ifBlank { url },
                url = url,
                timestamp = System.currentTimeMillis(),
                isSecure = isSecure
            )
        )
    }

    suspend fun deleteHistory(idOrUrl: String) {
        val idLong = idOrUrl.toLongOrNull()
        if (idLong != null) {
            historyDao.deleteHistoryById(idLong)
        } else {
            historyDao.deleteHistoryByUrl(idOrUrl)
        }
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    suspend fun addBookmark(title: String, url: String, folder: String = "Main"): Long {
        if (url.isBlank() || url == "about:blank" || url == "about:home") return -1L
        return bookmarkDao.insertBookmark(
            BookmarkEntity(
                title = title.ifBlank { url },
                url = url,
                folder = folder,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteBookmark(idOrUrl: String) {
        val idLong = idOrUrl.toLongOrNull()
        if (idLong != null) {
            bookmarkDao.deleteBookmarkById(idLong)
        } else {
            bookmarkDao.deleteBookmarkByUrl(idOrUrl)
        }
    }

    suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.isBookmarked(url) > 0
    }

    suspend fun clearBookmarks() {
        bookmarkDao.clearAllBookmarks()
    }
}

