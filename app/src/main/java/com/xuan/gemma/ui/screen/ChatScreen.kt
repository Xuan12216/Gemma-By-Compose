package com.xuan.gemma.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xuan.gemma.R
import com.xuan.gemma.activity.LocalMainViewModel
import com.xuan.gemma.data.ChatMessage
import com.xuan.gemma.data.stateObject.UiState
import com.xuan.gemma.database.Message
import com.xuan.gemma.model.InferenceModel
import com.xuan.gemma.ui.compose.HorizontalCarousel
import com.xuan.gemma.viewmodel.ChatViewModel
import com.xuan.gemma.ui.compose.AppBar
import com.xuan.gemma.ui.compose.BottomSheet
import com.xuan.gemma.ui.compose.ChatItem
import com.xuan.gemma.ui.compose.TextFieldLayout
import com.xuan.gemma.ui.compose.WelcomeLayout
import com.xuan.gemma.ui.lazyList.FlexLazyRow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
internal fun ChatRoute(
    paddingValues: PaddingValues,
    active: Boolean,
    type: String,
    selectedMessage: Message?,
    onSelectedMessageClear: () -> Unit
    ) {

    val context = LocalContext.current.applicationContext
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.getFactory(context))
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val textInputEnabled by chatViewModel.isTextInputEnabled.collectAsStateWithLifecycle()

    // Reset InferenceModel when entering ChatScreen
    LaunchedEffect(Unit) {
        val inferenceModel = InferenceModel.getInstance(context)
        chatViewModel.resetInferenceModel(inferenceModel)
    }

    LaunchedEffect(selectedMessage) {
        selectedMessage?.let { message ->
            chatViewModel.selectedMessage = selectedMessage
            chatViewModel.clearMessages(message.id)
            val newMessages = message.messages.map { chat ->
                ChatMessage(
                    rawMessage = chat.message,
                    uris = chat.uris,
                    author = chat.author,
                )
            }

            // 在修改完 _uiState.value.messages 后，再将消息插入到数据库
            newMessages.reversed().forEach { chat ->
                chatViewModel.addMessage(chat.message, chat.uris, chat.author)
            }
            chatViewModel.recomputeSizeInTokens("")
            onSelectedMessageClear()
        }
    }

    ChatScreen(
        context = context,
        paddingValues = paddingValues,
        uiState = uiState,
        textInputEnabled = textInputEnabled,
        onSendMessage = { id, message, imageUris -> chatViewModel.sendMessage(id, type, message, imageUris) },
        onClearMessages = { chatViewModel.clearMessages() },
        active = active,
        chatViewModel = chatViewModel,
        type = type,
        remainingTokens = chatViewModel.tokensRemaining,
        onChangedMessage = { message ->
            chatViewModel.recomputeSizeInTokens(message)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    context: Context,
    paddingValues: PaddingValues,
    drawerState: DrawerState = LocalMainViewModel.current.drawerState.value,
    uiState: UiState,
    textInputEnabled: Boolean = true,
    onSendMessage: (String, String, List<Uri>) -> Unit,
    onClearMessages: () -> Unit,
    active: Boolean,
    chatViewModel: ChatViewModel,
    type: String,
    remainingTokens: StateFlow<Int>,
    onChangedMessage: (String) -> Unit,
) {
    //bottomSheet=====
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    //status========================================================================

    // State for LazyList
    val listState = rememberLazyListState()

    // Scroll to bottom when a new message is added
    LaunchedEffect(uiState.messages.size, textInputEnabled) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size)
        }
    }

    val tokens by remainingTokens.collectAsState(initial = -1)

    //backHandler=====
    if (!active) {
        BackHandler(enabled = uiState.messages.isNotEmpty()) {
            scope.launch {
                if (uiState.messages.isNotEmpty() && textInputEnabled) {
                    chatViewModel.refreshFlexItems()
                    onClearMessages()
                }
            }
        }
    }

    //=================================================================================

    //uiState.messages.isEmpty()

    Column(
        modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .imePadding()
    ) {
        //AppBar=====
        AppBar(
            textInputEnabled = textInputEnabled,
            iconBtn1Onclick = {
                if (uiState.messages.isEmpty()) {
                    scope.launch { drawerState.open() }
                }
                else {
                    InferenceModel.getInstance(context).resetSession()
                    chatViewModel.refreshFlexItems()
                    onClearMessages()
                }
            },
            iconBtn1Painter = painterResource(id = if (uiState.messages.isEmpty()) R.drawable.baseline_menu_24 else R.drawable.baseline_arrow_back_24),
            iconBtn1Content = "Open Drawer Navigation",
            animatedText = if (uiState.messages.isEmpty()) " " else uiState.messages.last().message,
        )

        Column (modifier = Modifier.weight(1f)){
            //welcomeLayout
            if (uiState.messages.isEmpty()) {
                WelcomeLayout(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 30.dp),
                    glideDrawable = R.drawable.sparkle_resting,
                    glideContent = "Welcome Layout",
                    animatedText = stringResource(id = R.string.welcome_text, type)
                )
            }
            else {
                //mainLayout
                val messages = uiState.messages.reversed()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                ) {
                    itemsIndexed(messages.toList()) { index, chat ->
                        val isLastItem = index == messages.lastIndex
                        ChatItem(chat, tokens = if (isLastItem) tokens else null)
                    }
                }
            }
        }

        //flexItemList
        if (uiState.messages.isEmpty()) {
            FlexLazyRow(
                flexItem = chatViewModel.flexItem,
                onItemClick = { chatViewModel.updateUserMessage(it) }
            )
        }

        //pickImage Func and textField
        TextFieldLayout(
            textInputEnabled = textInputEnabled && tokens > 0,
            filledIconBtnOnClick = { scope.launch { chatViewModel.toggleBottomSheet(true) }},
            filledIconPainter = painterResource(id = R.drawable.baseline_add_24),
            filledIconContent = "Insert Image",
            textFieldText = chatViewModel.userMessage,
            onTextFieldChange = { chatViewModel.updateUserMessage(it) },
            onTextFieldAdd = {
                onChangedMessage(it)
                onSendMessage(uiState.id, chatViewModel.userMessage, chatViewModel.filteredUriList)
                chatViewModel.updateUserMessage("")
                chatViewModel.clearTempAndDeleteUriLists()
            },
            textFieldHint = stringResource(id = R.string.EditText_hint),
            recordFunc = LocalMainViewModel.current.recordFunc,
            textFieldTrailingIcon1 = painterResource(id = R.drawable.baseline_keyboard_voice_24),
            textFieldTrailingIcon2 = painterResource(id = R.drawable.baseline_send_24),
            textFieldContent = stringResource(id = R.string.EditText_hint),
            isShowButton = true
        )

        HorizontalCarousel(
            filterUriList = chatViewModel.filteredUriList,
            onItemDelete = { chatViewModel.addDeleteUri(it) }
        )
    }

    //bottomSheet
    if (chatViewModel.openBottomSheet) {
        BottomSheet(
            bottomSheetState = bottomSheetState,
            onDismiss = { chatViewModel.toggleBottomSheet(false) },
            options = chatViewModel.options,
            onCallbackImageUri = {
                scope.launch {
                    bottomSheetState.hide()
                    chatViewModel.toggleBottomSheet(false)
                }
                chatViewModel.addTempImageUri(it)
            }
        )
    }
}