package com.xuan.gemma.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.xuan.gemma.R
import com.xuan.gemma.`object`.Constant
import com.xuan.gemma.ui.compose.AppBar
import com.xuan.gemma.ui.theme.GemmaTheme
import com.xuan.gemma.util.secure.SecuritySharedPreference

class SettingMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra("id")

        setContent {
            GemmaTheme {
                when (id) {
                    "3" -> SetApiScreen(
                        onFinishActivity = { finish() },
                        context = LocalContext.current
                    )
                }
            }
        }
    }
}

@Composable
fun SetApiScreen(
    onFinishActivity: () -> Unit,
    context: Context
) {
    var inputText by remember { mutableStateOf("") }
    val preferences = SecuritySharedPreference(context, Constant.GEMINI, Context.MODE_PRIVATE)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppBar(
                textInputEnabled = true,
                iconBtn1Onclick = { onFinishActivity() },
                iconBtn1Painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                iconBtn1Content = "Open Drawer Navigation",
                animatedText = context.resources.getStringArray(R.array.settingsItem)[3]
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = context.getString(
                        if (preferences.contains(Constant.API_KEY)) R.string.status_true
                        else R.string.status_false
                    ))

                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text(context.getString(R.string.EditText_hint_api)) },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )

                    ClickableText(
                        text = buildAnnotatedString {
                            append(context.getString(R.string.get_api_hint))
                            pushStringAnnotation(tag = "url", annotation = context.resources.getString(R.string.get_api_key_url))
                            withStyle(style = SpanStyle(color = Color.Blue)) {
                                append("這裏")
                            }
                        },
                        onClick = { offset ->
                            val annotation = buildAnnotatedString {
                                append(context.getString(R.string.get_api_hint))
                                pushStringAnnotation(tag = "url", annotation = context.resources.getString(R.string.get_api_key_url))
                                withStyle(style = SpanStyle(color = Color.Blue)) {
                                    append("這裏")
                                }
                            }.getStringAnnotations("url", offset, offset).firstOrNull()

                            annotation?.let {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.padding(16.dp)
                    )

                    Button(
                        onClick = {
                            if (inputText.isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.editText_empty), Toast.LENGTH_SHORT).show()
                            }
                            else {
                                preferences.edit().putString(Constant.API_KEY, inputText).apply()
                                Toast.makeText(context, context.getString(R.string.successfully_toast), Toast.LENGTH_SHORT).show()
                                onFinishActivity()
                            }
                        }
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}