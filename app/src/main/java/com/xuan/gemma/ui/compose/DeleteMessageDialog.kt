package com.xuan.gemma.ui.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.xuan.gemma.database.Message
import com.xuan.gemma.database.MessageRepository
import kotlinx.coroutines.launch

@Composable
fun DeleteMessageDialog(
    message: Message,
    repository: MessageRepository,
    onDismiss: () -> Unit
) {

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Confirm Deletion")
        },
        text = {
            Text(text = "Are you sure you want to delete the message \"${message.title}\"?")
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch { repository.deleteMessage(message) }
                    onDismiss()
                }
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}