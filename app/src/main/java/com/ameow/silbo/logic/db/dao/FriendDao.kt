package com.ameow.silbo.logic.db.dao

import androidx.room.*
import com.ameow.silbo.logic.db.entity.Friend

@Dao
interface FriendDao {

    @Query("SELECT secret FROM Friend WHERE user_id = :userId")
    suspend fun getSecretByUserId(userId: String): String?

    @Insert
    suspend fun addFriend(friend: Friend)

    @Update()
    suspend fun updateFriend(friend: Friend)

    @Delete
    suspend fun deleteFriend(friend: Friend)
}