package com.example.smartemgvision.ui.screens

import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.smartemgvision.R
import com.example.smartemgvision.model.BoxData
import com.example.smartemgvision.ui.components.NoPermissionGranted
import com.example.smartemgvision.ui.components.YoloDetections
import com.example.smartemgvision.utils.processImageProxy
import com.example.smartemgvision.utils.sendKeywordToServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


val suggestionsBank = mapOf(
    "utensils" to listOf(
        "Use the :item.",
        "Pass me the :item.",
        "Put the :item away."
    ),
    "food" to listOf(
        "Bring me the :item.",
        "Pass me the :item.",
        "Check the :item."
    ),
    "person" to listOf(
        "Say hello to :item.",
        "Wave goodbye to :item.",
        "Call :item.",
    ),
    "furniture" to listOf(
        "Move the :item.",
        "Use the :item.",
        "Point to the :item."
    )
)

val itemGroups = mapOf(
    "spoon" to "utensils",
    "knife" to "utensils",
    "fork" to "utensils",
    "bottle" to "utensils",
    "cup" to "utensils",
    "bowl" to "utensils",
    "apple" to "food",
    "banana" to "food",
    "sandwich" to "food",
    "broccoli" to "food",
    "orange" to "food",
    "carrot" to "food",
    "hot dog" to "food",
    "pizza" to "food",
    "donut" to "food",
    "cake" to "food",
    "person" to "person",
    "bench" to "furniture",
    "chair" to "furniture",
    "couch" to "furniture",
    "bed" to "furniture",
    "dining table" to "furniture"
)

@Composable
fun SimulationScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
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
    var actionMessage by remember { mutableStateOf("") }
    var animateText by remember { mutableStateOf(false) }
    var activeButton by remember { mutableStateOf<String?>(null) }

    var predictedClass by remember { mutableStateOf("") }
    var predictedClassText by remember { mutableStateOf("") }

    predictedClassText = when (predictedClass.toIntOrNull()) {
        0 -> "Tip"
        1 -> "Spherical"
        2 -> "Lateral"
        else -> ""
    }


    LaunchedEffect(key1 = actionMessage) {
        if (actionMessage.isNotEmpty()) {
            animateText = true
            delay(5000)
            activeButton = null
            animateText = false
            actionMessage = ""
            predictedClass = ""
            predictedClassText = ""
        }
    }


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

            if (activeButton != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .background(
                            if (predictedClassText == activeButton) Color.Green else Color.Red,
                            shape = CircleShape
                        )
                ) {

                    Text(
                        text = if (predictedClassText == activeButton) "The prediction $predictedClassText is correct" else "The prediction $activeButton is incorrect \n ($predictedClassText)",
                        color = Color.Black,
                        modifier = Modifier
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }



            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val animatedBackgroundColor by animateColorAsState(
                        targetValue = if (animateText) MaterialTheme.colorScheme.background else Color.Transparent,
                        animationSpec = tween(durationMillis = 500), label = "Action"
                    )

                    Text(
                        text = actionMessage,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .background(animatedBackgroundColor, shape = CircleShape)
                            .padding(32.dp)
                    )
                    Spacer(modifier = Modifier.height(64.dp))

                    selectedLabel?.let { label ->
                        val group = itemGroups[label]
                            ?: "unknown" // Get the group ("utensils", "food", etc.)
                        val suggestions =
                            suggestionsBank[group] ?: listOf("No suggestions available.")


                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val tipSize by animateDpAsState(
                                targetValue = if (activeButton == "Tip") 100.dp else 80.dp,
                                animationSpec = tween(durationMillis = 300), label = "TipSize"
                            )

                            Button(
                                onClick = {
                                    if (!animateText) {
                                        scope.launch {
                                            val suggestion = suggestions[0]
                                            actionMessage = suggestion.replace(":item", label)
                                            activeButton = "Tip"
                                            sendKeywordToServer(activeButton!!.lowercase()) { response ->
                                                predictedClass = response
                                            }
                                            delay(1000)
                                        }
                                    }
                                },
                                enabled = !animateText || activeButton == "Tip",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(tipSize)
                                    .then(
                                        if (activeButton == "Tip" || !animateText) Modifier else Modifier.alpha(
                                            0f
                                        )
                                    ),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.img_tip),
                                    contentDescription = "Tip Image",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            val sphericalSize by animateDpAsState(
                                targetValue = if (activeButton == "Spherical") 100.dp else 80.dp,
                                animationSpec = tween(durationMillis = 300), label = "SphericalSize"
                            )

                            Button(
                                onClick = {
                                    if (!animateText) {
                                        scope.launch {
                                            val suggestion = suggestions[1]
                                            actionMessage = suggestion.replace(
                                                ":item",
                                                label
                                            ) // Replace :item with the detected label
                                            activeButton = "Spherical"
                                            sendKeywordToServer(activeButton!!.lowercase()) { response ->
                                                predictedClass = response
                                            }
                                            delay(1000)
                                        }
                                    }
                                },
                                enabled = !animateText || activeButton == "Spherical",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(sphericalSize)
                                    .then(
                                        if (activeButton == "Spherical" || !animateText) Modifier else Modifier.alpha(
                                            0f
                                        )
                                    ),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.img_spherical),
                                    contentDescription = "Spherical Image",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            val lateralSize by animateDpAsState(
                                targetValue = if (activeButton == "Lateral") 100.dp else 80.dp,
                                animationSpec = tween(durationMillis = 300), label = "LateralSize"
                            )

                            Button(
                                onClick = {
                                    if (!animateText) {
                                        scope.launch {
                                            val suggestion = suggestions[2]
                                            actionMessage = suggestion.replace(":item", label)
                                            activeButton = "Lateral"
                                            sendKeywordToServer(activeButton!!.lowercase()) { response ->
                                                predictedClass = response
                                            }
                                            delay(1000)
                                        }
                                    }
                                },
                                enabled = !animateText || activeButton == "Lateral",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(lateralSize)
                                    .then(
                                        if (activeButton == "Lateral" || !animateText) Modifier else Modifier.alpha(
                                            0f
                                        )
                                    ),
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
            }


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







