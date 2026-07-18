package com.listingstudio.app.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Thin client over the Gemini image model ("Nano Banana" / gemini-2.5-flash-image).
 * Sends an input image plus a text instruction and returns the edited image.
 */
class GeminiClient {

    private val model = "gemini-2.5-flash-image"
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    sealed class Result {
        data class Success(val bitmap: Bitmap) : Result()
        data class Error(val message: String) : Result()
    }

    suspend fun edit(apiKey: String, source: Bitmap, prompt: String): Result =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext Result.Error("No Gemini API key set. Add one in Settings.")
            }
            try {
                val base64Image = source.toJpegBase64()

                val body = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray()
                            .put(JSONObject().put("text", prompt))
                            .put(JSONObject().put("inline_data", JSONObject()
                                .put("mime_type", "image/jpeg")
                                .put("data", base64Image)))
                        )
                    ))
                }.toString()

                val url = "https://generativelanguage.googleapis.com/v1beta/models/" +
                    "$model:generateContent?key=$apiKey"

                val request = Request.Builder()
                    .url(url)
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                http.newCall(request).execute().use { resp ->
                    val text = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        return@withContext Result.Error(parseError(text, resp.code))
                    }
                    val bitmap = parseImage(text)
                        ?: return@withContext Result.Error(
                            "The model did not return an image. Try again or rephrase."
                        )
                    Result.Success(bitmap)
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }

    private fun parseImage(json: String): Bitmap? {
        val parts = JSONObject(json)
            .optJSONArray("candidates")?.optJSONObject(0)
            ?.optJSONObject("content")?.optJSONArray("parts") ?: return null
        for (i in 0 until parts.length()) {
            val inline = parts.optJSONObject(i)?.optJSONObject("inline_data")
                ?: parts.optJSONObject(i)?.optJSONObject("inlineData")
            val data = inline?.optString("data")
            if (!data.isNullOrBlank()) {
                val bytes = Base64.decode(data, Base64.DEFAULT)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }
        return null
    }

    private fun parseError(json: String, code: Int): String = try {
        val msg = JSONObject(json).optJSONObject("error")?.optString("message")
        if (msg.isNullOrBlank()) "Request failed (HTTP $code)" else msg
    } catch (e: Exception) {
        "Request failed (HTTP $code)"
    }

    private fun Bitmap.toJpegBase64(): String {
        val out = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 92, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }
}
