package com.xuan.gemma.viewmodel

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.xuan.gemma.util.FileManagerHelper
import com.xuan.gemma.util.PickImageFunc
import com.xuan.gemma.util.PickImageUsingCamera
import com.xuan.gemma.util.RecordFunc

class MainViewModel : ViewModel() {

    val pickImage = PickImageFunc()
    val pickImageUsingCamera = PickImageUsingCamera()
    val recordFunc = RecordFunc()

    lateinit var fileManagerHelper: FileManagerHelper

    fun init(activity: ComponentActivity, context: Context) {
        pickImage.init(activity, context)
        pickImageUsingCamera.init(activity, context)
        recordFunc.init(activity, context)

        fileManagerHelper = FileManagerHelper(activity).apply { initPickFileLauncher() }
    }

    companion object {
        fun getFactory() = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return MainViewModel() as T
            }
        }
    }
}