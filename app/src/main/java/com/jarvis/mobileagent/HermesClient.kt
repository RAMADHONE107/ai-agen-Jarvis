package com.jarvis.mobileagent

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HermesClient(
    private val baseUrl: String = "http://127.0.0.1:8642"
) {
    fun testConnection(apiKey: String): String {
        val connection = openConnection("/v1/models", "GET", apiKey)
        return try {
            val code = connection.responseCode
            val body = readBody(connection, code)
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code: $body")
            }
            val models = JSONObject(body).optJSONArray("data")
            val names = mutableListOf<String>()
            if (models != null) {
                for (i in 0 until models.length()) {
                    names += models.optJSONObject(i)?.optString("id").orEmpty()
                }
            }
            "Terhubung ke Hermes. Model: " + names.filter { it.isNotBlank() }.joinToString()
        } finally {
            connection.disconnect()
        }
    }

    fun sendMessage(apiKey: String, message: String): String {
        val payload = JSONObject()
            .put("model", "hermes-agent")
            .put("stream", false)
            .put(
                "messages",
                org.json.JSONArray().put(
                    JSONObject().put("role", "user").put("content", message)
                )
            )
        val connection = openConnection("/v1/chat/completions", "POST", apiKey)
        return try {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val body = readBody(connection, code)
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code: $body")
            }
            val choices = JSONObject(body).optJSONArray("choices")
            val content = choices?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                .orEmpty()
            content.ifBlank { "Hermes merespons, tetapi isi jawaban kosong." }
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(path: String, method: String, apiKey: String): HttpURLConnection {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10000
            readTimeout = 90000
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Accept", "application/json")
        }
        return connection
    }

    private fun readBody(connection: HttpURLConnection, code: Int): String {
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        return stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
    }
}
