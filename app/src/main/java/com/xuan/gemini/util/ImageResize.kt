package com.xuan.gemini.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageResize(private val context: Context) {

    fun imgResize(imageUri: Uri?, callback: ImageResizeCallback) {
        if (imageUri != null) {
            Glide.with(context)
                .asBitmap()
                .load(imageUri)
                .override(500)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val compressedUri = compressAndSaveBitmap(resource)
                        callback.onImageResized(compressedUri)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        callback.onImageResized(imageUri)
                    }
                })
        }
    }

    private fun compressAndSaveBitmap(bitmap: Bitmap): Uri {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val compressedFile = saveByteArrayToFile(byteArrayOutputStream.toByteArray())
        return Uri.fromFile(compressedFile)
    }

    private fun saveByteArrayToFile(byteArray: ByteArray): File? {
        return try {
            val tempFile = File.createTempFile("compressed_image", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { fileOutputStream ->
                fileOutputStream.write(byteArray)
            }
            tempFile
        }
        catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun interface ImageResizeCallback {
        fun onImageResized(compressedUri: Uri?)
    }
}