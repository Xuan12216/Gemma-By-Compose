package com.xuan.gemma.ui.compose

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xuan.gemma.data.ChatMessage
import com.xuan.gemma.ui.carousel.HorizontalCarousel

@Composable
fun ChatItem(
    chatMessage: ChatMessage
) {
    val bubbleShape = if (chatMessage.isFromUser) RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    else RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)

    val horizontalAlignment = if (chatMessage.isFromUser) Alignment.End
    else Alignment.Start

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

                        if (chatMessage.uris.isNotEmpty()) {
                            HorizontalCarousel (
                                filterUriList = chatMessage.uris,
                                onItemDelete = null
                            )
                        }
                    }
                }
            }
        }
    }
}