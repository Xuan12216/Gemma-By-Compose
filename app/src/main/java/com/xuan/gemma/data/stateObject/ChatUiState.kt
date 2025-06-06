package com.xuan.gemma.data.stateObject

import android.net.Uri
import androidx.compose.runtime.toMutableStateList
import com.xuan.gemma.data.ChatMessage
import java.util.UUID

const val USER_PREFIX = "user"
const val MODEL_PREFIX = "model"

interface UiState {
    val id: String
    val messages: List<ChatMessage>
    val fullPrompt: String

    /**
     * Creates a new loading message.
     * Returns the id of that message.
     */
    fun createLoadingMessage(): String

    /**
     * Appends the specified text to the message with the specified ID.
     * @param done - indicates whether the model has finished generating the message.
     */
    fun appendMessage(id: String, text: String, done: Boolean = false)

    /**
     * Creates a new message with the specified text and author.
     * Return the id of that message.
     */
    fun addMessage(text: String, imageUris: List<Uri>, author: String): String

    /** Clear all messages. */
    fun clearMessages()

    /** Formats a messages from the user into the prompt format of the model. */
    fun formatPrompt(text:String) : String
}

/**
 * An implementation of [UiState] to be used with the Gemma model.
 */
class GemmaUiState(
    newId: String = "",
    messages: List<ChatMessage> = emptyList(),
    override val id: String = newId.ifEmpty { UUID.randomUUID().toString() }
) : UiState{
    private val START_TURN = "<start_of_turn>"
    private val END_TURN = "<end_of_turn>"
    private val lock = Any()

    private val _messages: MutableList<ChatMessage> = messages.toMutableStateList()
    override val messages: List<ChatMessage>
        get() = synchronized(lock) {
            _messages. apply{
                for (i in indices) {
                    this[i] = this[i].copy(
                        rawMessage = this[i].rawMessage.replace(START_TURN + this[i].author + "\n", "")
                            .replace(END_TURN, "")
                    )
                }
            }.asReversed()

        }

    // Only using the last 4 messages to keep input + output short
    override val fullPrompt: String
        get() = _messages.takeLast(4).joinToString(separator = "\n") { it.rawMessage }

    val fullPromptUris: List<Uri>
        get() = _messages.takeLast(4).flatMap { it.uris }

    override fun createLoadingMessage(): String {
        val chatMessage = ChatMessage(author = MODEL_PREFIX, isLoading = true)
        _messages.add(chatMessage)
        return chatMessage.id
    }

    fun appendFirstMessage(id: String, text: String) {
        appendMessage(id, "$START_TURN$MODEL_PREFIX\n$text", false)
    }

    override fun appendMessage(id: String, text: String, done: Boolean) {
        val index = _messages.indexOfFirst { it.id == id }
        if (index != -1) {
            val newText = if (done) {
                // Append the Suffix when model is done generating the response
                _messages[index].rawMessage + text + END_TURN
            } else {
                // Append the text
                _messages[index].rawMessage + text
            }
            _messages[index] = _messages[index].copy(rawMessage = newText, isLoading = false)
        }
    }

    override fun addMessage(text: String, imageUris: List<Uri>, author: String): String {
        val chatMessage = ChatMessage(
            rawMessage = "$START_TURN$author\n$text$END_TURN",
            uris = ArrayList(imageUris),
            author = author
        )
        _messages.add(chatMessage)
        return chatMessage.id
    }

    override fun clearMessages() {
        _messages.clear()
    }

    override fun formatPrompt(text: String): String {
        return text
    }
}
