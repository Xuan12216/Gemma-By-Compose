package com.xuan.gemini.ui.compose

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.xuan.gemini.util.RecordFunc

@Composable
fun TextFieldLayout(
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
    textFieldContent: String
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
    ){
        //takeImageIcon=====
        FilledIconButton(
            onClick = filledIconBtnOnClick,
            enabled = true,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp, end = 5.dp, top = 5.dp)
        ) {
            Icon(
                painter = filledIconPainter,
                contentDescription = filledIconContent
            )
        }

        //textField=====
        OutlinedTextField(
            value = textFieldText,
            onValueChange = { newText ->
                onTextFieldChange(newText)
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                .align(Alignment.Bottom),
            label = { Text(textFieldHint) },
            shape = RoundedCornerShape(50.dp),
            keyboardOptions = KeyboardOptions.Default,
            keyboardActions = KeyboardActions.Default,
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (textFieldText.isEmpty()) {
                            recordFunc.startRecordFunc { result ->
                                if (result.isNotEmpty()) {
                                    onTextFieldChange(result)
                                }
                            }
                        }
                        else onTextFieldAdd(textFieldText)
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