package com.xuan.gemma.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    fun getMessageToTitle(message: String): String {
        val trimmedTitle = message
            .lineSequence() // 將訊息分成多行序列
            .filter { it.isNotBlank() } // 過濾掉空行
            .joinToString(" ") // 合併成單行文字
            .take(30) // 限制字數為 30 個字

        return trimmedTitle
    }

    fun downloadFileFromUrl(context: Context, url: String, onProgressUpdate: (String) -> Unit): Boolean {
        var file: File? = null

        return try {
            onProgressUpdate("Downloading model... 0%\n0 MB / 0 MB")

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.MINUTES)
                .writeTimeout(30, TimeUnit.MINUTES)
                .build()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return false

            val contentLength = response.body?.contentLength() ?: -1L
            val inputStream = response.body?.byteStream() ?: return false

            val contentDisposition = response.header("Content-Disposition")
            val fileName = contentDisposition?.substringAfter("filename=")?.trim('"')
            val extension = fileName?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }?.let { ".$it" } ?: ".bin"
            file = File(context.cacheDir, "model$extension")

            val bufferedInputStream = BufferedInputStream(inputStream)
            var totalBytesRead = 0L
            val buffer = ByteArray(8192)

            BufferedOutputStream(FileOutputStream(file)).use { output ->
                var bytesRead: Int
                while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    output.flush()  // 定期刷新緩衝區
                    totalBytesRead += bytesRead

                    // 計算並更新進度
                    if (contentLength > 0) {
                        val progress = ((totalBytesRead * 100) / contentLength).toInt()
                        // 計算目前下載大小和文件總大小
                        val downloadedSizeMB = totalBytesRead / (1024 * 1024)
                        val totalSizeMB = contentLength / (1024 * 1024)

                        // 更新進度字串
                        val progressText = "Downloading model... $progress%\n$downloadedSizeMB MB / $totalSizeMB MB"
                        onProgressUpdate(progressText)
                    }
                }
            }

            // 驗證文件大小
            if (contentLength > 0 && file.length() != contentLength) {
                file.delete()  // 如果大小不匹配，刪除文件
                return false
            }

            true
        }
        catch (e: Exception) {
            e.printStackTrace()
            try { file?.delete() }
            catch (cleanupError: Exception) { cleanupError.printStackTrace() }
            false
        }
        finally { System.gc() }
    }
}