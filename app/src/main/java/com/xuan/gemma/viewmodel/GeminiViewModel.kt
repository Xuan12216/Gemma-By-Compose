package com.xuan.gemma.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.xuan.gemma.R
import com.xuan.gemma.data.ChatMessage
import com.xuan.gemma.data.stateObject.GemmaUiState
import com.xuan.gemma.data.stateObject.MODEL_PREFIX
import com.xuan.gemma.data.stateObject.USER_PREFIX
import com.xuan.gemma.data.stateObject.UiState
import com.xuan.gemma.database.Message
import com.xuan.gemma.database.MessageRepository
import com.xuan.gemma.model.gemini.GeminiContentBuilder
import com.xuan.gemma.model.gemini.GenerativeModelManager
import com.xuan.gemma.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeminiViewModel( private val appContext: Context ) : ViewModel() {

    // 使用 SnapshotStateList 存储消息
    private val _displayMessages = mutableStateListOf<ChatMessage>()
    val displayMessages: List<ChatMessage> = _displayMessages

    // `GemmaUiState()` is optimized for the Gemma model.
    // Replace `GemmaUiState` with `ChatUiState()` if you're using a different model
    private val _uiState: MutableStateFlow<GemmaUiState> = MutableStateFlow(GemmaUiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _textInputEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isTextInputEnabled: StateFlow<Boolean> = _textInputEnabled.asStateFlow()

    private val repository = MessageRepository(appContext)

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

    val generativeModelManager : GenerativeModelManager = GenerativeModelManager()

    //=====

    init {
        refreshFlexItems()
    }

    private fun updateDisplayMessages(messages: List<ChatMessage>) {
        _displayMessages.clear()
        _displayMessages.addAll(messages)
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

    //==========

    fun sendMessage(id: String, type: String, userMessage: String, imageUris: List<Uri>, lifecycle: Lifecycle) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value.addMessage(userMessage,imageUris, USER_PREFIX)
            updateDisplayMessages(_uiState.value.messages)
            updateFilteredUriList()

            var currentMessageId: String? = _uiState.value.createLoadingMessage()
            updateDisplayMessages(_uiState.value.messages)

            setInputEnabled(false)
            try {
                var fullPrompt = _uiState.value.fullPrompt
                val fullPromptUris = _uiState.value.fullPromptUris

                //使用系統語言進行回答, 目前支援英文和中文
                val prefix = appContext.getString(R.string.please_ans_with_specified_language)
                fullPrompt = "$prefix $fullPrompt<start_of_turn>model\n"

                val contentBuilder = GeminiContentBuilder(fullPromptUris, appContext, lifecycle, generativeModelManager)
                contentBuilder.startGeminiBuilder(fullPrompt, fullPromptUris.isNotEmpty(), object : GeminiContentBuilder.GeminiBuilderCallback {
                    override fun callBackResult(text: String?, isFinish: Boolean) {
                        currentMessageId?.let {
                            if (isFinish) {
                                uiState.value.appendMessage(it, text ?: "", true)
                                updateDisplayMessages(_uiState.value.messages)
                                insertChatMessage(id, type, uiState.value.messages)
                                currentMessageId = null
                                filteredUriList.clear()
                                setInputEnabled(true)
                            }
                            else {
                                uiState.value.appendMessage(it, text ?: "", false)
                                updateDisplayMessages(_uiState.value.messages)
                            }
                        }
                    }
                })
            }
            catch (e: Exception) {
                _uiState.value.addMessage(e.localizedMessage ?: "Unknown Error", emptyList(), MODEL_PREFIX)
                updateDisplayMessages(_uiState.value.messages)
                setInputEnabled(true)
            }
        }
    }

    fun addMessage(userMessage: String, imageUris: List<Uri>, userOrModel: String) {
        _uiState.value.addMessage(userMessage,imageUris, userOrModel)
        updateDisplayMessages(_uiState.value.messages)
    }

    private fun insertChatMessage(id: String, type: String, chatMessage: List<ChatMessage>) {
        viewModelScope.launch {
            val message = Message(
                id = id,
                messages = chatMessage,
                title = chatMessage.last().message,
                type = type,
                date = AppUtils.getCurrentDateTime(),
                isPinned = false
            )
            repository.insertOrUpdateMessage(message)
        }
    }

    fun clearMessages(id: String = "") {
        _uiState.value = GemmaUiState(newId = id) // or ChatUiState() if you're using a different model
        _displayMessages.clear()
    }

    private fun setInputEnabled(isEnabled: Boolean) {
        _textInputEnabled.value = isEnabled
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return GeminiViewModel(context) as T
            }
        }
    }
}