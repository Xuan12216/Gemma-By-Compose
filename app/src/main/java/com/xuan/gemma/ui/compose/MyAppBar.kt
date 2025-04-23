package com.xuan.gemma.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppBar(
    textInputEnabled: Boolean,
    iconBtn1Onclick: () ->Unit,
    iconBtn1Painter: Painter,
    iconBtn1Content: String,
    animatedText: String,
    iconBtn2Onclick: (() ->Unit)? = null,
    iconBtn2Painter: Painter? = null,
    iconBtn2Content: String = "",
    modifier: Modifier = Modifier
) {
    Row (modifier = modifier){
        IconButton(
            onClick = iconBtn1Onclick,
            enabled = textInputEnabled,
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

        if (null != iconBtn2Painter && null != iconBtn2Onclick) {
            IconButton(
                onClick = iconBtn2Onclick,
                enabled = textInputEnabled,
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
        else {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(16.dp)
                    .size(24.dp)
                    .background(color = Color.Transparent)
            )
        }
    }
}