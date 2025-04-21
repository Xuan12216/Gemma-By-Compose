package com.xuan.gemma.ui.screen

import android.content.Context
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
import com.xuan.gemma.R
import com.xuan.gemma.util.AppUtils
import com.xuan.gemma.util.FileManagerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ModelNotFoundScreen(
    fileManagerHelper: FileManagerHelper,
    onRefresh: () -> Unit,
    context: Context
) {
    var isDownloading by remember { mutableStateOf(false) }
    var downloadMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp),) {
        Column (
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            if (!isDownloading) {
                Text(
                    text = "Model not found!",
                    modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Please make sure the required model files are available." ,
                    textAlign = TextAlign.Center
                )
            }
            else {
                Text(
                    text = downloadMessage,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
        // 按鈕：開啟檔案管理器
        Button(
            enabled = !isDownloading,
            onClick = {
                fileManagerHelper.openFileManager()
                onRefresh()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Select Model From File Manager")
        }

        // 按鈕：下載模型
        Button(
            enabled = !isDownloading,
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    isDownloading = true
                    val success = AppUtils.downloadFileFromUrl(
                        context,
                        context.getString(R.string.gemma3_model_download_url),
                        onProgressUpdate = { progressString -> downloadMessage = progressString }
                    )
                    downloadMessage = if (success) "Download complete!" else "Download failed."
                    if (success) onRefresh()
                    else isDownloading = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Download Model")
        }
    }
}
