package com.xuan.gemini.data.rememberObject

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import com.xuan.gemini.data.stateObject.TransformSettingState

@Composable
fun rememberSettingState(): TransformSettingState {
    return rememberSaveable(saver = TransformSettingState.Saver) {
        TransformSettingState()
    }
}