package com.example.smartemgvision.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

fun sendFrameToServer(frame: ByteArray) {
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
                Log.d("YOLODetection", "Server response: $body")
            } else {
                Log.e("YOLODetection", "Error: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

