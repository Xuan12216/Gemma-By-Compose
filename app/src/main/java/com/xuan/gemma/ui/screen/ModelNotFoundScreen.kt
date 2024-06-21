package com.xuan.gemma.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xuan.gemma.util.FileManagerHelper

@Composable
fun ModelNotFoundScreen(
    fileManagerHelper: FileManagerHelper,
    onRefresh: () -> Unit
) {

    var isClick by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Column (
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = if (!isClick) "Model not found!" else "Click Button to Refresh Page!",
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Please make sure the required model files are available." ,
                textAlign = TextAlign.Center
            )
        }
        Button(
            onClick = {
                if (!isClick) {
                    fileManagerHelper.openFileManager()
                    isClick = true
                }
                else {
                    onRefresh()
                    isClick = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (!isClick) "Add Model" else "Refresh State")
        }
    }
}
