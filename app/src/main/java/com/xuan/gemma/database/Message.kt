package com.xuan.gemma.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xuan.gemma.data.ChatMessage
import com.xuan.gemma.util.converters.Converters

@Entity(tableName = "messages")
@TypeConverters(Converters::class)
data class Message(
    @PrimaryKey val id: String,
    val messages: List<ChatMessage>,
    val title: String,
    val type: String,
    val date: String,
    val isPinned: Boolean
)