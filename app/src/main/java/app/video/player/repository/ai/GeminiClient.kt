/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.video.player.repository.ai

import app.video.player.network.awaitResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GeminiClient(
  private val client: OkHttpClient,
  private val json: Json,
) : AiClient {
  companion object {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
    private val JSON_MEDIA_TYPE = "application/json".toMediaType()
  }

  private val apiClient: OkHttpClient = client.withAiChatTimeouts()

  override suspend fun fetchModels(apiKey: String): Result<List<AiModelInfo>> =
    withContext(Dispatchers.IO) {
      runCatchingCancellable {
        val request =
          Request
            .Builder()
            .url("$BASE_URL/models")
            .header("x-goog-api-key", apiKey)
            .get()
            .build()

        apiClient.newCall(request).awaitResponse().use { response ->
          val body = response.body.string()
          if (!response.isSuccessful) {
            throw IllegalStateException("Gemini API error ${response.code}: ${AiResponseParser.error(json, body)}")
          }

          AiResponseParser.modelArray(json, body, "Gemini").mapNotNull { model ->
            val name = model.string("name") ?: return@mapNotNull null
            val id = name.removePrefix("models/")
            val methods = model["supportedGenerationMethods"] as? JsonArray
            if (methods != null) {
              val methodStrings = methods.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
              if (!methodStrings.contains("generateContent")) return@mapNotNull null
            } else if (!id.contains("gemini", ignoreCase = true)) {
              return@mapNotNull null
            }
            val displayName = model.string("displayName") ?: id
            AiModelInfo(
              id = id,
              displayName = displayName,
              isFree = id.contains("flash", ignoreCase = true) || id.contains("exp", ignoreCase = true),
            )
          }
        }
      }
    }

  override suspend fun verifyKey(apiKey: String): Result<String> =
    withContext(Dispatchers.IO) {
      fetchModels(apiKey).map { "API key verified successfully (${it.size} models available)" }
    }

  override suspend fun generateContent(
    apiKey: String,
    model: String,
    instruction: String,
    userInput: String,
    options: AiGenerationOptions,
  ): Result<AiGeneratedContent> =
    withContext(Dispatchers.IO) {
      runCatchingCancellable {
        val modelPath = if (model.startsWith("models/")) model else "models/$model"
        val url = "$BASE_URL/$modelPath:generateContent"
        val payload = googlePayload(instruction, userInput, options)
        val request =
          Request
            .Builder()
            .url(url)
            .header("x-goog-api-key", apiKey)
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        apiClient.newCall(request).awaitResponse().use { response ->
          val body = response.body.string()
          if (!response.isSuccessful) {
            throw IllegalStateException("Gemini generate error ${response.code}: ${AiResponseParser.error(json, body)}")
          }
          AiResponseParser.google(json, body, "Gemini")
        }
      }
    }

  private fun googlePayload(
    instruction: String,
    userInput: String,
    options: AiGenerationOptions,
  ) = buildJsonObject {
    if (instruction.isNotBlank()) {
      put(
        "systemInstruction",
        buildJsonObject {
          put("parts", buildJsonArray { add(buildJsonObject { put("text", instruction) }) })
        },
      )
    }
    put(
      "contents",
      buildJsonArray {
        add(
          buildJsonObject {
            put("role", "user")
            put("parts", buildJsonArray { add(buildJsonObject { put("text", userInput) }) })
          },
        )
      },
    )
    put(
      "generationConfig",
      buildJsonObject {
        put("maxOutputTokens", options.maxTokens)
        put("temperature", options.temperature)
      },
    )
  }

  private fun JsonObject.string(key: String): String? = (get(key) as? JsonPrimitive)?.contentOrNull
}

