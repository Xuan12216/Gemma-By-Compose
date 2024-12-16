package com.xuan.gemma.data

import android.net.Uri
import com.xuan.gemma.data.stateObject.USER_PREFIX
import java.util.UUID

/**
 * Used to represent a ChatMessage
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val rawMessage: String = "",
    val author: String,
    val isLoading: Boolean = false,
    val uris: List<Uri> = emptyList()
) {
    val isFromUser: Boolean
        get() = author == USER_PREFIX
    val message: String
        get() = rawMessage.trim()
}
