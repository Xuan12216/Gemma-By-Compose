package com.xuan.gemma.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd HH:mm", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
}