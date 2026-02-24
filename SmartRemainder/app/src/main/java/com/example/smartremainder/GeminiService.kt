package com.example.smartremainder

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object GeminiService {
    private const val API_KEY = "AIzaSyDPrY3CouLIk6lOtzX8oIP0HxtMQ7LARRs"  // Replace with actual key
    private const val URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$API_KEY"

    fun sendMessage(message: String, callback: (String) -> Unit) {
        val client = OkHttpClient()

        val content = JSONObject().apply {
            put("parts", JSONArray().apply {
                put(JSONObject().put("text", message))
            })
        }

        val bodyJson = JSONObject().apply {
            put("contents", JSONArray().put(content))
        }

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            bodyJson.toString()
        )

        val request = Request.Builder()
            .url(URL)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Gemini Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody == null) {
                    callback("Gemini failed: ${response.code} ${response.message}")
                    return
                }

                try {
                    val jsonResponse = JSONObject(responseBody)
                    val reply = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    callback(reply.trim())
                } catch (e: Exception) {
                    callback("Error parsing Gemini response: ${e.message}")
                }
            }
        })
    }
}