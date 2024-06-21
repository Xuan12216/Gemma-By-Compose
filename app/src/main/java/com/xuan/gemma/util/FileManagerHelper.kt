package com.xuan.gemma.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import java.io.File
import java.io.FileOutputStream

class FileManagerHelper(
    private val activity: Activity,
    private val pickFileLauncher: ActivityResultLauncher<Intent>
) {

    private fun printFilesInCacheDir(directory: File) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                printFilesInCacheDir(file) // 递归调用以处理子文件夹
            } else {
                println("File in cache directory: ${file.absolutePath}")
            }
        }
    }

    // 檢查指定文件是否存在
    fun checkFileExists(fileName: String): Boolean {
        val tempDir = activity.cacheDir // 使用临时目录

        //printFilesInCacheDir(tempDir)

        val file = File(tempDir, fileName)
        return file.exists()
    }

    fun openFileManager() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickFileLauncher.launch(intent)
    }

    // 处理用户选择的文件 URI
    fun handleSelectedFile(uri: Uri) {
        println("Selected file URI: $uri")
        // 在这里处理选择的文件
        // 保存文件到临时目录
        val tempFile = saveFileToTempDir(activity, uri)
        if (tempFile != null) {
            println("File saved to temp directory: ${tempFile.absolutePath}")
        } else {
            println("Failed to save file to temp directory")
        }
    }

    // 在FileManagerHelper类中添加一个方法来保存文件到临时目录
    private fun saveFileToTempDir(context: Context, uri: Uri): File? {
        // 获取文件的输入流
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        // 获取用户选择的文件名
        val fileName = "model.bin"
        if (fileName.isNullOrEmpty()) return null

        // 创建临时文件
        val tempDir = context.cacheDir // 获取应用的临时目录
        val tempFile = File(tempDir, fileName) // 使用用户选择的文件名创建临时文件

        // 将输入流中的文件内容复制到临时文件中
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        // 返回保存的临时文件
        return tempFile
    }
}
