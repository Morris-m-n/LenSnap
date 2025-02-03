package com.lensnap.app.ui.theme.screens.createEventScreen

import Event
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lensnap.app.data.UserViewModel

val PrimaryBlueColor = Color(0xFF0D6EFD) // Primary blue color
val IconGrayTint = Color.Gray // Gray tint for icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventConfirmationScreen(
    onPrevious: () -> Unit,
    onConfirm: (Event, List<Bitmap>, List<Uri>) -> Unit,
    eventName: String,
    eventDescription: String,
    eventLocation: String,
    eventDate: String,
    eventTime: String,
    eventImageUri: Uri?,
    eventImageBitmap: Bitmap?,
    userViewModel: UserViewModel // Add userViewModel parameter
) {
    var isPrivate by remember { mutableStateOf(false) }
    val currentUser = userViewModel.currentUser.observeAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details", fontSize = 35.sp, fontWeight = FontWeight.Thin) }
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
                        eventImageBitmap?.let { bitmap ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Event Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(Color.Gray)
                                    .clip(RoundedCornerShape(8.dp)) // Rounded corners for the image
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Description: $eventDescription",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                        EventInfoRow(Icons.Default.Event, eventName)
                        EventInfoRow(Icons.Default.LocationOn, eventLocation)
                        EventInfoRow(Icons.Default.CalendarToday, eventDate)
                        EventInfoRow(Icons.Default.Schedule, eventTime)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Checkbox(
                                checked = isPrivate,
                                onCheckedChange = { isChecked -> isPrivate = isChecked }
                            )
                            Text(
                                text = if (isPrivate) "Private Event" else "Public Event",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onPrevious,
                        shape = RoundedCornerShape(8.dp), // Less border radius
                        border = BorderStroke(1.dp, PrimaryBlueColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlueColor)
                    ) {
                        Text("Previous")
                    }
                    Button(
                        onClick = {
                            val eventStatus = if (isPrivate) "private" else "public"
                            val event = Event(
                                name = eventName,
                                description = eventDescription,
                                location = eventLocation,
                                date = eventDate,
                                time = eventTime,
                                imageUrl = eventImageUri?.toString() ?: "",
                                creatorId = currentUser?.id ?: "", // Set creatorId to current user's ID
                                status = eventStatus
                            )
                            // Assuming you have lists of captured images and gallery images to pass
                            val capturedImages = listOf<Bitmap>() // Replace with actual list of captured images
                            val galleryImages = listOf<Uri>() // Replace with actual list of gallery images
                            onConfirm(event, capturedImages, galleryImages)
                        },
                        shape = RoundedCornerShape(8.dp), // Less border radius
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlueColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    )
}

@Composable
fun EventInfoRow(icon: ImageVector, info: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = IconGrayTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = info,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


//package com.lensnap.app.ui.theme.screens.createEventScreen
//
//import Event
//import android.graphics.Bitmap
//import android.net.Uri
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//val PrimaryBlueColor = Color(0xFF0D6EFD) // Primary blue color
//val IconGrayTint = Color.Gray // Gray tint for icons
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CreateEventConfirmationScreen(
//    onPrevious: () -> Unit,
//    onConfirm: (Event, List<Bitmap>, List<Uri>) -> Unit,
//    eventName: String,
//    eventDescription: String,
//    eventLocation: String,
//    eventDate: String,
//    eventTime: String,
//    eventImageUri: Uri?,
//    eventImageBitmap: Bitmap?
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Event Details", fontSize = 35.sp, fontWeight = FontWeight.Thin) }
//            )
//        },
//        content = { paddingValues ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    elevation = CardDefaults.cardElevation(8.dp)
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color.White)
//                            .padding(16.dp)
//                    ) {
//                        eventImageBitmap?.let { bitmap ->
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Image(
//                                bitmap = bitmap.asImageBitmap(),
//                                contentDescription = "Event Image",
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(200.dp)
//                                    .background(Color.Gray)
//                                    .clip(RoundedCornerShape(8.dp)) // Rounded corners for the image
//                            )
//                        }
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Text(
//                            text = "Description: $eventDescription",
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
//                        )
//                        EventInfoRow(Icons.Default.Event, eventName)
//                        EventInfoRow(Icons.Default.LocationOn, eventLocation)
//                        EventInfoRow(Icons.Default.CalendarToday, eventDate)
////                        EventInfoRow(Icons.Default.Schedule, eventTime)
//                    }
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//                Row(
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    OutlinedButton(
//                        onClick = onPrevious,
//                        shape = RoundedCornerShape(8.dp), // Less border radius
//                        border = BorderStroke(1.dp, PrimaryBlueColor),
//                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlueColor)
//                    ) {
//                        Text("Previous")
//                    }
//                    Button(
//                        onClick = {
//                            val event = Event(
//                                name = eventName,
//                                description = eventDescription,
//                                location = eventLocation,
//                                date = eventDate,
//                                time = eventTime,
//                                imageUrl = eventImageUri?.toString() ?: ""
//                            )
//                            // Assuming you have lists of captured images and gallery images to pass
//                            val capturedImages = listOf<Bitmap>() // Replace with actual list of captured images
//                            val galleryImages = listOf<Uri>() // Replace with actual list of gallery images
//                            onConfirm(event, capturedImages, galleryImages)
//                        },
//                        shape = RoundedCornerShape(8.dp), // Less border radius
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = PrimaryBlueColor,
//                            contentColor = Color.White
//                        )
//                    ) {
//                        Text("Confirm")
//                    }
//                }
//            }
//        }
//    )
//}
//
//@Composable
//fun EventInfoRow(icon: ImageVector, info: String) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 8.dp)
//    ) {
//        Icon(
//            imageVector = icon,
//            contentDescription = null,
//            tint = IconGrayTint,
//            modifier = Modifier.size(24.dp)
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(
//            text = info,
//            style = MaterialTheme.typography.bodySmall
//        )
//    }
//}
