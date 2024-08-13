package com.xuan.gemma.database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessageRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val messageDao = database.messageDao()

    suspend fun insertOrUpdateMessage(message: Message): Long {
        return withContext(Dispatchers.IO) {
            messageDao.insertOrUpdateMessage(message)
        }
    }

    suspend fun getMessageById(id: String): Message? {
        return withContext(Dispatchers.IO) {
            messageDao.getMessageById(id)
        }
    }

    suspend fun getAllMessages(): List<Message> {
        return withContext(Dispatchers.IO) {
            messageDao.getAllMessages()
        }
    }
}