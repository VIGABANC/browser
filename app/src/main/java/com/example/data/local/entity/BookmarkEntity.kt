package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Bookmark

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val folder: String = "Main",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toModel(): Bookmark = Bookmark(
        id = id.toString(),
        title = title,
        url = url,
        folder = folder,
        createdAt = createdAt
    )
}
