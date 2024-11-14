package com.xuan.gemma.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jvziyaoyao.scale.image.previewer.ImagePreviewer
import com.jvziyaoyao.scale.zoomable.pager.PagerGestureScope
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import com.xuan.gemma.data.ChatMessage
import com.xuan.gemma.data.rememberObject.rememberCoilImagePainter
import com.xuan.gemma.ui.lazyList.TransformImageLazyList
import kotlinx.coroutines.launch

@Composable
fun ChatItem(
    chatMessage: ChatMessage
) {
    //val backgroundColor = if (chatMessage.isFromUser) MaterialTheme.colorScheme.tertiaryContainer
    //else MaterialTheme.colorScheme.secondaryContainer

    val bubbleShape = if (chatMessage.isFromUser) RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    else RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)

    val horizontalAlignment = if (chatMessage.isFromUser) Alignment.End
    else Alignment.Start

    val scope = rememberCoroutineScope()
    val previewerState = rememberPreviewerState(pageCount = { chatMessage.uris.size })

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(previewerState.canOpen) { showDialog = !previewerState.canOpen }

    LaunchedEffect(showDialog) { if (!showDialog) previewerState.close() }

    if (previewerState.visible) BackHandler { scope.launch { previewerState.close() } }

    SelectionContainer {
        Column(
            horizontalAlignment = horizontalAlignment,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .padding(start = 8.dp, end = 8.dp)
                .fillMaxWidth()
        ) {
            val author = if (chatMessage.isFromUser) "User"
            else "Model"

            Text(
                text = author,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row {
                BoxWithConstraints {
                    Card(
                        //colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        shape = bubbleShape,
                        modifier = Modifier.widthIn(0.dp, maxWidth * 0.9f)
                    ) {
                        if (chatMessage.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        else {
                            Text(
                                text = chatMessage.message,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        TransformImageLazyList(
                            modifier = Modifier
                                .align(if (chatMessage.isFromUser) Alignment.End else Alignment.Start)
                                .padding(bottom = if (chatMessage.uris.isEmpty()) 0.dp else 16.dp),
                            tempImageUriList = chatMessage.uris,
                            filteredUriList = chatMessage.uris,
                            deleteUriList = emptyList(),
                            onClick = {
                                scope.launch { previewerState.open(it) }
                            },
                            onDelete = {},
                            previewerState = previewerState,
                            isShowDelete = false
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        Dialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showDialog = false }
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                ImagePreviewer(
                    state = previewerState,
                    detectGesture = PagerGestureScope(onTap = { scope.launch { previewerState.close() } }),
                    imageLoader = { index ->
                        val painter = rememberCoilImagePainter(image = chatMessage.uris[index])
                        Pair(painter, painter.intrinsicSize)
                    }
                )
            }
        }
    }
}