package com.xuan.gemma.ui.compose

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.xuan.gemma.activity.LocalMainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    bottomSheetState: SheetState,
    onDismiss: () -> Unit,
    options: List<Triple<Int, String, Int>>,
    onCallbackImageUri: (Uri) -> Unit
) {
    val scope = rememberCoroutineScope()
    val pickImageFunc = LocalMainViewModel.current.pickImage
    val pickImageUsingCamera = LocalMainViewModel.current.pickImageUsingCamera

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            scope.launch { bottomSheetState.hide() }
        },
        sheetState = bottomSheetState,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                if (option.third == 1) {
                                    pickImageUsingCamera.startPickImage { compressedUri ->
                                        onCallbackImageUri(compressedUri)
                                    }
                                } else if (option.third == 2) {
                                    pickImageFunc.startPickImage { compressedUri ->
                                        onCallbackImageUri(compressedUri)
                                    }
                                }
                            }
                    ) {
                        FilledIconButton(
                            onClick = {},
                            enabled = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                                .width(30.dp)
                                .height(30.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = option.first),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(18.dp)
                            )
                        }
                        Text(
                            text = option.second,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        },
        scrimColor = Color.Black.copy(alpha = 0.6f)
    )
}