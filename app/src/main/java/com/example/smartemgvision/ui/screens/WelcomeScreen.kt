package com.example.smartemgvision.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartemgvision.R
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(onNavigateToInteraction: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "iconAnimation")

    val animatedAlpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animatedAlpha"
    )

    val animatedScale = infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animatedScale"
    )

    val typingText = "Empowering Mobility with EMG Signals and Real-Time Vision"
    var currentText by remember { mutableStateOf("") }
    var typingDirection by remember { mutableIntStateOf(1) }

    LaunchedEffect(typingDirection) {
        while (true) {
            if (typingDirection == 1) {
                if (currentText.length < typingText.length) {
                    currentText = typingText.substring(0, currentText.length + 1)
                } else {
                    delay(1000)
                    typingDirection = -1
                }
            } else {
                if (currentText.isNotEmpty()) {
                    currentText = currentText.substring(0, currentText.length - 1)
                } else {
                    delay(500)
                    typingDirection = 1
                }
            }
            delay(100)
        }
    }


    val buttonScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            buttonScale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(500, easing = LinearEasing)
            )
            buttonScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(500, easing = LinearEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Smart EMG Vision",
                modifier = Modifier
                    .size(150.dp)
                    .scale(animatedScale.value)
                    .alpha(animatedAlpha.value),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Smart Interaction System",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$currentText |",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 48.dp),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onNavigateToInteraction,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(0.7f)
                    .scale(buttonScale.value),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Start Simulation",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
