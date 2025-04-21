package com.xuan.gemma.model

import android.content.Context
import android.util.Log
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession.LlmInferenceSessionOptions
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import com.xuan.gemma.R
import com.xuan.gemma.data.stateObject.UiState
import java.io.File
import kotlin.math.max

/** The maximum number of tokens the model can process. */
var MAX_TOKENS = 8192

/**
 * An offset in tokens that we use to ensure that the model always has the ability to respond when
 * we compute the remaining context length.
 */
var DECODE_TOKEN_OFFSET = 256

class ModelLoadFailException :
    Exception("Failed to load model, please try again")

class ModelSessionCreateFailException :
    Exception("Failed to create model session, please try again")

class InferenceModel private constructor(private val context: Context) {
    private lateinit var llmInference: LlmInference
    private lateinit var llmInferenceSession: LlmInferenceSession
    private val TAG = InferenceModel::class.qualifiedName

    val uiState: UiState

    init {
        val modelFile = findModelFile(context.cacheDir)
            ?: throw IllegalArgumentException("Model not found at path: ${context.cacheDir}/$MODEL_NAME")

        uiState = model.uiState
        createEngine(context, modelFile)
        createSession()
    }

    private fun createEngine(context: Context, modelFile: File) {
        val inferenceOptions = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelFile.absolutePath)
            .setMaxTokens(MAX_TOKENS)
            .apply { model.preferredBackend?.let { setPreferredBackend(it) } }
            .build()

        try {
            llmInference = LlmInference.createFromOptions(context, inferenceOptions)
        }
        catch (e: Exception) {
            Log.e(TAG, "Load model error: ${e.message}", e)
            throw ModelLoadFailException()
        }
    }

    private fun createSession() {
        val sessionOptions =  LlmInferenceSessionOptions.builder()
            .setTemperature(model.temperature)
            .setTopK(model.topK)
            .setTopP(model.topP)
            .build()

        try {
            llmInferenceSession =
                LlmInferenceSession.createFromOptions(llmInference, sessionOptions)
        }
        catch (e: Exception) {
            Log.e(TAG, "LlmInferenceSession create error: ${e.message}", e)
            throw ModelSessionCreateFailException()
        }
    }

    fun generateResponseAsync(prompt: String, progressListener: ProgressListener<String>) : ListenableFuture<String> {
        val formattedPrompt = model.uiState.formatPrompt(prompt)
        llmInferenceSession.addQueryChunk(formattedPrompt)

        val postfix = context.getString(R.string.please_ans_with_specified_language)
        var gemmaPrompt = "$prompt $postfix<start_of_turn>model\n"
        var totalTokens = llmInferenceSession.sizeInTokens(gemmaPrompt)
        val maxTokens = MAX_TOKENS / 2

        if (totalTokens > maxTokens) {
            var reducedPrompt = prompt
            reducedPrompt = reducedPrompt.replace(Regex("\\s+"), "").trim()
            while (totalTokens > maxTokens && reducedPrompt.isNotEmpty()) {
                reducedPrompt = reducedPrompt.drop(50)
                gemmaPrompt = "$reducedPrompt $postfix<start_of_turn>model\n"
                totalTokens = llmInferenceSession.sizeInTokens(gemmaPrompt)
            }
        }

        return llmInferenceSession.generateResponseAsync(progressListener)
    }

    fun estimateTokensRemaining(prompt: String): Int {
        val context = uiState.messages.joinToString { it.rawMessage } + prompt
        if (context.isEmpty()) return -1 // Specia marker if no content has been added

        val sizeOfAllMessages = llmInferenceSession.sizeInTokens(context)
        val approximateControlTokens = uiState.messages.size * 3
        val remainingTokens = MAX_TOKENS - sizeOfAllMessages - approximateControlTokens -  DECODE_TOKEN_OFFSET
        // Token size is approximate so, let's not return anything below 0
        return max(0, remainingTokens)
    }

    companion object {
        var model: Model = Model.GEMMA3_CPU
        private const val MODEL_NAME = "model"
        private var instance: InferenceModel? = null

        // 获取 InferenceModel 实例
        fun getInstance(context: Context): InferenceModel {
            return instance ?: InferenceModel(context).also { instance = it }
        }

        // 遍历缓存目录，查找与指定文件名匹配的文件（不包括扩展名）
        private fun findModelFile(directory: File): File? {
            val files = directory.listFiles() ?: return null
            return files.firstOrNull { file ->
                val nameWithoutExtension = file.name.substringBeforeLast(".")
                nameWithoutExtension == MODEL_NAME
            }
        }
    }
}
