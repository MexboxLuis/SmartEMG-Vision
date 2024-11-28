package com.example.smartemgvision.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.smartemgvision.model.BoxData

@Composable
fun YoloDetections(
    detections: List<BoxData>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        detections.forEachIndexed { index, boxData ->
            Text(
                text = "${index + 1}. ${boxData.label} (${(boxData.confidence * 100).toInt()}%)",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val colors = listOf(
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.Yellow,
            Color.Magenta,
            Color.Cyan,
            Color(0xFF6200EA),
            Color(0xFFFF5722),
            Color(0xFF3F51B5)
        )

        detections.forEachIndexed { index, boxData ->
            val color = colors[index % colors.size]
            drawRect(
                color = color,
                topLeft = Offset(
                    x = boxData.xMin * canvasWidth,
                    y = boxData.yMin * canvasHeight
                ),
                size = Size(
                    width = (boxData.xMax - boxData.xMin) * canvasWidth,
                    height = (boxData.yMax - boxData.yMin) * canvasHeight
                ),
                style = Stroke(width = 4.dp.toPx())
            )
        }
    }
}