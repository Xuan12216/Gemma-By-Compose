package com.xuan.gemini.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppBar(
    iconBtn1Onclick: () ->Unit,
    iconBtn1Painter: Painter,
    iconBtn1Content: String,
    animatedText: String,
    iconBtn2Onclick: () ->Unit,
    iconBtn2Painter: Painter,
    iconBtn2Content: String
) {
    Row {
        IconButton(
            onClick = iconBtn1Onclick,
            enabled = true,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(5.dp)
        ) {
            Icon(
                painter = iconBtn1Painter,
                contentDescription = iconBtn1Content
            )
        }

        AnimatedText(
            text = animatedText,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(start = 10.dp, end = 10.dp),
            maxLine = 1,
            15.sp
        )

        IconButton(
            onClick = iconBtn2Onclick,
            enabled = true,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(5.dp)
        ) {
            Icon(
                painter = iconBtn2Painter,
                contentDescription = iconBtn2Content
            )
        }
    }
}