package com.xuan.gemma.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.xuan.gemma.R
import com.xuan.gemma.data.ChatMessage
import com.xuan.gemma.data.stateObject.MODEL_PREFIX
import com.xuan.gemma.data.stateObject.USER_PREFIX
import com.xuan.gemma.data.stateObject.UiState
import com.xuan.gemma.database.Message
import com.xuan.gemma.database.MessageRepository
import com.xuan.gemma.model.InferenceModel
import com.xuan.gemma.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

class ChatViewModel(
    private var inferenceModel: InferenceModel,
    private val appContext: Context
) : ViewModel() {

    // `GemmaUiState()` is optimized for the Gemma model.
    // Replace `GemmaUiState` with `ChatUiState()` if you're using a different model
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(inferenceModel.uiState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _tokensRemaining = MutableStateFlow(-1)
    val tokensRemaining: StateFlow<Int> = _tokensRemaining.asStateFlow()

    private val _textInputEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isTextInputEnabled: StateFlow<Boolean> = _textInputEnabled.asStateFlow()

    private val repository = MessageRepository(appContext)

    var selectedMessage by mutableStateOf<Message?>(null)

    //===========

    var userMessage by mutableStateOf("")

    var tempImageUriList = mutableStateListOf<Uri>()
    var deleteUriList = mutableStateListOf<Uri>()
    var filteredUriList = mutableStateListOf<Uri>()

    var flexItem = mutableStateListOf<String>()

    var openBottomSheet by mutableStateOf(false)

    val options: List<Triple<Int, String, Int>> = listOf(
        Triple(R.drawable.baseline_camera_alt_24, appContext.getString(R.string.useCameraPickImage), 1),
        Triple(R.drawable.baseline_image_24, appContext.getString(R.string.useGalleryPickImage), 2)
    )

    //=====

    init {
        recomputeSizeInTokens("")
        refreshFlexItems()
    }

    fun updateUserMessage(message: String) {
        userMessage = message
    }

    fun addTempImageUri(uri: Uri) {
        tempImageUriList.add(uri)
        updateFilteredUriList()
    }

    fun removeTempImageUri(uri: Uri) {
        tempImageUriList.remove(uri)
    }

    fun addDeleteUri(uri: Uri) {
        deleteUriList.add(uri)
        updateFilteredUriList()
    }

    fun removeDeleteUri(uri: Uri) {
        deleteUriList.remove(uri)
    }

    private fun updateFilteredUriList() {
        val currentTempImageUriList = tempImageUriList
        val currentDeleteUriList = deleteUriList

        filteredUriList.clear()
        filteredUriList.addAll(
            currentTempImageUriList.filter { uri ->
                !currentDeleteUriList.contains(uri)
            }
        )
    }

    fun clearTempAndDeleteUriLists() {
        tempImageUriList.clear()
        deleteUriList.clear()
    }

    fun toggleBottomSheet(show: Boolean) {
        openBottomSheet = show
    }

    fun refreshFlexItems() {
        val flexboxItemArray = appContext.resources.getStringArray(R.array.flexboxItem)
        val randomIndices = flexboxItemArray.indices.shuffled().take(5)
        flexItem.clear()
        flexItem.addAll(randomIndices.map { flexboxItemArray[it] })
    }

    fun resetInferenceModel(newModel: InferenceModel) {
        inferenceModel = newModel
        _uiState.value = inferenceModel.uiState
    }

    //==========

    fun sendMessage(id: String, type: String, userMessage: String, imageUris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value.addMessage(userMessage,imageUris, USER_PREFIX)
            setInputEnabled(false)
            updateFilteredUriList()

            var currentMessageId: String? = _uiState.value.createLoadingMessage()
            try {
                //val fullPrompt = _uiState.value.fullPrompt
                val asyncInference = inferenceModel.generateResponseAsync(userMessage) { partialResult, done ->
                    currentMessageId?.let { _uiState.value.appendMessage(it, partialResult, done) }
                    if (done) {
                        insertChatMessage(id, type, uiState.value.messages)
                        currentMessageId = null
                        filteredUriList.clear()
                        setInputEnabled(true)// Re-enable text input
                    }
                    else {
                        // Reduce current token count (estimate only). sizeInTokens() will be used
                        // when computation is done
                        _tokensRemaining.update { max(0, it - 1) }
                    }
                }
                // Once the inference is done, recompute the remaining size in tokens
                asyncInference.addListener({
                    viewModelScope.launch(Dispatchers.IO) {
                        recomputeSizeInTokens(userMessage)
                    }
                }, Dispatchers.Main.asExecutor())
            }
            catch (e: Exception) {
                _uiState.value.addMessage(e.localizedMessage ?: "Unknown Error", emptyList(), MODEL_PREFIX)
                setInputEnabled(true)
            }

        }
    }

    fun addMessage(userMessage: String, imageUris: List<Uri>, userOrModel: String) {
        _uiState.value.addMessage(userMessage,imageUris, userOrModel)
    }

    private fun insertChatMessage(id: String, type: String, chatMessage: List<ChatMessage>) {
        viewModelScope.launch {
            val message = Message(
                id = id,
                messages = chatMessage,
                title = selectedMessage?.title.takeIf { !it.isNullOrBlank() }
                    ?: AppUtils.getMessageToTitle(chatMessage.last().message),
                type = type,
                date = AppUtils.getCurrentDateTime(),
                isPinned = selectedMessage?.isPinned ?: false
            )
            repository.insertOrUpdateMessage(message)
            selectedMessage = null
        }
    }

    fun clearMessages(id: String = "") {
        inferenceModel.resetUiState(id)
        _uiState.value = inferenceModel.uiState // or ChatUiState() if you're using a different model
        recomputeSizeInTokens("")
        selectedMessage = null
    }

    private fun setInputEnabled(isEnabled: Boolean) {
        _textInputEnabled.value = isEnabled
    }

    fun recomputeSizeInTokens(message: String) {
        val remainingTokens = inferenceModel.estimateTokensRemaining(message)
        _tokensRemaining.value = remainingTokens
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val inferenceModel = InferenceModel.getInstance(context)
                return ChatViewModel(inferenceModel, context) as T
            }
        }
    }
}
