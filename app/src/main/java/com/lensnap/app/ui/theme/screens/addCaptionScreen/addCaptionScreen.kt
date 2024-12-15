package com.lensnap.app.ui.theme.screens.addCaptionScreen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaptionScreen(
    mediaUri: Uri,
    onSubmit: (String) -> Unit
) {
    var caption by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Display the post in the background
        Image(
            painter = rememberImagePainter(mediaUri),
            contentDescription = "Selected Media",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Text field for caption at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                placeholder = { Text("Add a caption...") },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .clip(RoundedCornerShape(8.dp))
                    .graphicsLayer { shadowElevation = 8.dp.toPx() }, // Add glow effect
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White,
                    cursorColor = Color.White
                )
            )
        }

        // Submit button at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Button(
                onClick = { onSubmit(caption) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D6EFD),
                    contentColor = Color.White
                )
            ) {
                Text("Submit")
            }
        }
    }
}
