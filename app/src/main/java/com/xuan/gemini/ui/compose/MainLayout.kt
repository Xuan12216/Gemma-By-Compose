package com.xuan.gemini.ui.compose

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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jvziyaoyao.image.previewer.ImagePreviewer
import com.jvziyaoyao.zoomable.previewer.VerticalDragType
import com.jvziyaoyao.zoomable.previewer.rememberPreviewerState
import com.xuan.gemini.R
import com.xuan.gemini.ui.lazyList.FlexLazyRow
import com.xuan.gemini.util.PickImageFunc
import com.xuan.gemini.util.PickImageUsingCamera
import com.xuan.gemini.util.RecordFunc
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.xuan.gemini.data.rememberObject.rememberCoilImagePainter
import com.xuan.gemini.data.rememberObject.rememberSettingState
import com.xuan.gemini.ui.lazyList.TransformImageLazyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    drawerState: DrawerState,
    recordFunc: RecordFunc,
    pickImageFunc: PickImageFunc,
    pickImageUsingCamera: PickImageUsingCamera,
    paddingValues: PaddingValues
) {
    //state============================================================================

    var text by remember { mutableStateOf("") }//textField
    //=====
    val itemList = remember { mutableStateListOf<String>() }//内容顯示
    //=====
    val tempImageUriList = remember { mutableStateListOf<Uri>() }//圖片
    val deleteUriList = remember { mutableStateListOf<Uri>() }//需要刪除的圖片
    val filteredUriList by remember { derivedStateOf { tempImageUriList - deleteUriList } }//過濾后的list
    //flex=====
    val flexboxItemArray = stringArrayResource(id = R.array.flexboxItem)
    var flexItem by remember { mutableStateOf(emptyList<String>()) }
    if (flexItem.isEmpty()) {
        val randomIndices = flexboxItemArray.indices.shuffled().take(5)
        val randomItems = randomIndices.map { flexboxItemArray[it] }
        flexItem = randomItems
    }
    //bottomSheet=====
    val openBottomSheet = rememberSaveable { mutableStateOf(false) }
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

    //LaunchedEffect(settingState.dataRepeat) { onRepeatChanged(settingState.dataRepeat) }
    LaunchedEffect(filteredUriList.size) {
        if (filteredUriList.isEmpty() && (previewerState.canClose || previewerState.animating)) {
            previewerState.close()
        }
    }
    //backHandler=====
    val backHandlerEnabled = drawerState.isOpen || previewerState.canClose || previewerState.animating

    BackHandler(enabled = backHandlerEnabled) {
        scope.launch {
            when {
                drawerState.isOpen -> drawerState.close()
                previewerState.canClose -> {
                    if (settingState.transformExit) previewerState.exitTransform()
                    else previewerState.close()
                }
            }
        }
    }

    //start============================================================================

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        //AppBar=====
        AppBar(
            iconBtn1Onclick = {scope.launch { drawerState.open() }},
            iconBtn1Painter = painterResource(id = R.drawable.baseline_menu_24),
            iconBtn1Content = "Open Drawer Navigation",
            animatedText = if (itemList.isEmpty()) " " else itemList[itemList.size - 1],
            iconBtn2Painter = painterResource(id = R.drawable.baseline_add_comment_24),
            iconBtn2Content = "Add New Chat",
            iconBtn2Onclick = {
                //refresh FlexItem
                val randomIndices = flexboxItemArray.indices.shuffled().take(5)
                val randomItems = randomIndices.map { flexboxItemArray[it] }
                flexItem = randomItems

                //clear ItemList
                itemList.clear()
            }
        )

        Column (modifier = Modifier.weight(1f)){
            //welcomeLayout
            if (itemList.isEmpty()) {
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
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 16.dp)
                ) { items(itemList) { Text(text = it) } }
            }
        }

        //flexItemList
        if (itemList.isEmpty()) {
            FlexLazyRow(
                flexItem = flexItem,
                hapticFeedback = hapticFeedback,
                onItemClick = { text = it }
            )
        }

        //pickImage Func and textField
        TextFieldLayout(
            filledIconBtnOnClick = { scope.launch { openBottomSheet.value = true }},
            filledIconPainter = painterResource(id = R.drawable.baseline_image_24),
            filledIconContent = "Insert Image",
            textFieldText = text,
            onTextFieldChange = { text = it },
            onTextFieldAdd = {
                itemList.add(it)
                text = ""
                tempImageUriList.clear()
                deleteUriList.clear()
            },
            textFieldHint = stringResource(id = R.string.EditText_hint),
            recordFunc = recordFunc,
            textFieldTrailingIcon1 = painterResource(id = R.drawable.baseline_keyboard_voice_24),
            textFieldTrailingIcon2 = painterResource(id = R.drawable.baseline_send_24),
            textFieldContent = stringResource(id = R.string.EditText_hint)
        )

        //show image
        TransformImageLazyList(
            tempImageUriList = tempImageUriList,
            filteredUriList = filteredUriList,
            deleteUriList = deleteUriList,
            onClick = {
                scope.launch {
                    if (settingState.transformEnter) previewerState.enterTransform(it)
                    else previewerState.open(it)
                }
            },
            onDelete = { deleteUriList.add(it) },
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
    if (openBottomSheet.value) {
        BottomSheet(
            bottomSheetState = bottomSheetState,
            onDismiss = { openBottomSheet.value = false },
            options = options,
            pickImageFunc = pickImageFunc,
            pickImageUsingCamera = pickImageUsingCamera,
            onCallbackImageUri = {
                scope.launch {
                    bottomSheetState.hide()
                    openBottomSheet.value = false
                }
                tempImageUriList.add(it)
            }
        )
    }
}