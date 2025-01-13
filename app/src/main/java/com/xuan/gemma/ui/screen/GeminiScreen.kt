package com.xuan.gemma.ui.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xuan.gemma.R
import com.xuan.gemma.activity.LocalMainViewModel
import com.xuan.gemma.data.ChatMessage
import com.xuan.gemma.data.stateObject.UiState
import com.xuan.gemma.database.Message
import com.xuan.gemma.ui.carousel.HorizontalCarousel
import com.xuan.gemma.ui.compose.AppBar
import com.xuan.gemma.ui.compose.BottomSheet
import com.xuan.gemma.ui.compose.ChatItem
import com.xuan.gemma.ui.compose.TextFieldLayout
import com.xuan.gemma.ui.compose.WelcomeLayout
import com.xuan.gemma.ui.lazyList.FlexLazyRow
import com.xuan.gemma.viewmodel.GeminiViewModel
import kotlinx.coroutines.launch

@Composable
fun GeminiLayout (
    paddingValues: PaddingValues,
    drawerState: DrawerState,
    viewModel: GeminiViewModel = viewModel(factory = GeminiViewModel.getFactory(LocalContext.current.applicationContext)),
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    type: String,
    selectedMessage: Message?,
    onSelectedMessageClear: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textInputEnabled by viewModel.isTextInputEnabled.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current

    LaunchedEffect(lifecycleOwner.lifecycle.currentState) {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
            viewModel.generativeModelManager.checkApiKey(context)
            viewModel.generativeModelManager.initializeGenerativeModel(context)
        }
    }

    selectedMessage?.let { message ->
        viewModel.clearMessages(message.id)
        val newMessages = message.messages.map { chat ->
            ChatMessage(
                rawMessage = chat.message,
                uris = chat.uris,
                author = chat.author
            )
        }

        // 在修改完 _uiState.value.messages 后，再将消息插入到数据库
        newMessages.reversed().forEach { viewModel.addMessage(it.message, it.uris, it.author) }
        onSelectedMessageClear()
    }

    GeminiChatScreen(
        paddingValues = paddingValues,
        drawerState = drawerState,
        uiState = uiState,
        textInputEnabled = textInputEnabled,
        onSendMessage = { id, message, imageUris -> viewModel.sendMessage(id, type, message, imageUris, lifecycleOwner.lifecycle) },
        onClearMessages = { viewModel.clearMessages() },
        active = active,
        onActiveChange = onActiveChange,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiChatScreen(
    paddingValues: PaddingValues,
    drawerState: DrawerState,
    uiState: UiState,
    textInputEnabled: Boolean = true,
    onSendMessage: (String, String, List<Uri>) -> Unit,
    onClearMessages: () -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    viewModel: GeminiViewModel
) {
    //bottomSheet=====
    val bottomSheetState = rememberModalBottomSheetState()

    //=====
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

    //backHandler=====
    val backHandlerEnabled = drawerState.isOpen ||  active || uiState.messages.isNotEmpty()

    BackHandler(enabled = backHandlerEnabled) {
        scope.launch {
            if (!active) {
                when {
                    drawerState.isOpen -> drawerState.close()
                    uiState.messages.isNotEmpty() -> {
                        viewModel.refreshFlexItems()
                        onClearMessages()
                    }
                }
            }
            else onActiveChange(false)
        }
    }

    //=================================================================================

    //uiState.messages.isEmpty()

    Column(
        modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
        //AppBar=====
        AppBar(
            textInputEnabled = textInputEnabled,
            iconBtn1Onclick = {
                if (uiState.messages.isEmpty()) {
                    scope.launch { drawerState.open() }
                }
                else {
                    viewModel.refreshFlexItems()
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
                    animatedText = stringResource(id = R.string.welcome_text)
                )
            }
            else {
                //mainLayout
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                ) {
                    if (viewModel.displayMessages.isNotEmpty()) {
                        items(viewModel.displayMessages.reversed()) { chat ->
                            ChatItem(chat)
                        }
                    }
                }
            }
        }

        //flexItemList
        if (uiState.messages.isEmpty()) {
            FlexLazyRow(
                flexItem = viewModel.flexItem,
                onItemClick = { viewModel.updateUserMessage(it) }
            )
        }

        //pickImage Func and textField
        TextFieldLayout(
            textInputEnabled = textInputEnabled,
            filledIconBtnOnClick = { scope.launch { viewModel.toggleBottomSheet(true) }},
            filledIconPainter = painterResource(id = R.drawable.baseline_add_24),
            filledIconContent = "Insert Image",
            textFieldText = viewModel.userMessage,
            onTextFieldChange = { viewModel.updateUserMessage(it) },
            onTextFieldAdd = {
                onSendMessage(uiState.id, viewModel.userMessage, viewModel.filteredUriList)
                viewModel.updateUserMessage("")
                viewModel.clearTempAndDeleteUriLists()
            },
            textFieldHint = stringResource(id = R.string.EditText_hint),
            recordFunc = LocalMainViewModel.current.recordFunc,
            textFieldTrailingIcon1 = painterResource(id = R.drawable.baseline_keyboard_voice_24),
            textFieldTrailingIcon2 = painterResource(id = R.drawable.baseline_send_24),
            textFieldContent = stringResource(id = R.string.EditText_hint),
            isShowButton = true
        )

        HorizontalCarousel(
            filterUriList = viewModel.filteredUriList,
            onItemDelete = { viewModel.addDeleteUri(it) }
        )
    }

    //bottomSheet
    if (viewModel.openBottomSheet) {
        BottomSheet(
            bottomSheetState = bottomSheetState,
            onDismiss = { viewModel.toggleBottomSheet(false) },
            options = viewModel.options,
            onCallbackImageUri = {
                scope.launch {
                    bottomSheetState.hide()
                    viewModel.toggleBottomSheet(false)
                }
                viewModel.addTempImageUri(it)
            }
        )
    }
}
