package com.xuan.gemma.ui.lazyList

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.zoomable.previewer.rememberTransformItemState
import com.origeek.ui.common.compose.DetectScaleGridGesture
import com.origeek.ui.common.compose.ScaleGrid
import com.xuan.gemma.R
import com.xuan.gemma.`object`.ImagePicker.previewer.PreviewerState
import com.xuan.gemma.`object`.ImagePicker.previewer.TransformItemView

@Composable
fun TransformImageLazyList(
    modifier: Modifier,
    tempImageUriList: List<Uri>,
    filteredUriList: List<Uri>,
    deleteUriList: List<Uri>,
    onClick: (Int) -> Unit,
    onDelete: (Uri) -> Unit,
    previewerState: PreviewerState,
    isShowDelete: Boolean
) {
    LazyRow(
        modifier = modifier,
    ) {
        items(tempImageUriList) {item ->

            var isFirst = false
            var isLast = false
            if (filteredUriList.isNotEmpty()) {
                isFirst = item == filteredUriList.first()
                isLast = item == filteredUriList.last()
            }

            val isEmpty = filteredUriList.isEmpty()
            val exitTrans = if (isEmpty) shrinkVertically(animationSpec = tween(durationMillis = 300))
            else shrinkHorizontally(animationSpec = tween(durationMillis = 300))

            val painter = rememberAsyncImagePainter(
                model = item,
                placeholder = painterResource(id = R.drawable.baseline_broken_image_24),
            )
            val itemState = rememberTransformItemState(intrinsicSize = painter.intrinsicSize)

            AnimatedVisibility(
                visible = !deleteUriList.contains(item),
                enter = expandVertically(),
                exit = exitTrans
            ) {
                Card (modifier = Modifier.padding(
                    end = if (isLast) 16.dp else 8.dp,
                    start = if (isFirst) 16.dp else 0.dp)
                ){
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .height(120.dp)
                            .width(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ScaleGrid(
                            detectGesture = DetectScaleGridGesture(
                                onPress = { onClick(filteredUriList.indexOf(item)) }
                            )
                        ) {
                            TransformItemView(
                                key = filteredUriList.indexOf(item),
                                itemState = itemState,
                                transformState = previewerState,
                            ) {
                                Image(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp)),
                                    painter = painter,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null,
                                )
                            }
                        }
                        if (isShowDelete) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onDelete(item) }
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}