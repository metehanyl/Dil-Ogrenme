package com.metehanyl.dilogrenme.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class ChatRequestMessage(val role: String, val content: String)

@Serializable
private data class ChatRequest(
    val model: String,
    val messages: List<ChatRequestMessage>,
    val temperature: Double = 0.7
)

@Serializable
private data class ChatChoice(val message: ChatRequestMessage)

@Serializable
private data class ChatResponse(val choices: List<ChatChoice> = emptyList())

/**
 * Talks to any OpenAI-compatible "/chat/completions" endpoint using the user's own API key,
 * entered in Settings. No key is bundled with the app; nothing is sent anywhere unless the
 * user has configured their own provider.
 */
class AiConversationClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun sendChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        history: List<ChatRequestMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf(ChatRequestMessage("system", systemPrompt))
            messages.addAll(history)
            val requestBody = json.encodeToString(ChatRequest(model = model, messages = messages))
            val url = baseUrl.trimEnd('/') + "/chat/completions"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("HTTP ${response.code}: ${bodyText.take(300)}")
                    )
                }
                val parsed = json.decodeFromString<ChatResponse>(bodyText)
                val reply = parsed.choices.firstOrNull()?.message?.content
                if (reply.isNullOrBlank()) {
                    Result.failure(Exception("Empty response from AI provider"))
                } else {
                    Result.success(reply.trim())
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
