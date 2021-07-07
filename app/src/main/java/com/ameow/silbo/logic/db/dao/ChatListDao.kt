package com.ameow.silbo.logic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ameow.silbo.logic.db.entity.ChatList

@Dao
interface ChatListDao {
    @Query("SELECT * FROM ChatList ORDER BY lastTime DESC")
    suspend fun getChatList(): List<ChatList>

    @Query("SELECT * FROM ChatList WHERE fromId = :fromId")
    suspend fun getChatListByFromId(fromId: String): ChatList

    @Insert
    suspend fun insertChatList(chatlist: ChatList)

    @Update
    suspend fun updateChatList(chatlist: ChatList)
}