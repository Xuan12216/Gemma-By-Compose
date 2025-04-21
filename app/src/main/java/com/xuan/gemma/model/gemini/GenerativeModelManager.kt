package com.xuan.gemma.model.gemini

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.java.GenerativeModelFutures
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.xuan.gemma.activity.SettingMainActivity
import com.xuan.gemma.`object`.Constant
import com.xuan.gemma.util.secure.SecuritySharedPreference

class GenerativeModelManager {
    private var generativeModel: GenerativeModelFutures? = null

    //===============================================
    private var userContent: Content? = null
    private var modelContent: Content? = null
    private var generationConfig: GenerationConfig? = null
    private var safetyList: List<SafetySetting>? = null

    //modelGenerateConfig================
    private var temperature: Float = 0.9f
    private var topP: Float = 0.95f
    private var topK: Int = 3
    private var maxOutputToken: Int = 2048
    private var candidateCount: Int = 1
    private val stopSequences: MutableList<String> = mutableListOf()
    private var preferences: SharedPreferences? = null
    private val safeList = arrayOfNulls<String>(4)

    fun initializeGenerativeModel(context: Context) {
        preferences = context.getSharedPreferences(Constant.GEMINI, Context.MODE_PRIVATE)
        val pres = SecuritySharedPreference(context, Constant.GEMINI, Context.MODE_PRIVATE)
        resetModel()
        generateConfig()
        setSafetySetting()
        // 初始化 Generative Model
        val api: String = pres.getString(Constant.API_KEY, "") ?: ""
        if (api.isEmpty()) return

        val gm1 = GenerativeModel("gemini-2.0-flash", api, generationConfig, safetyList)

        // 使用 GenerativeModelFutures 创建 GenerativeModelFutures 实例
        generativeModel = GenerativeModelFutures.from(gm1)

        createHistoryData()
    }

    fun checkApiKey(context: Context) {
        val pres = SecuritySharedPreference(context, Constant.GEMINI, Context.MODE_PRIVATE)
        if (!pres.contains(Constant.API_KEY)) {
            val intent = Intent(context, SettingMainActivity::class.java)
            intent.putExtra("id", "3")
            context.startActivity(intent)
        }
    }

    private fun setSafetySetting() {
        safeList[0] = preferences!!.getString("harassment", "0")
        safeList[1] = preferences!!.getString("hate_speech", "0")
        safeList[2] = preferences!!.getString("sexually_explicit", "0")
        safeList[3] = preferences!!.getString("dangerous_content", "0")

        val harassmentSafety = SafetySetting(HarmCategory.HARASSMENT, getBlockThreshold(safeList[0]))
        val hateSpeechSafety = SafetySetting(HarmCategory.HATE_SPEECH, getBlockThreshold(safeList[1]))
        val sexuallyExplicitSafety = SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, getBlockThreshold(safeList[2]))
        val dangerousContentSafety = SafetySetting(HarmCategory.DANGEROUS_CONTENT, getBlockThreshold(safeList[3]))
        safetyList = listOf(harassmentSafety, hateSpeechSafety, sexuallyExplicitSafety, dangerousContentSafety)
    }

    private fun getBlockThreshold(s: String?): BlockThreshold {
        return when (s) {
            "0" -> BlockThreshold.NONE
            "1" -> BlockThreshold.ONLY_HIGH
            "2" -> BlockThreshold.MEDIUM_AND_ABOVE
            "3" -> BlockThreshold.LOW_AND_ABOVE
            else -> BlockThreshold.UNSPECIFIED
        }
    }

    private fun generateConfig() {
        temperature = preferences!!.getFloat("temperature", temperature)
        topP = preferences!!.getFloat("topP", topP)
        topK = preferences!!.getInt("topK", topK)
        maxOutputToken = preferences!!.getInt("maxOutputToken", maxOutputToken)
        val stp = preferences!!.getString("stop", "")
        candidateCount = preferences!!.getInt("candidateCount", candidateCount)

        stopSequences.clear()
        if (stp != null) { stopSequences.add(stp) }

        val configBuilder: GenerationConfig.Builder = GenerationConfig.builder()
        configBuilder.temperature = temperature
        configBuilder.topK = topK
        configBuilder.topP = topP
        configBuilder.maxOutputTokens = maxOutputToken
        configBuilder.candidateCount = 1
        if (!stp.isNullOrEmpty()) configBuilder.stopSequences = ArrayList(stopSequences)

        generationConfig = configBuilder.build()
    }

    private fun createHistoryData() {
        val userContentBuilder: Content.Builder = Content.Builder()
        userContentBuilder.role = "user"
        userContentBuilder.text("Hello.")
        userContent = userContentBuilder.build()

        val modelContentBuilder: Content.Builder = Content.Builder()
        modelContentBuilder.role = "model"
        modelContentBuilder.text("Great to meet you.")
        modelContent = modelContentBuilder.build()
    }

    private fun resetModel() {
        generativeModel = null
        userContent = null
        modelContent = null
        generationConfig = null
        safetyList = null
    }

    fun getSafetyList(): Array<String?> {
        return safeList
    }

    fun getStopSequences(): List<String?> {
        return stopSequences
    }

    fun getUserContent() : Content? {
        return userContent
    }

    fun getModelContent() : Content? {
        return modelContent
    }

    fun getGenerativeModel() : GenerativeModelFutures? {
        return generativeModel
    }
}
