package com.xuan.gemma.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun WelcomeLayout(
    modifier: Modifier,
    glideDrawable: Int,
    glideContent: String,
    animatedText: String
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GlideImage(
                model = glideDrawable,
                contentDescription = glideContent,
                modifier = Modifier
                    .width(35.dp)
                    .height(35.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedText(
                text = animatedText,
                modifier = Modifier.fillMaxWidth(0.65f),
                maxLine = Int.MAX_VALUE,
                18.sp
            )
        }
    }
}