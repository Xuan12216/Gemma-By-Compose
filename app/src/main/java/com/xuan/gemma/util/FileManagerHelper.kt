package com.xuan.gemma.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FileManagerHelper(
    private val activity: ComponentActivity
) {
    private lateinit var pickFileLauncher: ActivityResultLauncher<Intent>
    var isLoading = mutableStateOf(false)

    // 初始化文件选择器的注册
    fun initPickFileLauncher() {
        pickFileLauncher = AppUtils.registerPickFileLauncher(activity) { uri ->
            isLoading.value = true
            handleSelectedFile(uri) {
                isLoading.value = false
            }
        }
    }

    // 打开文件管理器
    fun openFileManager() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickFileLauncher.launch(intent)
    }

    // 处理用户选择的文件 URI
    private fun handleSelectedFile(uri: Uri, onComplete: () -> Unit = {}) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            Log.d("FileManager", "Selected file URI: $uri")

            // 在保存新文件之前，删除所有包含 "model" 的文件
            deleteModelFiles(activity.cacheDir)

            // 保存文件到临时目录
            val tempFile = saveFileToTempDir(activity, uri)
            if (tempFile != null) Log.d("FileManager", "File saved to temp directory: ${tempFile.absolutePath}")
            else Log.e("FileManager", "Failed to save file to temp directory")

            // 完成后回到主线程执行回调
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // 检查指定文件是否存在（不指定文件格式）
    fun checkFileExists(fileNameWithoutExtension: String): Boolean {
        val tempDir = activity.cacheDir

        val files = tempDir.listFiles() ?: return false

        // 检查文件名是否匹配，不考虑扩展名
        return files.any { file ->
            val nameWithoutExtension = file.name.substringBeforeLast(".")
            nameWithoutExtension == fileNameWithoutExtension
        }
    }

    // 删除所有包含 "model" 的文件
    private fun deleteModelFiles(directory: File) {
        val files = directory.listFiles() ?: return
        files.forEach { file ->
            if (file.name.contains("model", ignoreCase = true)) {
                if (file.delete()) Log.d("FileManager", "Deleted file: ${file.absolutePath}")
                else Log.e("FileManager", "Failed to delete file: ${file.absolutePath}")
            }
        }
    }

    // 保存文件到临时目录
    private fun saveFileToTempDir(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        val originalFileName = getFileName(context, uri) ?: return null
        val fileExtension = getFileExtension(originalFileName) ?: return null

        val tempDir = context.cacheDir
        val tempFile = File(tempDir, "model.$fileExtension")

        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        return tempFile
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        } else {
            fileName = uri.path?.let { path ->
                File(path).name
            }
        }
        return fileName
    }

    private fun getFileExtension(fileName: String): String? {
        val dotIndex = fileName.lastIndexOf(".")
        return if (dotIndex != -1) {
            fileName.substring(dotIndex + 1)
        } else {
            null
        }
    }
}
