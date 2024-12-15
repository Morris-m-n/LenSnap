package com.lensnap.app.ui.theme.screens.splashScreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lensnap.app.R

@Composable
fun SplashScreen() {
    val text = "LenSnap"
    val animatedProgress = remember { Animatable(0f) }

    // Launch an animation coroutine
    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    val bootstrapPrimaryColor = Color(0xFF007BFF) // Bootstrap Primary Blue color

    // Background image
    Image(
        painter = painterResource(R.drawable.splashbackground),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                text.forEachIndexed { index, char ->
                    val animatedColor = when (index) {
                        3 -> bootstrapPrimaryColor.copy(alpha = animatedProgress.value)
                        4, 5, 6 -> bootstrapPrimaryColor
                        else -> Color.White
                    }
                    Text(
                        text = char.toString(),
                        fontSize = 40.sp,
                        color = animatedColor,
                        modifier = Modifier.padding(2.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Custom Text
            Text(
                text = "Capture every moment",
                fontSize = 20.sp,
                color = bootstrapPrimaryColor,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = animatedProgress.value,
                modifier = Modifier
                    .width(200.dp)
                    .height(8.dp)
                    .graphicsLayer { shadowElevation = 4.dp.toPx() },
                color = bootstrapPrimaryColor,
                trackColor = bootstrapPrimaryColor.copy(alpha = 0.3f)
            )
        }
    }
}
