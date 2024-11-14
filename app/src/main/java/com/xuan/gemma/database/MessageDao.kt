package com.xuan.gemma.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMessage(message: Message): Long

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: String): Message?

    @Query("SELECT * FROM messages ORDER BY isPinned DESC, date DESC")
    suspend fun getAllMessages(): List<Message>

    @Query("SELECT * FROM messages WHERE type = :type ORDER BY isPinned DESC, date DESC")
    suspend fun getMessagesByType(type: String): List<Message>

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

}