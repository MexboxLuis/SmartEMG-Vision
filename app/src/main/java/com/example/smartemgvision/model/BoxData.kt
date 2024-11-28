package com.example.smartemgvision.model

data class BoxData(
    val xMin: Float,
    val yMin: Float,
    val xMax: Float,
    val yMax: Float,
    val label: String,
    val confidence: Float
)
