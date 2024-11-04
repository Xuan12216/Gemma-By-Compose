package com.xuan.gemma.model.gemini

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.Lifecycle
import com.google.ai.client.generativeai.java.ChatFutures
import com.google.ai.client.generativeai.java.GenerativeModelFutures
import com.google.ai.client.generativeai.type.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Arrays

class GeminiContentBuilder(
    private val imageUris: List<Uri>,
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val generativeModelManager : GenerativeModelManager
) {
    private var chatNormal: ChatFutures? = null
    private var historyNormal: MutableList<Content> = ArrayList()

    init {
        historyNormal = Arrays.asList(
            generativeModelManager.getUserContent(),
            generativeModelManager.getModelContent()
        )
    }

    suspend fun startGeminiBuilder(text: String, isVision: Boolean, callback: GeminiBuilderCallback) {
        val model: GenerativeModelFutures? = generativeModelManager.getGenerativeModel()

        if (model == null) {
            callback.callBackResult(null, true)
            return
        }

        val builder: Content.Builder = Content.Builder()
        builder.role = "user"
        builder.text(text)

        if (isVision) {
            for (uri in imageUris) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    checkNotNull(inputStream)
                    withContext(Dispatchers.IO) {
                        inputStream.close()
                    }
                    builder.image(bitmap)
                }
                catch (e: IOException) { throw RuntimeException(e) }
            }
        }
        else if (null == chatNormal) { chatNormal = model.startChat(historyNormal) }

        val contentUser: Content = builder.build()

        val sendToServer: SendToServer = if (isVision) SendToServer(model, context, lifecycle) else SendToServer(chatNormal, context, lifecycle)
        sendToServer.useStreamSendToServer(isVision, contentUser) { text: String?, isFinish: Boolean -> callback.callBackResult(text, isFinish) }
    }

    //=================

    interface GeminiBuilderCallback {
        fun callBackResult(text: String?, isFinish: Boolean)
    }
}
