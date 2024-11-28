package com.example.smartemgvision.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


fun processImageProxy(imageProxy: ImageProxy, onResponse: (String) -> Unit) {
    val buffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val bitmap = Bitmap.createBitmap(
        imageProxy.width,
        imageProxy.height,
        Bitmap.Config.ARGB_8888
    ).apply {
        copyPixelsFromBuffer(ByteBuffer.wrap(bytes))
    }

    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees)

    val outputStream = ByteArrayOutputStream()
    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    val jpegBytes = outputStream.toByteArray()

    sendFrameToServer(jpegBytes, onResponse)

    imageProxy.close()
}

fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}


