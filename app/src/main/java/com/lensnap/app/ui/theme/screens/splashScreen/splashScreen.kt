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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun SplashScreen() {
    val primaryColor = Color(0xFF0D6EFD) // Bootstrap Primary Blue color

    // Box to hold the splash screen content
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.size(80.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.1f, size.height * 0.5f) // Start point (tail)
                    lineTo(size.width * 0.85f, size.height * 0.5f) // End point (tail)
                    moveTo(size.width * 0.55f, size.height * 0.3f) // Start of the arrow head
                    lineTo(size.width * 0.85f, size.height * 0.5f) // Tip of the arrow head
                    lineTo(size.width * 0.55f, size.height * 0.7f) // End of the arrow head
                }
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        // Creator text at the bottom of the screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "by",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Vector",
                    fontSize = 20.sp,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
