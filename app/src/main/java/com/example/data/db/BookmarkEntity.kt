package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val favicon: String? = null,
    val folder: String = "Main",
    val createdAt: Long = System.currentTimeMillis()
)
