package com.xuan.gemma.data.stateObject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import com.jvziyaoyao.scale.zoomable.previewer.VerticalDragType

private val VERTICAL_DRAG_ENABLE = VerticalDragType.UpAndDown
val DEFAULT_VERTICAL_DRAG = VERTICAL_DRAG_ENABLE
const val DEFAULT_LOADER_ERROR = false
const val DEFAULT_TRANSFORM_ENTER = true
const val DEFAULT_TRANSFORM_EXIT = true
const val DEFAULT_ANIMATION_DURATION = 400
const val DEFAULT_DATA_REPEAT = 1

class TransformSettingState {

    var loaderError by mutableStateOf(DEFAULT_LOADER_ERROR)
    var verticalDrag by mutableStateOf(DEFAULT_VERTICAL_DRAG)
    var transformEnter by mutableStateOf(DEFAULT_TRANSFORM_ENTER)
    var transformExit by mutableStateOf(DEFAULT_TRANSFORM_EXIT)
    var animationDuration by mutableIntStateOf(DEFAULT_ANIMATION_DURATION)
    var dataRepeat by mutableIntStateOf(DEFAULT_DATA_REPEAT)

    companion object {
        val Saver: Saver<TransformSettingState, *> = mapSaver(
            save = {
                mapOf<String, Any>(
                    it::loaderError.name to it.loaderError,
                    it::verticalDrag.name to it.verticalDrag,
                    it::transformEnter.name to it.transformEnter,
                    it::transformExit.name to it.transformExit,
                    it::animationDuration.name to it.animationDuration,
                    it::dataRepeat.name to it.dataRepeat,
                )
            },
            restore = {
                val state = TransformSettingState()
                state.loaderError = it[state::loaderError.name] as Boolean
                state.verticalDrag = it[state::verticalDrag.name] as VerticalDragType
                state.transformEnter = it[state::transformEnter.name] as Boolean
                state.transformExit = it[state::transformExit.name] as Boolean
                state.animationDuration = it[state::animationDuration.name] as Int
                state.dataRepeat = it[state::dataRepeat.name] as Int
                state
            }
        )
    }
}