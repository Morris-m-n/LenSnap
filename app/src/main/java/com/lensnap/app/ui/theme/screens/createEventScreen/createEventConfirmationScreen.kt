package com.lensnap.app.ui.theme.screens.createEventScreen

import Event
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventConfirmationScreen(
    onPrevious: () -> Unit,
    onConfirm: (Event, List<Bitmap>, List<Uri>) -> Unit, // Add galleryImages parameter
    eventName: String,
    eventDescription: String,
    eventLocation: String,
    eventDate: String,
    eventTime: String,
    eventImageUri: Uri?,
    eventImageBitmap: Bitmap?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Event Details") }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = eventName,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Description: $eventDescription",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Location: $eventLocation",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Date: $eventDate",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Time: $eventTime",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        eventImageBitmap?.let { bitmap ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Event Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(Color.Gray)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onPrevious) {
                        Text("Previous")
                    }
                    Button(onClick = {
                        val event = Event(
                            name = eventName,
                            description = eventDescription,
                            location = eventLocation,
                            date = eventDate,
                            time = eventTime,
                            imageUrl = eventImageUri?.toString() ?: ""
                        )
                        // Assuming you have lists of captured images and gallery images to pass
                        val capturedImages = listOf<Bitmap>() // Replace with actual list of captured images
                        val galleryImages = listOf<Uri>() // Replace with actual list of gallery images
                        onConfirm(event, capturedImages, galleryImages)
                    }) {
                        Text("Confirm")
                    }
                }
            }
        }
    )
}
