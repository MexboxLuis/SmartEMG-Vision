package com.example.smartemgvision.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

fun sendFrameToServer(frame: ByteArray, onResponse: (String) -> Unit) {
    val url = "http://10.0.2.2:5000/detect-objects"
    val client = OkHttpClient()

    val requestBody = frame.toRequestBody("image/jpeg".toMediaTypeOrNull())

    val multipartBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", "frame.jpg", requestBody)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = Request.Builder()
                .url(url)
                .post(multipartBody)
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                withContext(Dispatchers.Main) {
                    onResponse(body ?: "No response body")
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResponse("Error: ${response.message}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResponse("Exception: ${e.message}")
            }
        }
    }
}

fun sendKeywordToServer(keyword: String, onResponse: (String) -> Unit) {
    val url = "http://10.0.2.2:5001/predict"
    val client = OkHttpClient()


    val json = JSONObject().apply {
        put("keyword", keyword)
    }

    val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                withContext(Dispatchers.Main) {
                    val predictedClass = try {
                        val jsonResponse = JSONObject(body ?: "{}")
                        jsonResponse.getInt("predicted_class").toString()
                    } catch (e: Exception) {
                        "Error parsing response"
                    }
                    onResponse(predictedClass)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResponse("Error: ${response.message}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResponse("Exception: ${e.message}")
            }
        }
    }
}