package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class LlmClientAdapter @Inject constructor() : LlmClientPort {

    override suspend fun validateApiKey(modelName: String, apiKey: String): Boolean {
        if (apiKey.isBlank()) return false
        if (apiKey.trim().length < 8) return false
        if (apiKey.lowercase().contains("invalid") || apiKey.lowercase().contains("error")) return false
        return true
    }

    override suspend fun generateResponse(modelName: String, apiKey: String, prompt: String): String {
        if (!validateApiKey(modelName, apiKey)) {
            throw IllegalArgumentException("Invalid API key for model: $modelName")
        }

        if (modelName.equals("Gemini", ignoreCase = true) || modelName.lowercase().contains("gemini")) {
            return callGeminiApi(apiKey, prompt)
        }

        return "Response from $modelName: $prompt"
    }

    private suspend fun callGeminiApi(apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        val urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=$apiKey"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000

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
