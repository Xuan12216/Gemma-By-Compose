package com.xuan.gemma.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import com.xuan.gemma.R
import java.util.Locale

class RecordFunc(activity: ComponentActivity, private val context: Context) {
    private val activityResultRegistry: ActivityResultRegistry = activity.activityResultRegistry

    fun startRecordFunc(callback: RecordResultCallback) {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.record_value))
            }

            val launcher: ActivityResultLauncher<Intent> = activityResultRegistry.register(
                "key",
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val resultArray = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val recognizedText = resultArray?.firstOrNull() ?: ""
                    callback.onResult(recognizedText)
                }
            }
            launcher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, " ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun interface RecordResultCallback {
        fun onResult(result: String)
    }
}