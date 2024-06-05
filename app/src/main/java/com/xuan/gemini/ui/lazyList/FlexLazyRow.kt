package com.xuan.gemini.ui.lazyList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.xuan.gemini.ui.compose.FlexItem

@Composable
fun FlexLazyRow(
    flexItem: List<String>,
    hapticFeedback: HapticFeedback,
    onItemClick: (String) -> Unit
) {
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
            val endPadding = if (index == flexItem.size - 1) 16.dp else 0.dp

            if (parts.isNotEmpty() && parts.size == 2) {
                FlexItem(
                    title = parts[0],
                    belowText = parts[1],
                    modifier = Modifier
                        .clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onItemClick(item)
                        },
                    modifier2 = Modifier
                        .padding(end = endPadding)
                )
            }
        }
    }
}