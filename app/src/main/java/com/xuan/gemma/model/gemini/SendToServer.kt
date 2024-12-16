package com.xuan.gemma.model.gemini

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.ai.client.generativeai.java.ChatFutures
import com.google.ai.client.generativeai.java.GenerativeModelFutures
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class SendToServer {
    private var modelVision: GenerativeModelFutures? = null
    private var chatNormal: ChatFutures? = null
    private val context: Context
    private val lifecycle: Lifecycle

    constructor(modelVision: GenerativeModelFutures?, context: Context, lifecycle: Lifecycle) {
        this.modelVision = modelVision
        this.context = context
        this.lifecycle = lifecycle
    }

    constructor(chatNormal: ChatFutures?, context: Context, lifecycle: Lifecycle) {
        this.chatNormal = chatNormal
        this.context = context
        this.lifecycle = lifecycle
    }

    suspend fun useStreamSendToServer(isVision: Boolean, contentUser: Content?, callback: (String?, Boolean) -> Unit) {
        val response = if (isVision) modelVision!!.generateContentStream(contentUser!!) else chatNormal!!.sendMessageStream(contentUser!!)
        val fullResponse = arrayOf<String?>("")
        val single = Single.create { _: SingleEmitter<GenerateContentResponse?>? ->
            response.subscribe(object :
                Subscriber<GenerateContentResponse> {
                override fun onNext(generateContentResponse: GenerateContentResponse) {
                    val chunk = generateContentResponse.text
                    fullResponse[0] += chunk
                    callback.invoke(chunk, false)
                }

                override fun onComplete() {
                    callback.invoke("", true)
                }

                override fun onError(t: Throwable) {
                    t.printStackTrace()
                    callback.invoke(t.toString(), true)
                }

                override fun onSubscribe(s: Subscription) {
                    s.request(Long.MAX_VALUE)
                }
            })
        }

        withContext(Dispatchers.Main) {
            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle)))
                .subscribe({ }, { error -> error.printStackTrace() })
        }
    }

    interface ResultCallback {
        fun onResult(result: String?, isFinish: Boolean)
    }
}
