package com.xuan.gemma.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

object AppUtils {
    fun registerPickFileLauncher(
        activity: ComponentActivity,
        onFilePicked: (Uri) -> Unit
    ): ActivityResultLauncher<Intent> {
        return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    onFilePicked(uri)
                }
            }
        }
    }

    fun handleSelectedFile(uri: Uri, activity: ComponentActivity, pickFileLauncher: ActivityResultLauncher<Intent>) {
        val fileManagerHelper = FileManagerHelper(activity, pickFileLauncher)
        fileManagerHelper.handleSelectedFile(uri)
    }
}