package com.xuan.gemma.model

import android.content.Context
import android.net.Uri
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
import com.xuan.gemma.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

class ChatViewModel(
    private val inferenceModel: InferenceModel,
    private val appContext: Context
) : ViewModel() {

    // `GemmaUiState()` is optimized for the Gemma model.
    // Replace `GemmaUiState` with `ChatUiState()` if you're using a different model
    private val _uiState: MutableStateFlow<GemmaUiState> = MutableStateFlow(GemmaUiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _textInputEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isTextInputEnabled: StateFlow<Boolean> = _textInputEnabled.asStateFlow()

    private val repository = MessageRepository(appContext)

    //===========

    private val _userMessage = MutableStateFlow("")
    val userMessage: StateFlow<String> = _userMessage.asStateFlow()

    private val _tempImageUriList = MutableStateFlow<MutableList<Uri>>(mutableListOf())
    val tempImageUriList: StateFlow<List<Uri>> = _tempImageUriList.asStateFlow()

    private val _deleteUriList = MutableStateFlow<MutableList<Uri>>(mutableListOf())
    val deleteUriList: StateFlow<List<Uri>> = _deleteUriList.asStateFlow()

    private val _filteredUriList: MutableStateFlow<List<Uri>> = MutableStateFlow(emptyList())
    val filteredUriList: StateFlow<List<Uri>> = _filteredUriList.asStateFlow()

    private val _flexItem = MutableStateFlow<List<String>>(emptyList())
    val flexItem: StateFlow<List<String>> = _flexItem.asStateFlow()

    private val _openBottomSheet = MutableStateFlow(false)
    val openBottomSheet: StateFlow<Boolean> = _openBottomSheet.asStateFlow()

    //=====

    init {
        refreshFlexItems()
    }

    fun updateUserMessage(message: String) {
        _userMessage.value = message
    }

    fun addTempImageUri(uri: Uri) {
        _tempImageUriList.value.add(uri)
        updateFilteredUriList()
    }

    fun removeTempImageUri(uri: Uri) {
        _tempImageUriList.value.remove(uri)
    }

    fun addDeleteUri(uri: Uri) {
        _deleteUriList.value.add(uri)
        updateFilteredUriList()
    }

    fun removeDeleteUri(uri: Uri) {
        _deleteUriList.value.remove(uri)
    }

    private fun updateFilteredUriList() {
        val currentTempImageUriList = _tempImageUriList.value
        val currentDeleteUriList = _deleteUriList.value

        _filteredUriList.value = currentTempImageUriList.filter { uri ->
            !currentDeleteUriList.contains(uri)
        }
    }

    fun clearTempAndDeleteUriLists() {
        _tempImageUriList.value.clear()
        _deleteUriList.value.clear()
    }

    fun toggleBottomSheet(show: Boolean) {
        _openBottomSheet.value = show
    }

    fun refreshFlexItems() {
        val flexboxItemArray = appContext.resources.getStringArray(R.array.flexboxItem)
        val randomIndices = flexboxItemArray.indices.shuffled().take(5)
        _flexItem.value = randomIndices.map { flexboxItemArray[it] }
    }

    //==========

    fun sendMessage(id: String, type: String, userMessage: String, imageUris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value.addMessage(userMessage,imageUris, USER_PREFIX)
            var currentMessageId: String? = _uiState.value.createLoadingMessage()
            setInputEnabled(false)
            try {
                val fullPrompt = _uiState.value.fullPrompt
                inferenceModel.generateResponseAsync(fullPrompt)
                inferenceModel.partialResults
                    .collectIndexed { index, (partialResult, done) ->
                        currentMessageId?.let {

                            if (index == 0) _uiState.value.appendFirstMessage(it, partialResult)
                            else _uiState.value.appendMessage(it, partialResult, done)

                            if (done) {
                                insertChatMessage(id, type, uiState.value.messages)
                                currentMessageId = null
                                // Re-enable text input
                                setInputEnabled(true)
                            }
                        }
                    }
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
                title = chatMessage.last().message,
                type = type,
                date = AppUtils.getCurrentDateTime()
            )
            repository.insertOrUpdateMessage(message)
        }
    }

    fun clearMessages(id: String = "") {
        _uiState.value = GemmaUiState(newId = id) // or ChatUiState() if you're using a different model
    }

    private fun setInputEnabled(isEnabled: Boolean) {
        _textInputEnabled.value = isEnabled
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
