package com.example.smartremainder.repository

import com.example.smartremainder.api.NetworkClient
import com.example.smartremainder.model.Content
import com.example.smartremainder.model.GeminiRequest
import com.example.smartremainder.model.Part


class ChatRepository {
    private val geminiApi = NetworkClient.geminiApi

    suspend fun sendMessage(message: String): Result<String> {
        return try {
            val request = GeminiRequest(
                listOf(
                    Content(
                        listOf(
                            Part(text = message))
                    )
                )
            )

            val response = geminiApi.generateContent(
                //Put your API key here
                apiKey = "AIzaSyDKBSAGexERUUJYhThSJgD-4wM0-ouVQxo",
                request = request
            )

            if (response.isSuccessful) {
                val geminiResponse = response.body()
                val reply = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                Result.success(reply ?: "No response from Gemini")
            } else {
                Result.failure(Exception("API Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}