package com.xuan.gemma.util

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

class PickImageFunc {
    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private lateinit var imageResize: ImageResize
    private var callback: OnImageResultCallback? = null

    fun init(activity: ComponentActivity,context: Context) {
        imageResize = ImageResize(context)
        pickMedia = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia(), this::saveImage)
    }

    fun startPickImage(callback: OnImageResultCallback) {
        this.callback = callback
        val mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(mediaType)
            .build()
        pickMedia?.launch(request)
    }

    private fun saveImage(imageUri: Uri?) {
        imageUri?.let {
            imageResize.imgResize(it) { compressedUri ->
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