package com.ameow.silbo.logic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ameow.silbo.logic.db.entity.ChatHistory

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM ChatHistory")
    suspend fun getAll(): List<ChatHistory>

    @Query("SELECT * FROM ChatHistory WHERE fromId = :fromId")
    suspend fun getChatHistoryByFromId(fromId: String): List<ChatHistory>

    @Insert
    suspend fun insertChatHistory(history: ChatHistory)
}