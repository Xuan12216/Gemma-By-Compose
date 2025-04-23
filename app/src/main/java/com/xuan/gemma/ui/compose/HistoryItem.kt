package com.xuan.gemma.ui.compose

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuan.gemma.R
import com.xuan.gemma.database.Message

data class DropDownItem(
    val text: String
)

@Composable
fun HistoryItem(
    message: Message,
    dropdownItems: List<DropDownItem>,
    onItemClick: (String) -> Unit,
    onItemLongClick: (DropDownItem, String) -> Unit,
    showDate: Boolean
) {
    var isContextMenuVisible by rememberSaveable { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    val itemHeight by remember { mutableStateOf(0.dp) }
    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current

    if (showDate) {
        Text(
            text = message.date.substring(0, 10), // Display only "yyyy/MM/dd"
            modifier = Modifier.padding(start = 21.dp, top = 16.dp),
            fontSize = 12.sp
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .indication(interactionSource, LocalIndication.current)
                .pointerInput(true) {
                    detectTapGestures(
                        onLongPress = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            isContextMenuVisible = true
                            pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                        },
                        onPress = { pressPos ->
                            val press = PressInteraction.Press(pressPos)
                            interactionSource.emit(press)
                            tryAwaitRelease()
                            interactionSource.emit(PressInteraction.Release(press))
                        },
                        onTap = { if (!isContextMenuVisible) onItemClick(message.id) }
                    )
                }
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = {},
                    enabled = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 5.dp, end = 5.dp)
                        .width(30.dp)
                        .height(30.dp)
                ) {
                    Icon(
                        painter = painterResource(id = when {
                            message.isPinned -> R.drawable.baseline_push_pin_24
                            else -> R.drawable.chat_bubble_24px
                        }),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = message.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        DropdownMenu(
            expanded = isContextMenuVisible,
            onDismissRequest = {
                isContextMenuVisible = false
            },
            offset = pressOffset.copy(
                y = pressOffset.y - itemHeight
            )
        ) {
            dropdownItems.forEach {
                DropdownMenuItem(
                    text = { Text(text = it.text,) },
                    onClick = {
                        onItemLongClick(it, message.id)
                        isContextMenuVisible = false
                    }
                )
            }
        }
    }
}