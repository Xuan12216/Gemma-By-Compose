package com.xuan.gemma.model

import com.google.mediapipe.tasks.genai.llminference.LlmInference.Backend
import com.xuan.gemma.data.stateObject.GemmaUiState
import com.xuan.gemma.data.stateObject.UiState

// NB: Make sure the filename is *unique* per model you use!
// Weight caching is currently based on filename alone.
enum class Model(
    val path: String,
    val url: String,
    val licenseUrl: String,
    val needsAuth: Boolean,
    val preferredBackend: Backend?,
    val uiState: UiState,
    val temperature: Float,
    val topK: Int,
    val topP: Float,
) {
    GEMMA3_CPU(
        path = "/data/local/tmp/gemma3-1b-it-int4.task",
        url = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task",
        licenseUrl = "https://huggingface.co/litert-community/Gemma3-1B-IT",
        needsAuth = true,
        preferredBackend = Backend.CPU,
        uiState = GemmaUiState(),
        temperature = 1f,
        topK = 64,
        topP = 0.95f
    ),
}
