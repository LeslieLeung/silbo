package com.ameow.silbo.logic.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ameow.silbo.logic.db.dao.ChatHistoryDao
import com.ameow.silbo.logic.db.dao.ChatListDao
import com.ameow.silbo.logic.db.dao.FriendDao
import com.ameow.silbo.logic.db.entity.ChatHistory
import com.ameow.silbo.logic.db.entity.ChatList
import com.ameow.silbo.logic.db.entity.Friend

@Database(entities = [ChatHistory::class, ChatList::class, Friend::class], version = 1)
abstract class AppDataBase : RoomDatabase(){
    abstract fun chatHistoryDao() : ChatHistoryDao
    
    abstract fun chatListDao(): ChatListDao

    abstract fun friendDao(): FriendDao
}