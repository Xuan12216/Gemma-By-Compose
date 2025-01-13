package com.xuan.gemma.model

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.xuan.gemma.R
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File

class InferenceModel private constructor(private val context: Context) {
    private var llmInference: LlmInference

    private val _partialResults = MutableSharedFlow<Pair<String, Boolean>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val partialResults: SharedFlow<Pair<String, Boolean>> = _partialResults.asSharedFlow()

    init {
        val modelFile = findModelFile(context.cacheDir)
            ?: throw IllegalArgumentException("Model not found at path: ${context.cacheDir}/$MODEL_NAME")

        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelFile.absolutePath) // 使用找到的文件路径
            .setMaxTokens(2048)
            .setResultListener { partialResult, done ->
                _partialResults.tryEmit(partialResult to done)
            }
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun generateResponseAsync(prompt: String) {
        try {
            // Add the gemma prompt prefix to trigger the response.
            val prefix = context.getString(R.string.please_ans_with_specified_language)
            val gemmaPrompt = "$prefix $prompt<start_of_turn>model\n"
            llmInference.generateResponseAsync(gemmaPrompt)
        }
        catch (e: Exception) { e.printStackTrace() }
    }

    companion object {
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
