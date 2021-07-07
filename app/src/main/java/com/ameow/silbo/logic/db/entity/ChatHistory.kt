package com.ameow.silbo.logic.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatHistory(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "fromId") val fromId: String,
    @ColumnInfo(name = "timestamp") val timestamp: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "type") val type: Int
)
