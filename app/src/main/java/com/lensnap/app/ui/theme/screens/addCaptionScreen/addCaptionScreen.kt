package com.lensnap.app.ui.theme.screens.addCaptionScreen

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.navigation.NavController
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import android.webkit.MimeTypeMap
import androidx.compose.ui.platform.LocalContext
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.MediaItem
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaptionScreen(
    mediaUri: Uri,
    onSubmit: (String) -> Unit,
    navController: NavController,
    isLoading: Boolean // Add a parameter for loading state
) {
    var caption by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) } // State to show the dialog
    val context = LocalContext.current

    // Handle back press
    BackHandler {
        showDialog = true // Show the dialog when user tries to back out
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Determine the type of media and display appropriately
        if (isImageFile(context, mediaUri)) {
            // Display image
            Image(
                painter = rememberImagePainter(mediaUri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp)), // No rounded corners and no padding
                contentScale = ContentScale.Crop
            )
        } else {
            // Display video using ExoPlayer
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = SimpleExoPlayer.Builder(context).build().apply {
                            setMediaItem(MediaItem.fromUri(mediaUri))
                            prepare()
                            playWhenReady = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp))
            )
        }

        // Show loading indicator
        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            // Caption and Submit Section on the same row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Caption Input
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("Add a caption...") },
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    modifier = Modifier
                        .weight(1f) // Takes available space
                        .clip(RoundedCornerShape(24.dp)) // Rounded corners
                        .background(Color(0xFFF0F0F0)), // Gray background
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.Transparent, // Remove border
                        focusedBorderColor = Color.Transparent,
                        containerColor = Color(0xFFF0F0F0), // Background color
                        cursorColor = Color(0xFF0D6EFD), // Cursor color
                    )
                )

                // Submit Button with only the icon
                Button(
                    onClick = { onSubmit(caption) },
                    enabled = caption.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D6EFD), // Button color
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(56.dp), // Fixed button size
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Submit",
                        tint = Color.White,
                    )
                }
            }

            // Show a dialog if the user tries to navigate away
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Discard Changes?") },
                    text = { Text("Are you sure you want to discard the media and caption?") },
                    shape = RoundedCornerShape(8.dp), // Adjusted border radius
                    confirmButton = {
                        TextButton(onClick = {
                            navController.popBackStack() // Navigate back if user confirms
                        }) {
                            Text("Discard", color = Color.Red) // Red text for discard
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel", color = Color(0xFF0D6EFD)) // Blue text for cancel
                        }
                    },
                    modifier = Modifier.padding(16.dp) // Add padding for a more compact dialog
                )
            }
        }
    }
}

private fun isImageFile(context: Context, uri: Uri): Boolean {
    val contentResolver = context.contentResolver
    val type = contentResolver.getType(uri)
    return type?.startsWith("image") == true
}
