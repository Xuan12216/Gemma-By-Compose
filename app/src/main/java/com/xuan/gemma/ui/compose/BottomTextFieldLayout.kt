package com.xuan.gemma.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.xuan.gemma.util.RecordFunc

@Composable
fun TextFieldLayout(
    textInputEnabled: Boolean,
    filledIconBtnOnClick: () -> Unit,
    filledIconPainter: Painter,
    filledIconContent: String,
    textFieldText: String,
    onTextFieldChange: (String) -> Unit,
    onTextFieldAdd: (String) -> Unit,
    textFieldHint: String,
    recordFunc: RecordFunc,
    textFieldTrailingIcon1: Painter,
    textFieldTrailingIcon2: Painter,
    textFieldContent: String,
    isShowButton: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp, top = 5.dp)
    ){
        if (isShowButton) {
            //takeImageIcon=====
            FilledIconButton(
                onClick = filledIconBtnOnClick,
                enabled = true,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 16.dp)
            ) {
                Icon(
                    painter = filledIconPainter,
                    contentDescription = filledIconContent
                )
            }
        }

        //textField=====
        TextField(
            value = textFieldText,
            onValueChange = { newText ->
                onTextFieldChange(newText)
            },
            modifier = Modifier
                .weight(1f)
                .padding(start = if (isShowButton) 5.dp else 16.dp, end = 16.dp)
                .align(Alignment.Bottom)
                .focusRequester(focusRequester),
            label = { Text(textFieldHint) },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            enabled = true,
            keyboardOptions = KeyboardOptions.Default,
            keyboardActions = KeyboardActions.Default,
            trailingIcon = {
                IconButton(
                    enabled = textInputEnabled,
                    onClick = {
                        if (textFieldText.isEmpty()) {
                            recordFunc.startRecordFunc { result ->
                                if (result.isNotEmpty()) {
                                    onTextFieldChange(result)
                                }
                            }
                        }
                        else {
                            onTextFieldAdd(textFieldText)
                            focusManager.clearFocus()
                        }
                    }
                ) {
                    Icon(
                        painter = if (textFieldText.isEmpty()) textFieldTrailingIcon1 else textFieldTrailingIcon2,
                        contentDescription = textFieldContent
                    )
                }
            }
        )
    }
}