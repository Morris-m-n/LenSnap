package com.lensnap.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import android.net.Uri
import android.widget.VideoView
import android.widget.MediaController
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import com.lensnap.app.models.DailyUpdate

@Composable
fun DailyUpdateScreen(
    userId: String,
    uri: Uri,
    username: String,
    profilePhotoUrl: String,
    onUpload: (String, DailyUpdate, Uri) -> Unit,
    onCancel: () -> Unit
) {
    var caption by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uri.toString().contains("video")) {
            VideoPlayer(uri = uri)
        } else {
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = "Selected Media",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f),
                contentScale = ContentScale.Crop
            )
        }

        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = { Text("Add a caption") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val aspectRatio = 1.0f // This can be calculated based on the media
                    val dailyUpdate = DailyUpdate(
                        uri = uri.toString(),
                        timestamp = System.currentTimeMillis(),
                        aspectRatio = aspectRatio,
                        caption = caption,
                        userId = userId, // Include the userId here
                        username = username, // Include the username here
                        profilePhotoUrl = profilePhotoUrl // Include the profile photo URL here
                    )
                    onUpload(userId, dailyUpdate, uri)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Submit")
            }
        }
    }
}

@Composable
fun VideoPlayer(uri: Uri) {
    AndroidView(
        factory = { context ->
            val videoView = VideoView(context)
            videoView.apply {
                setVideoURI(uri)
                setMediaController(MediaController(context))
                requestFocus()
                start()
            }
        },
        update = { view ->
            view.setVideoURI(uri)
            view.start()
        }
    )
}
