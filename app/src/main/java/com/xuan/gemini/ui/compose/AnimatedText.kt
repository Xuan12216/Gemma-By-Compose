package com.xuan.gemini.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay

@Composable
fun AnimatedText(text: String, modifier: Modifier = Modifier, maxLine: Int, fontSize: TextUnit) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        delay(300)
        text.forEachIndexed { index, _ ->
            delay(50)
            displayedText = text.substring(0, index + 1)
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = displayedText,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            maxLines = maxLine
        )
    }
}
