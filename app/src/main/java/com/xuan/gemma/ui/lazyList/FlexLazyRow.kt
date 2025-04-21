package com.xuan.gemma.ui.lazyList

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.xuan.gemma.R
import com.xuan.gemma.ui.compose.FlexItem

@Composable
fun FlexLazyRow(
    context: Context = LocalContext.current,
    flexItem: List<String>,
    onItemClick: (String) -> Unit
) {
    //hapticFeedback=====
    val hapticFeedback = LocalHapticFeedback.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = 5.dp
            )
    ) {
        items(flexItem.size) { index ->
            val item = flexItem[index]
            val parts = item.split(" : ")

            if (parts.isNotEmpty() && parts.size == 2) {
                FlexItem(
                    title = parts[0],
                    belowText = parts[1],
                    modifier = Modifier
                        .clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onItemClick(item)
                        },
                    modifier2 = Modifier.padding(end = 0.dp)
                )
            }
        }

        item {
            val generatedItem = context.getString(R.string.flexbox_ai_generate)
            val parts = generatedItem.split(" : ")

            FlexItem(
                title = parts[0],
                belowText = parts[1],
                modifier = Modifier
                    .clickable {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onItemClick(parts[0] + parts[1] + " : ")
                    },
                modifier2 = Modifier
                    .padding(end = 16.dp) // 适当设置 padding
            )
        }
    }
}