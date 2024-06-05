package com.xuan.gemini.util

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PickImageUsingCamera(activity: ComponentActivity, private val context: Context) {
    private val imageResize: ImageResize = ImageResize(context)
    private var takePicture: ActivityResultLauncher<Uri>? = null
    private var callback: OnImageResultCallback? = null
    private var photoUri: Uri? = null

    init {
        activity.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                takePicture = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                    saveTakenImage(success)
                }
            }
        })
    }

    fun startPickImage(callback: OnImageResultCallback) {
        this.callback = callback
        startTakePicture()
    }

    private fun startTakePicture() {
        try {
            val photoFile = createImageFile()
            if (photoFile != null) {
                val uri = FileProvider.getUriForFile(context, "com.xuan.gemini.fileprovider", photoFile)
                photoUri = uri
                takePicture?.launch(uri)
            }
        }
        catch (_: IOException) { }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = context.getExternalFilesDir(null)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun saveTakenImage(success: Boolean) {
        if (success && photoUri != null) {
            imageResize.imgResize(photoUri!!) { compressedUri ->
                if (compressedUri != null) {
                    callback?.onResult(compressedUri)
                }
            }
        }
    }

    fun interface OnImageResultCallback {
        fun onResult(compressedUri: Uri)
    }
}