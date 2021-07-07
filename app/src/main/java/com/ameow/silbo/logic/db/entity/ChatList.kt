package com.ameow.silbo.logic.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatList(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "fromId") val fromId: String,
    @ColumnInfo(name = "lastMsg") val lastMsg: String,
    @ColumnInfo(name = "lastTime") val lastTime: Int,
    @ColumnInfo(name = "isTop") val isTop: Boolean
)
