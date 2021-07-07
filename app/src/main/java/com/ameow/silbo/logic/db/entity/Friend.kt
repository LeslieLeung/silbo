package com.ameow.silbo.logic.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Friend(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "user_id") val userId: String, // 对方id
    @ColumnInfo(name = "secret") val secret: String // 协商的密钥
)
