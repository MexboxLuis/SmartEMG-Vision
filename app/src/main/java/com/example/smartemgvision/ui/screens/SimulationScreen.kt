package com.example.smartemgvision.ui.screens

import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.smartemgvision.R
import com.example.smartemgvision.model.BoxData
import com.example.smartemgvision.ui.components.NoPermissionGranted
import com.example.smartemgvision.ui.components.YoloDetections
import com.example.smartemgvision.utils.processImageProxy
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


val suggestionsBank = mapOf(
    "person" to listOf(
        "Say hello to that person.",
        "Wave goodbye to that person.",
        "Approach that person.",
        "Point to that person."
    ),
    "bicycle" to listOf(
        "Bring me that bicycle.",
        "Take me to the bicycle.",
        "Check the bicycle's condition.",
        "Ask for a ride on the bicycle."
    ),
    "car" to listOf(
        "Take me to the car.",
        "Inspect the car for damage.",
        "Point to the car.",
        "Wave to the car owner."
    ),
    "dog" to listOf(
        "Pet the dog.",
        "Bring the dog closer.",
        "Point to the dog.",
        "Wave to the dog."
    ),
    "chair" to listOf(
        "Bring me the chair.",
        "Move the chair closer.",
        "Point to the chair.",
        "Sit on the chair."
    ),
    "bottle" to listOf(
        "Bring me the bottle.",
        "Open the bottle.",
        "Point to the bottle.",
        "Pass the bottle to someone."
    )
)

@Composable
fun SimulationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    var isCameraPermissionGranted by remember { mutableStateOf(false) }
    var showPermissionDeniedMessage by remember { mutableStateOf(false) }
    var serverResponse by remember { mutableStateOf("Waiting for response...") }
    var lastProcessedTime by remember { mutableLongStateOf(0L) }
    var isCameraInitialized by remember { mutableStateOf(false) }
    var selectedLabel by remember { mutableStateOf<String?>(null) }
    val detections = remember { mutableStateOf(emptyList<BoxData>()) }
    var actionMessage by remember { mutableStateOf("Waiting for action...") }

    LaunchedEffect(serverResponse) {
        try {
            val json = JSONObject(serverResponse)
            val detectionsArray = json.getJSONArray("detections")
            val parsedDetections = mutableListOf<BoxData>()

            for (i in 0 until detectionsArray.length()) {
                val detection = detectionsArray.getJSONObject(i)
                val box = detection.getJSONArray("box")
                val label = detection.getString("label")
                val confidence = detection.getDouble("confidence")

                parsedDetections.add(
                    BoxData(
                        xMin = box.getDouble(0).toFloat(),
                        yMin = box.getDouble(1).toFloat(),
                        xMax = box.getDouble(2).toFloat(),
                        yMax = box.getDouble(3).toFloat(),
                        label = label,
                        confidence = confidence.toFloat()
                    )
                )
            }
            detections.value = parsedDetections
            selectedLabel = detections.value.firstOrNull()?.label
        } catch (e: Exception) {
            detections.value = emptyList()
            selectedLabel = null
        }
    }

    LaunchedEffect(Unit) {
        val permissions = arrayOf(android.Manifest.permission.CAMERA)
        isCameraPermissionGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!isCameraPermissionGranted) {
            ActivityCompat.requestPermissions(
                (context as Activity),
                permissions,
                0
            )
        }

        showPermissionDeniedMessage = !permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    if (showPermissionDeniedMessage) {
        NoPermissionGranted {
            onBack()
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = {
                    previewView.apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { view ->
                if (!isCameraInitialized) {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .apply { surfaceProvider = view.surfaceProvider }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastProcessedTime >= 1000) {
                                lastProcessedTime = currentTime
                                processImageProxy(imageProxy) { response ->
                                    serverResponse = response
                                }
                            } else {
                                imageProxy.close()
                            }
                        }

                        try {
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                            isCameraInitialized = true
                        } catch (exc: Exception) {
                            Toast.makeText(
                                context,
                                "Error initializing the camera.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            }

            YoloDetections(
                detections = detections.value,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                selectedLabel?.let { label ->
                    val suggestions = suggestionsBank[label] ?: listOf("No suggestions available.")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                actionMessage = suggestions.randomOrNull() ?: "No action defined."
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .size(80.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),


                        ) {
                            Image(
                                painter = painterResource(R.drawable.img_tip),
                                contentDescription = "Tip Image",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Button(
                            onClick = {
                                actionMessage = suggestions.randomOrNull() ?: "No action defined."
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .size(80.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),

                        ) {
                            Image(
                                painter = painterResource(R.drawable.img_spherical),
                                contentDescription = "Spherical Image",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Button(
                            onClick = {
                                actionMessage = suggestions.randomOrNull() ?: "No action defined."
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .size(80.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.img_lateral),
                                contentDescription = "Lateral Image",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }


                }
            }

            Text(
                text = actionMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )


            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}







