package com.lensnap.app.ui.screens.eventDetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(event: Event, navController: NavController) {
    var showFullScreenImage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = event.name) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = 5.dp, end = 5.dp, top = 16.dp, bottom = 16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Image(
                        painter = rememberImagePainter(data = event.imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)) // Adding rounded corners
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(8.dp)
                            .clip(RoundedCornerShape(16.dp)) // Adding rounded corners
                    ) {
                        Text(text = event.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        Text(text = "Date: ${event.date}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Text(text = "Time: ${event.time}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Text(text = "Location: ${event.location}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Text(text = event.description, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Captured Images",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(event.images.chunked(3)) { rowImages ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    rowImages.forEach { imageUrl ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.2f) // Increase height slightly by adjusting aspect ratio
                                .clip(RoundedCornerShape(16.dp)) // Adding rounded corners
                        ) {
                            ImageCard(imageUrl = imageUrl, onClick = { showFullScreenImage = imageUrl })
                        }
                    }
                }
            }
        }
    }

    showFullScreenImage?.let { imageUrl ->
        FullScreenImageDialog(imageUrl = imageUrl) {
            showFullScreenImage = null
        }
    }
}

@Composable
fun ImageCard(imageUrl: String, onClick: () -> Unit) {
    Image(
        painter = rememberImagePainter(data = imageUrl),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp)) // Adding rounded corners
    )
}

@Composable
fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent) // Making the background transparent
        ) {
            Image(
                painter = rememberImagePainter(data = imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }
            )
        }
    }
}
