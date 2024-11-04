package com.xuan.gemma.util.secure

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * AES加密解密工具
 */
class EncryptUtil private constructor(context: Context) {

    private val key: String
    private val TAG = EncryptUtil::class.java.simpleName
    private var instance: EncryptUtil? = null

    init {
        val serialNo = getDeviceSerialNumber(context)
        //加密随机字符串生成AES key
        key = SHA("$serialNo#\$ERDTS\$D%F^Gojikbh")!!.substring(0, 16)
        Log.e(TAG, key)
    }

    companion object {
        @Volatile
        private var instance: EncryptUtil? = null

        /**
         * 单例模式
         * @param context context
         * @return
         */
        fun getInstance(context: Context): EncryptUtil {
            return instance ?: synchronized(this) {
                instance ?: EncryptUtil(context).also { instance = it }
            }
        }
    }

    /**
     * Gets the hardware serial number of this device.
     *
     * @return serial number or Settings.Secure.ANDROID_ID if not available.
     */
    @SuppressLint("HardwareIds")
    private fun getDeviceSerialNumber(context: Context): String {
        return try {
            val deviceSerial = Build::class.java.getField("SERIAL").get(null) as String
            if (TextUtils.isEmpty(deviceSerial)) {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } else {
                deviceSerial
            }
        } catch (ignored: Exception) {
            // Fall back to Android_ID
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }

    /**
     * SHA加密
     * @param strText 明文
     * @return
     */
    private fun SHA(strText: String): String? {
        var strResult: String? = null
        // 是否是有效字符串
        if (!strText.isEmpty()) {
            try {
                // SHA 加密开始
                val messageDigest = MessageDigest.getInstance("SHA-256")
                // 传入要加密的字符串
                messageDigest.update(strText.toByteArray())
                val byteBuffer = messageDigest.digest()
                val strHexString = StringBuilder()
                for (byte in byteBuffer) {
                    val hex = Integer.toHexString(0xff and byte.toInt())
                    if (hex.length == 1) {
                        strHexString.append('0')
                    }
                    strHexString.append(hex)
                }
                strResult = strHexString.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
        }
        return strResult
    }

    /**
     * AES128加密
     * @param plainText 明文
     * @return
     */
    fun encrypt(plainText: String): String? {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val keyspec = SecretKeySpec(key.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keyspec)
            val encrypted = cipher.doFinal(plainText.toByteArray())
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * AES128解密
     * @param cipherText 密文
     * @return
     */
    fun decrypt(cipherText: String): String? {
        return try {
            val encrypted1 = Base64.decode(cipherText, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val keyspec = SecretKeySpec(key.toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, keyspec)
            val original = cipher.doFinal(encrypted1)
            String(original)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}