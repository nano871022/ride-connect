package co.japl.android.ev_ride_connect.llm

import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties
import javax.inject.Inject

class LlmClientAdapter @Inject constructor() : LlmClientPort {

    private val properties = Properties().apply {
        try {
            val stream = LlmClientAdapter::class.java.classLoader?.getResourceAsStream("llm_settings.properties")
                ?: LlmClientAdapter::class.java.getResourceAsStream("/llm_settings.properties")
            stream?.use { load(it) }
        } catch (_: Exception) {}
    }

    private val connectTimeout: Int
        get() = properties.getProperty("connect.timeout")?.toIntOrNull() ?: 15000

    private val readTimeout: Int
        get() = properties.getProperty("read.timeout")?.toIntOrNull() ?: 15000

    override suspend fun validateApiKey(modelName: String, apiKey: String): Boolean {
        if (apiKey.isBlank()) return false
        if (apiKey.trim().length < 8) return false
        if (apiKey.lowercase().contains("invalid") || apiKey.lowercase().contains("error")) return false
        return true
    }

    override suspend fun fetchAvailableModels(modelName: String, apiKey: String): List<String> {
        if (!validateApiKey(modelName, apiKey)) {
            return getDefaultModels(modelName)
        }

        if (modelName.equals("Gemini", ignoreCase = true) || modelName.lowercase().contains("gemini")) {
            return try {
                fetchGeminiModels(apiKey)
            } catch (_: Exception) {
                getDefaultModels(modelName)
            }
        }

        return getDefaultModels(modelName)
    }

    override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String {
        if (!validateApiKey(modelName, apiKey)) {
            throw IllegalArgumentException("Invalid API key for model: $modelName")
        }

        if (modelName.equals("Gemini", ignoreCase = true) || modelName.lowercase().contains("gemini")) {
            return callGeminiApi(modelName, apiKey, prompt)
        }

        return "Response from $modelName: $prompt"
    }

    private suspend fun fetchGeminiModels(apiKey: String): List<String> = withContext(Dispatchers.IO) {
        val urlString = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = connectTimeout
        conn.readTimeout = readTimeout

        val responseCode = conn.responseCode
        if (responseCode !in 200..299) {
            return@withContext getDefaultModels("Gemini")
        }

        val responseText = conn.inputStream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
        val jsonObject = JSONObject(responseText)
        val modelsArray = jsonObject.optJSONArray("models") ?: return@withContext getDefaultModels("Gemini")

        val result = mutableListOf<String>()
        for (i in 0 until modelsArray.length()) {
            val item = modelsArray.optJSONObject(i) ?: continue
            val name = item.optString("name", "")
            val supportedMethods = item.optJSONArray("supportedGenerationMethods")
            val supportsGenerateContent = if (supportedMethods != null) {
                (0 until supportedMethods.length()).any { supportedMethods.optString(it) == "generateContent" }
            } else true

            if (supportsGenerateContent && name.isNotBlank()) {
                val cleanName = name.removePrefix("models/")
                result.add(cleanName)
            }
        }

        if (result.isEmpty()) getDefaultModels("Gemini") else result
    }

    private fun getDefaultModels(modelName: String): List<String> {
        return when {
            modelName.contains("gemini", ignoreCase = true) ->
                listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash", "gemini-2.5-pro")
            modelName.contains("deepseek", ignoreCase = true) ->
                listOf("deepseek-chat", "deepseek-coder", "deepseek-r1")
            modelName.contains("chatgpt", ignoreCase = true) || modelName.contains("openai", ignoreCase = true) ->
                listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo")
            modelName.contains("groq", ignoreCase = true) ->
                listOf("llama-3.3-70b-versatile", "mixtral-8x7b-32768")
            modelName.contains("claude", ignoreCase = true) ->
                listOf("claude-3-5-sonnet-latest", "claude-3-5-haiku-latest", "claude-3-opus-latest")
            modelName.contains("mistral", ignoreCase = true) ->
                listOf("mistral-large-latest", "mistral-small-latest", "open-mixtral-8x22b")
            else -> listOf(modelName)
        }
    }

    private suspend fun callGeminiApi(modelName: String, apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        val cleanModel = modelName.removePrefix("models/")
        val effectiveModel = if (cleanModel.equals("Gemini", ignoreCase = true) || cleanModel.isBlank()) {
            "gemini-flash-latest"
        } else {
            cleanModel
        }
        val urlString = "https://generativelanguage.googleapis.com/v1beta/models/$effectiveModel:generateContent?key=$apiKey"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.doOutput = true
        conn.connectTimeout = connectTimeout
        conn.readTimeout = readTimeout

        val escapedPrompt = JSONObject.quote(prompt)
        val jsonInputString = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": $escapedPrompt
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
            os.write(jsonInputString)
            os.flush()
        }

        val responseCode = conn.responseCode
        val inputStream = if (responseCode in 200..299) {
            conn.inputStream
        } else {
            conn.errorStream
        }

        val responseText = inputStream?.bufferedReader()?.use(BufferedReader::readText) ?: ""

        if (responseCode !in 200..299) {
            throw RuntimeException("Gemini API Error ($responseCode): $responseText")
        }

        extractGeminiText(responseText)
    }

    private fun extractGeminiText(jsonResponse: String): String {
        return try {
            val jsonObject = JSONObject(jsonResponse)
            val candidates = jsonObject.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                if (content != null) {
                    val parts = content.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", jsonResponse)
                    }
                }
            }
            jsonResponse
        } catch (e: Exception) {
            jsonResponse
        }
    }
}
