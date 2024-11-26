package com.example.smartemgvision.ui.screens

import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


@Composable
fun SimulationScreen(
    onBack: () -> Unit,
//    sendDataToServer: (String, String) -> Unit
) {

    val context = LocalContext.current
    var isCameraPermissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val permission = android.Manifest.permission.CAMERA
        isCameraPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (!isCameraPermissionGranted) {
            ActivityCompat.requestPermissions(
                (context as Activity),
                arrayOf(permission),
                0
            )
        }
    }

    if (!isCameraPermissionGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Camera permission is required to use this feature.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray)
        ) {
            Text(
                "Camera Feed Here",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("EMG Signals", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Detected Movement: Forward", style = MaterialTheme.typography.bodyLarge)
                }

                Column {
                    Text("YOLO Detection", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Detected Object: Chair", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }

//    LaunchedEffect(Unit) {
//        while (true) {
//            val emgData = "Forward"
//            val yoloData = "Chair"
//            sendDataToServer(emgData, yoloData)
//            delay(1000)
//        }
//    }
}

