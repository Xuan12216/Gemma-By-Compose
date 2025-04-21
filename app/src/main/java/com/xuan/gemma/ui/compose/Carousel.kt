package com.xuan.gemma.ui.compose

import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.xuan.gemma.R
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalCarousel(
    filterUriList: List<Uri> = emptyList(),
    onItemDelete: ((Uri) -> Unit)?,
) {
    val hapticFeedback = LocalHapticFeedback.current
    var isLongPressed by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    var fullScreenStartIndex by remember { mutableIntStateOf(0) }

    if (filterUriList.isEmpty()) isLongPressed = false
    val animatedHeight by animateDpAsState(
        targetValue = if (filterUriList.isEmpty()) 0.dp else 146.dp,
        animationSpec = if (filterUriList.isEmpty()) spring(stiffness = 300f) else snap(),
        label = "CloseAnimation"
    )

    HorizontalMultiBrowseCarousel(
        state = CarouselState { filterUriList.size },
        modifier = Modifier.fillMaxWidth().height(animatedHeight),
        preferredItemWidth = 120.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        val item = filterUriList[i]

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .maskClip(MaterialTheme.shapes.large)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                isFullScreen = true
                                fullScreenStartIndex = i
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            onLongPress = {
                                isLongPressed = !isLongPressed
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        )
                    },
                painter = rememberAsyncImagePainter(model = item),
                contentDescription = item.toString(),
                contentScale = ContentScale.Crop
            )

            if (null != onItemDelete && isLongPressed) {
                Box (
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = MaterialTheme.shapes.extraLarge
                        )
                ){
                    IconButton(
                        onClick = { onItemDelete(item) },
                        modifier = Modifier
                            .padding(5.dp)
                            .align(Alignment.Center)
                            .size(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_delete_24),
                            contentDescription = "Delete Image",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    if (isFullScreen && fullScreenStartIndex < filterUriList.size) {
        FullScreenCarousel(
            filterUriList = filterUriList,
            fullScreenStartIndex = fullScreenStartIndex,
            onDismiss = {
                isFullScreen = false
                fullScreenStartIndex = 0
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenCarousel(
    filterUriList: List<Uri> = emptyList(),
    fullScreenStartIndex: Int,
    onDismiss: () -> Unit
) {
    BaseFullScreenDialog(
        onDismiss = { onDismiss() },
        content = {
            val configuration = LocalConfiguration.current
            val preferredItemWidth = (configuration.screenWidthDp).dp

            HorizontalMultiBrowseCarousel(
                state = CarouselState(currentItem = fullScreenStartIndex) { filterUriList.size },
                modifier = Modifier.fillMaxSize(),
                preferredItemWidth = preferredItemWidth,
                minSmallItemWidth = 0.dp,
                itemSpacing = 0.dp,
                contentPadding = PaddingValues(0.dp),
            ) { i ->
                val item = filterUriList[i]

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .zoomable(rememberZoomState()),
                        painter = rememberAsyncImagePainter(model = item),
                        contentDescription = item.toString(),
                        contentScale = ContentScale.Fit
                    )

                    Box (
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(50.dp)
                            .padding(end = 16.dp, top = 16.dp)
                            .background(
                                color = Color.DarkGray.copy(alpha = 0.4f),
                                shape = MaterialTheme.shapes.extraLarge
                            )
                    ){
                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .padding(5.dp)
                                .align(Alignment.Center)
                                .size(50.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_close_24),
                                contentDescription = "Delete Image",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    )
}