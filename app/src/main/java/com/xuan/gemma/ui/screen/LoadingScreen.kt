package com.xuan.gemma.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xuan.gemma.model.InferenceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun LoadingRoute(
    onModelLoaded: () -> Unit = { },
    onRetry: () -> Unit = { }
) {
    val context = LocalContext.current.applicationContext
    var errorMessage by remember { mutableStateOf("") }

    if (errorMessage != "") {
        ErrorMessage(
            errorMessage,
             onRetry
        )
    }
    else {
        LoadingIndicator("Loading model")
    }

    LaunchedEffect(Unit, block = {
        // Create the LlmInference in a separate thread
        withContext(Dispatchers.IO) {
            try {
                InferenceModel.resetInstance(context)
                // Notify the UI that the model has finished loading
                withContext(Dispatchers.Main) {
                    onModelLoaded()
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "UnknownError"
            }
        }
    })
}

@Composable
fun LoadingIndicator(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(bottom = 8.dp)
        )
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
