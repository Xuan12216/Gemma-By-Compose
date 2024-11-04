package com.xuan.gemma.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import com.xuan.gemma.database.Message
import com.xuan.gemma.database.MessageRepository
import kotlinx.coroutines.launch

@Composable
fun RenameMessageDialog(
    message: Message,
    repository: MessageRepository,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(message.title) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Rename Title")
        },
        text = {
            Column {
                Text(text = "Enter new title:")
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Title") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        repository.insertOrUpdateMessage(message.copy(title = newName))
                    }
                    onDismiss()
                }
            ) {
                Text("Confirm")
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