package com.xuan.gemma.ui.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jvziyaoyao.scale.image.previewer.ImagePreviewer
import com.jvziyaoyao.scale.zoomable.previewer.VerticalDragType
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import com.xuan.gemma.R
import com.xuan.gemma.data.ChatMessage
import com.xuan.gemma.data.rememberObject.rememberCoilImagePainter
import com.xuan.gemma.data.rememberObject.rememberSettingState
import com.xuan.gemma.data.stateObject.UiState
import com.xuan.gemma.database.Message
import com.xuan.gemma.model.ChatViewModel
import com.xuan.gemma.ui.compose.AppBar
import com.xuan.gemma.ui.compose.BottomSheet
import com.xuan.gemma.ui.compose.ChatItem
import com.xuan.gemma.ui.compose.TextFieldLayout
import com.xuan.gemma.ui.compose.WelcomeLayout
import com.xuan.gemma.ui.lazyList.FlexLazyRow
import com.xuan.gemma.ui.lazyList.TransformImageLazyList
import com.xuan.gemma.util.PickImageFunc
import com.xuan.gemma.util.PickImageUsingCamera
import com.xuan.gemma.util.RecordFunc
import kotlinx.coroutines.launch

@Composable
internal fun ChatRoute(
    paddingValues: PaddingValues,
    drawerState: DrawerState,
    recordFunc: RecordFunc,
    pickImageFunc: PickImageFunc,
    pickImageUsingCamera: PickImageUsingCamera,
    chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.getFactory(LocalContext.current.applicationContext)
    ),
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    type: String,
    selectedMessage: Message?,
    onSelectedMessageClear: () -> Unit
    ) {
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val textInputEnabled by chatViewModel.isTextInputEnabled.collectAsStateWithLifecycle()

    selectedMessage?.let { message ->
        chatViewModel.clearMessages()
        val newMessages = message.messages.map { chat ->
            ChatMessage(
                message = chat.message,
                uris = chat.uris,
                author = chat.author
            )
        }

        // 在修改完 _uiState.value.messages 后，再将消息插入到数据库
        newMessages.reversed().forEach { chat ->
            chatViewModel.addMessage(chat.message, chat.uris, chat.author)
        }
        onSelectedMessageClear()
    }

    ChatScreen(
        pickImageFunc = pickImageFunc,
        pickImageUsingCamera = pickImageUsingCamera,
        recordFunc = recordFunc,
        paddingValues = paddingValues,
        drawerState = drawerState,
        uiState = uiState,
        textInputEnabled = textInputEnabled,
        onSendMessage = { id, message, imageUris ->
            chatViewModel.sendMessage(id, type, message, imageUris)
        },
        onClearMessages = { chatViewModel.clearMessages() },
        active = active,
        onActiveChange = onActiveChange,
        chatViewModel = chatViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    pickImageFunc: PickImageFunc,
    pickImageUsingCamera: PickImageUsingCamera,
    recordFunc: RecordFunc,
    paddingValues: PaddingValues,
    drawerState: DrawerState,
    uiState: UiState,
    textInputEnabled: Boolean = true,
    onSendMessage: (String, String, List<Uri>) -> Unit,
    onClearMessages: () -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    chatViewModel: ChatViewModel
) {
    //state============================================================================
    val userMessage by chatViewModel.userMessage.collectAsState()
    val tempImageUriList by chatViewModel.tempImageUriList.collectAsState()//圖片
    val deleteUriList by chatViewModel.deleteUriList.collectAsState()//需要刪除的圖片
    val filteredUriList by chatViewModel.filteredUriList.collectAsState()//過濾后的list
    val flexItem by chatViewModel.flexItem.collectAsState()
    val openBottomSheet by chatViewModel.openBottomSheet.collectAsState()

    //bottomSheet=====
    val bottomSheetState = rememberModalBottomSheetState()
    val options: List<Triple<Painter, String, Int>> = listOf(
        Triple(painterResource(id = R.drawable.baseline_camera_alt_24), stringResource(id = R.string.useCameraPickImage), 1),
        Triple(painterResource(id = R.drawable.baseline_image_24), stringResource(id = R.string.useGalleryPickImage), 2)
    )
    //hapticFeedback=====
    val hapticFeedback = LocalHapticFeedback.current
    //=====
    val scope = rememberCoroutineScope()
    //=====
    val settingState = rememberSettingState()
    val previewerState = rememberPreviewerState(
        scope = scope,
        defaultAnimationSpec = tween(settingState.animationDuration),
        verticalDragType = VerticalDragType.Down,
        pageCount = { filteredUriList.size },
        getKey = { it },
    )

    //status========================================================================

    // State for LazyList
    val listState = rememberLazyListState()

    // Scroll to bottom when a new message is added
    LaunchedEffect(uiState.messages.size, textInputEnabled) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size)
        }
    }

    LaunchedEffect(filteredUriList.size) {
        if (filteredUriList.isEmpty() && (previewerState.canClose || previewerState.animating)) {
            previewerState.close()
        }
    }

    //backHandler=====
    val backHandlerEnabled = drawerState.isOpen || previewerState.canClose || previewerState.animating || active

    BackHandler(enabled = backHandlerEnabled) {
        scope.launch {
            if (!active) {
                when {
                    drawerState.isOpen -> drawerState.close()
                    previewerState.canClose -> {
                        if (settingState.transformExit) previewerState.exitTransform()
                        else previewerState.close()
                    }
                }
            }
            else onActiveChange(false)
        }
    }

    //=================================================================================

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        //AppBar=====
        AppBar(
            textInputEnabled = textInputEnabled,
            iconBtn1Onclick = {scope.launch { drawerState.open() }},
            iconBtn1Painter = painterResource(id = R.drawable.baseline_menu_24),
            iconBtn1Content = "Open Drawer Navigation",
            animatedText = if (uiState.messages.isEmpty()) " " else uiState.messages.last().message,
            iconBtn2Painter = painterResource(id = R.drawable.baseline_add_comment_24),
            iconBtn2Content = "Add New Chat",
            iconBtn2Onclick = {
                //refresh FlexItem
                chatViewModel.refreshFlexItems()

                //clear messages
                onClearMessages()
            }
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
                    items(uiState.messages.reversed()) { chat ->
                        ChatItem(chat)
                    }
                }
            }
        }

        //flexItemList
        if (uiState.messages.isEmpty()) {
            FlexLazyRow(
                flexItem = flexItem,
                hapticFeedback = hapticFeedback,
                onItemClick = { chatViewModel.updateUserMessage(it) }
            )
        }

        //pickImage Func and textField
        TextFieldLayout(
            textInputEnabled = textInputEnabled,
            filledIconBtnOnClick = { scope.launch { chatViewModel.toggleBottomSheet(true) }},
            filledIconPainter = painterResource(id = R.drawable.baseline_add_24),
            filledIconContent = "Insert Image",
            textFieldText = userMessage,
            onTextFieldChange = { chatViewModel.updateUserMessage(it) },
            onTextFieldAdd = {
                onSendMessage(uiState.id, userMessage, filteredUriList)
                chatViewModel.updateUserMessage("")
                chatViewModel.clearTempAndDeleteUriLists()
            },
            textFieldHint = stringResource(id = R.string.EditText_hint),
            recordFunc = recordFunc,
            textFieldTrailingIcon1 = painterResource(id = R.drawable.baseline_keyboard_voice_24),
            textFieldTrailingIcon2 = painterResource(id = R.drawable.baseline_send_24),
            textFieldContent = stringResource(id = R.string.EditText_hint),
            isShowButton = true
        )

        //show image
        TransformImageLazyList(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            tempImageUriList = tempImageUriList,
            filteredUriList = filteredUriList,
            deleteUriList = deleteUriList,
            onClick = {
                scope.launch {
                    if (settingState.transformEnter) previewerState.enterTransform(it)
                    else previewerState.open(it)
                }
            },
            onDelete = { chatViewModel.addDeleteUri(it); },
            previewerState = previewerState,
            isShowDelete = true
        )
    }

    //full screen image
    ImagePreviewer(
        state = previewerState,
        imageLoader = { index ->
            val painter = if (settingState.loaderError && (index % 2 == 0)) null
            else rememberCoilImagePainter(image = filteredUriList[index])

            return@ImagePreviewer Pair(painter, painter?.intrinsicSize)
        }
    )

    //bottomSheet
    if (openBottomSheet) {
        BottomSheet(
            bottomSheetState = bottomSheetState,
            onDismiss = { chatViewModel.toggleBottomSheet(false) },
            options = options,
            pickImageFunc = pickImageFunc,
            pickImageUsingCamera = pickImageUsingCamera,
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
