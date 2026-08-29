package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.HistoryItem

@Entity(tableName = "browsing_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSecure: Boolean = true
) {
    fun toModel(): HistoryItem = HistoryItem(
        id = id.toString(),
        title = title,
        url = url,
        timestamp = timestamp,
        isSecure = isSecure
    )
}
