package com.xuan.gemma.util.converters

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.xuan.gemma.data.ChatMessage

class Converters {

    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
        .create()

    @TypeConverter
    fun fromChatMessageList(value: List<ChatMessage>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toChatMessageList(value: String): List<ChatMessage> {
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return gson.fromJson(value, type)
    }
}