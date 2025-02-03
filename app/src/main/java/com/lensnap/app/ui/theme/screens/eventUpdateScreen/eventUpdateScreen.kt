package com.lensnap.app.ui.theme.screens.eventUpdateScreen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

//@Composable
//fun EventUpdateScreen(
//    eventId: String,
//    eventViewModel: EventViewModel,
//    navController: NavController
//) {
//    var event by remember { mutableStateOf<Event?>(null) }
//    var isLoading by remember { mutableStateOf(true) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
//
//    // Fetch the event when the screen is first launched
//    LaunchedEffect(eventId) {
//        try {
//            eventViewModel.getEventById(
//                eventId = eventId,
//                onSuccess = { fetchedEvent ->
//                    event = fetchedEvent
//                    isLoading = false
//                },
//                onError = { error ->
//                    errorMessage = error
//                    isLoading = false
//                }
//            )
//        } catch (e: Exception) {
//            Log.e("EventUpdateScreen", "Error fetching event data", e)
//            errorMessage = e.message
//            isLoading = false
//        }
//    }
//
//    // Show a loading spinner while the event is being fetched
//    if (isLoading) {
//        CircularProgressIndicator(modifier = Modifier.fillMaxSize(), color = Color.Blue)
//    } else {
//        event?.let {
//            EventUpdateContent(
//                event = it,
//                selectedImageBitmap = selectedImageBitmap,
//                onImageSelected = { newImageBitmap ->
//                    selectedImageBitmap = newImageBitmap
//                },
//                onUpdate = { updatedEvent ->
//                    eventViewModel.updateEvent(updatedEvent, selectedImageBitmap, onSuccess = {
//                        navController.popBackStack() // Navigate back after updating
//                    }, onError = { error ->
//                        // Handle error (show error message, etc.)
//                        errorMessage = error
//                    })
//                },
//                onCancel = {
//                    navController.popBackStack() // Navigate back without changes
//                }
//            )
//        } ?: run {
//            // If event is null (maybe failed to fetch), display error message
//            errorMessage?.let {
//                Text(text = "Error: $it", color = Color.Red)
//            }
//        }
//    }
//}
//
//
//@Composable
//fun EventUpdateContent(
//    event: Event,
//    onUpdate: (Event) -> Unit,
//    onCancel: () -> Unit,
//    selectedImageBitmap: Bitmap?,
//    onImageSelected: (Bitmap) -> Unit
//) {
//    // State for the updated event details
//    var updatedEvent by remember { mutableStateOf(event) }
//    var selectedDate by remember { mutableStateOf(event.date) }
//    var selectedTime by remember { mutableStateOf(event.time) }
//
//    // Color theme
//    val primaryBlue = Color(0xFF0D6EFD)
//    val backgroundColor = Color(0xFFF8F9FA)
//
//    // Time picker state
//    val timePickerDialogState = remember { mutableStateOf(false) }
//
//    // Date picker state
//    val datePickerDialogState = remember { mutableStateOf(false) }
//
//    val context = LocalContext.current
//    val calendar = Calendar.getInstance()
//    val currentYear = calendar.get(Calendar.YEAR)
//    val currentMonth = calendar.get(Calendar.MONTH)
//    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
//    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
//    val currentMinute = calendar.get(Calendar.MINUTE)
//
//    // Main UI
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(backgroundColor)
//            .padding(16.dp)
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//            item {
//                // Title
//                Text(
//                    text = "Update Event",
//                    style = MaterialTheme.typography.h4.copy(
//                        color = primaryBlue,
//                        fontWeight = FontWeight.Bold
//                    ),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 24.dp),
//                    textAlign = TextAlign.Center
//                )
//
//                // Display Event Image (if available)
//                if (event.imageUrl.isNotEmpty()) {
//                    val imageUri = Uri.parse(event.imageUrl)
//                    val bitmap = imageUri?.toBitmap(context.contentResolver)
//                    bitmap?.let {
//                        Image(
//                            painter = rememberImagePainter(
//                                ImageRequest.Builder(LocalContext.current)
//                                    .data(it)  // The URL of the image
//                                    .crossfade(true)
//                                    .build()
//                            ),
//                            contentDescription = "Event Image",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(Color.LightGray)
//                                .shadow(8.dp, RoundedCornerShape(12.dp))
//                                .padding(8.dp),
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//                } else {
//                    // Show placeholder or a message if no image is set
//                    Text(
//                        text = "No Event Image",
//                        color = Color.Gray,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Name
//                OutlinedTextField(
//                    value = updatedEvent.name,
//                    onValueChange = { updatedEvent = updatedEvent.copy(name = it) },
//                    label = { Text("Event Name") },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = primaryBlue,
//                        cursorColor = primaryBlue
//                    )
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Description
//                OutlinedTextField(
//                    value = updatedEvent.description,
//                    onValueChange = { updatedEvent = updatedEvent.copy(description = it) },
//                    label = { Text("Event Description") },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = primaryBlue,
//                        cursorColor = primaryBlue
//                    ),
//                    maxLines = 3
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Date
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { datePickerDialogState.value = true }
//                        .padding(8.dp)
//                        .background(
//                            Color.White,
//                            shape = RoundedCornerShape(8.dp)
//                        )
//                        .padding(horizontal = 16.dp, vertical = 12.dp)
//                ) {
//                    Text(
//                        text = "Event Date: $selectedDate",
//                        color = Color.Gray,
//                        modifier = Modifier.weight(1f)
//                    )
//                    Icon(
//                        imageVector = Icons.Default.CalendarToday,
//                        contentDescription = "Pick Date",
//                        tint = primaryBlue
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Time
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { timePickerDialogState.value = true }
//                        .padding(8.dp)
//                        .background(
//                            Color.White,
//                            shape = RoundedCornerShape(8.dp)
//                        )
//                        .padding(horizontal = 16.dp, vertical = 12.dp)
//                ) {
//                    Text(
//                        text = "Event Time: $selectedTime",
//                        color = Color.Gray,
//                        modifier = Modifier.weight(1f)
//                    )
//                    Icon(
//                        imageVector = Icons.Default.AccessTime,
//                        contentDescription = "Pick Time",
//                        tint = primaryBlue
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Location
//                OutlinedTextField(
//                    value = updatedEvent.location,
//                    onValueChange = { updatedEvent = updatedEvent.copy(location = it) },
//                    label = { Text("Event Location") },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = primaryBlue,
//                        cursorColor = primaryBlue
//                    )
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Status (Checkbox with Help Text)
//                Column {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                    ) {
//                        Checkbox(
//                            checked = updatedEvent.status == "Public",
//                            onCheckedChange = { isPublic ->
//                                updatedEvent = updatedEvent.copy(status = if (isPublic) "Public" else "Private")
//                            },
//                            colors = CheckboxDefaults.colors(
//                                checkedColor = primaryBlue
//                            )
//                        )
//                        Text(
//                            text = if (updatedEvent.status == "Public") "Public" else "Private",
//                            style = MaterialTheme.typography.body1,
//                            color = Color.Black
//                        )
//                    }
//
//                    // Help Text
//                    Text(
//                        text = if (updatedEvent.status == "Public") {
//                            "This event will be visible on the public feed."
//                        } else {
//                            "This event will not be suggested on the public feed."
//                        },
//                        style = MaterialTheme.typography.body2,
//                        color = Color.Gray,
//                        modifier = Modifier.padding(start = 48.dp, top = 4.dp) // Indent to align with checkbox
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Image Picker
//                ImagePicker(
//                    selectedImageBitmap = selectedImageBitmap,
//                    onImageSelected = { bitmap ->
//                        onImageSelected(bitmap)
//                        updatedEvent = updatedEvent.copy(imageUrl = bitmap.toString()) // Optionally store image URL
//                    }
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Update Button
//                Button(
//                    onClick = { onUpdate(updatedEvent) },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.buttonColors(backgroundColor = primaryBlue)
//                ) {
//                    Text("Update Event", color = Color.White)
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Cancel Button
//                OutlinedButton(
//                    onClick = onCancel,
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryBlue)
//                ) {
//                    Text("Cancel")
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//            }
//        }
//    }
//}
//
//@Composable
//fun ImagePicker(
//    selectedImageBitmap: Bitmap?,
//    onImageSelected: (Bitmap) -> Unit
//) {
//    // Get the context properly within the composable scope
//    val context = LocalContext.current
//
//    // Image picker launcher
//    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        if (uri != null) {
//            try {
//                Log.d("ImagePicker", "Image URI: $uri")
//                val bitmap = uri.toBitmap(context.contentResolver)
//                bitmap?.let { onImageSelected(it) }
//            } catch (e: Exception) {
//                // Handle any error during conversion
//                Log.e("ImagePicker", "Error converting URI to Bitmap", e)
//            }
//        } else {
//            Log.e("ImagePicker", "Received null URI")
//        }
//    }
//
//    // Display selected image if available
//    if (selectedImageBitmap != null) {
//        Image(
//            bitmap = selectedImageBitmap.asImageBitmap(),
//            contentDescription = "Event Image",
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp)
//                .clip(RoundedCornerShape(12.dp))
//                .background(Color.LightGray)
//                .padding(8.dp),
//            contentScale = ContentScale.Crop
//        )
//    }
//
//    // Button to trigger image picker
//    Button(
//        onClick = { imagePickerLauncher.launch("image/*") },
//        modifier = Modifier.fillMaxWidth(),
//        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF0D6EFD))
//    ) {
//        Text("Pick Event Image", color = Color.White)
//    }
//}
//
//fun Uri.toBitmap(contentResolver: ContentResolver): Bitmap? {
//    return try {
//        MediaStore.Images.Media.getBitmap(contentResolver, this)
//    } catch (e: Exception) {
//        e.printStackTrace() // Log any error for debugging
//        null
//    }
//}


@Composable
fun EventUpdateScreen(
    eventId: String,
    eventViewModel: EventViewModel,
    navController: NavController
) {
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Fetch the event when the screen is first launched
    LaunchedEffect(eventId) {
        try {
            eventViewModel.getEventById(
                eventId = eventId,
                onSuccess = { fetchedEvent ->
                    event = fetchedEvent
                    isLoading = false
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        } catch (e: Exception) {
            Log.e("EventUpdateScreen", "Error fetching event data", e)
            errorMessage = e.message
            isLoading = false
        }
    }

    // Show a smaller loading spinner while the event is being fetched
    if (isLoading) {
//        CircularProgressIndicator(
//            modifier = Modifier
//                .size(48.dp)  // Smaller size for the progress indicator
//                .align(Alignment.Center)
//                .fillMaxSize(),
//        color = Color.Blue
//        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Centers the content inside the Box
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp), // Smaller size for the progress indicator
                color = Color.Blue
            )
        }
    } else {
        event?.let {
            EventUpdateContent(
                event = it,
                selectedImageBitmap = selectedImageBitmap,
                onImageSelected = { newImageBitmap ->
                    selectedImageBitmap = newImageBitmap
                },
                onUpdate = { updatedEvent ->
                    eventViewModel.updateEvent(updatedEvent, selectedImageBitmap, onSuccess = {
                        navController.popBackStack() // Navigate back after updating
                    }, onError = { error ->
                        // Handle error (show error message, etc.)
                        errorMessage = error
                    })
                },
                onCancel = {
                    navController.popBackStack() // Navigate back without changes
                }
            )
        } ?: run {
            // If event is null (maybe failed to fetch), display error message
            errorMessage?.let {
                Text(text = "Error: $it", color = Color.Red)
            }
        }
    }
}


//@Composable
//fun EventUpdateContent(
//    event: Event,
//    onUpdate: (Event) -> Unit,
//    onCancel: () -> Unit,
//    selectedImageBitmap: Bitmap?,
//    onImageSelected: (Bitmap) -> Unit
//) {
//    // State for the updated event details
//    var updatedEvent by remember { mutableStateOf(event) }
//    var selectedDate by remember { mutableStateOf(event.date) }
//    var selectedTime by remember { mutableStateOf(event.time) }
//
//    // Color theme
//    val primaryBlue = Color(0xFF0D6EFD)
//    val backgroundColor = Color(0xFFF8F9FA)
//
//    // Time picker state
//    val timePickerDialogState = remember { mutableStateOf(false) }
//
//    // Date picker state
//    val datePickerDialogState = remember { mutableStateOf(false) }
//
//    val context = LocalContext.current
//    val calendar = Calendar.getInstance()
//    val currentYear = calendar.get(Calendar.YEAR)
//    val currentMonth = calendar.get(Calendar.MONTH)
//    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
//    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
//    val currentMinute = calendar.get(Calendar.MINUTE)
//
//    // Main UI
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(backgroundColor)
//            .padding(16.dp)
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//            item {
//                // Title
//                Text(
//                    text = "Update Event",
//                    style = MaterialTheme.typography.h4.copy(
//                        color = primaryBlue,
//                        fontWeight = FontWeight.Bold
//                    ),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 24.dp),
//                    textAlign = TextAlign.Center
//                )
//
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Name
//                OutlinedTextField(
//                    value = updatedEvent.name,
//                    onValueChange = { updatedEvent = updatedEvent.copy(name = it) },
//                    label = { Text("Event Name") },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = primaryBlue,
//                        cursorColor = primaryBlue
//                    )
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Description
//                OutlinedTextField(
//                    value = updatedEvent.description,
//                    onValueChange = { updatedEvent = updatedEvent.copy(description = it) },
//                    label = { Text("Event Description") },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = primaryBlue,
//                        cursorColor = primaryBlue
//                    ),
//                    maxLines = 3
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Date
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { datePickerDialogState.value = true }
//                        .padding(8.dp)
//                        .background(
//                            Color.White,
//                            shape = RoundedCornerShape(8.dp)
//                        )
//                        .padding(horizontal = 16.dp, vertical = 12.dp)
//                ) {
//                    Text(
//                        text = "Event Date: $selectedDate",
//                        color = Color.Gray,
//                        modifier = Modifier.weight(1f)
//                    )
//                    Icon(
//                        imageVector = Icons.Default.CalendarToday,
//                        contentDescription = "Pick Date",
//                        tint = primaryBlue
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Time
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { timePickerDialogState.value = true }
//                        .padding(8.dp)
//                        .background(
//                            Color.White,
//                            shape = RoundedCornerShape(8.dp)
//                        )
//                        .padding(horizontal = 16.dp, vertical = 12.dp)
//                ) {
//                    Text(
//                        text = "Event Time: $selectedTime",
//                        color = Color.Gray,
//                        modifier = Modifier.weight(1f)
//                    )
//                    Icon(
//                        imageVector = Icons.Default.AccessTime,
//                        contentDescription = "Pick Time",
//                        tint = primaryBlue
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Displaying the existing Event Image
//                if (updatedEvent.imageUrl.isNotEmpty()) {
//                    // Assuming the image URL is a valid path or URL string
//                    Image(
//                        painter = rememberImagePainter(updatedEvent.imageUrl),
//                        contentDescription = "Current Event Image",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                            .background(Color.LightGray)
//                            .padding(8.dp),
//                        contentScale = ContentScale.Crop
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                } else {
//                    Text("No current image", style = MaterialTheme.typography.body2, color = Color.Gray)
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Location
//                OutlinedTextField(
//                    value = updatedEvent.location,
//                    onValueChange = { updatedEvent = updatedEvent.copy(location = it) },
//                    label = { Text("Event Location") },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = primaryBlue,
//                        cursorColor = primaryBlue
//                    )
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Event Status (Checkbox with Help Text)
//                Column {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                    ) {
//                        Checkbox(
//                            checked = updatedEvent.status == "Public",
//                            onCheckedChange = { isPublic ->
//                                updatedEvent = updatedEvent.copy(status = if (isPublic) "Public" else "Private")
//                            },
//                            colors = CheckboxDefaults.colors(
//                                checkedColor = primaryBlue
//                            )
//                        )
//                        Text(
//                            text = if (updatedEvent.status == "Public") "Public" else "Private",
//                            style = MaterialTheme.typography.body1,
//                            color = Color.Black
//                        )
//                    }
//
//                    // Help Text
//                    Text(
//                        text = if (updatedEvent.status == "Public") {
//                            "This event will be visible on the public feed."
//                        } else {
//                            "This event will not be suggested on the public feed."
//                        },
//                        style = MaterialTheme.typography.body2,
//                        color = Color.Gray,
//                        modifier = Modifier.padding(start = 48.dp, top = 4.dp) // Indent to align with checkbox
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Image Picker
//                ImagePicker(
//                    selectedImageBitmap = selectedImageBitmap,
//                    onImageSelected = { bitmap ->
//                        onImageSelected(bitmap)
//                        updatedEvent = updatedEvent.copy(imageUrl = bitmap.toString()) // Optionally store image URL
//                    }
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Buttons in the same row (Update and Cancel)
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Button(
//                        onClick = { onUpdate(updatedEvent) },
//                        modifier = Modifier.weight(1f),
//                        colors = ButtonDefaults.buttonColors(backgroundColor = primaryBlue)
//                    ) {
//                        Text("Update Event", color = Color.White)
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    OutlinedButton(
//                        onClick = onCancel,
//                        modifier = Modifier.weight(1f),
//                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryBlue)
//                    ) {
//                        Text("Cancel")
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//            }
//        }
//    }
//}
//
//@Composable
//fun ImagePicker(
//    selectedImageBitmap: Bitmap?,
//    onImageSelected: (Bitmap) -> Unit
//) {
//    // Get the context properly within the composable scope
//    val context = LocalContext.current
//
//    // Image picker launcher
//    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        if (uri != null) {
//            try {
//                Log.d("ImagePicker", "Image URI: $uri")
//                val bitmap = uri.toBitmap(context.contentResolver)
//                bitmap?.let { onImageSelected(it) }
//            } catch (e: Exception) {
//                // Handle any error during conversion
//                Log.e("ImagePicker", "Error converting URI to Bitmap", e)
//            }
//        } else {
//            Log.e("ImagePicker", "Received null URI")
//        }
//    }
//
//    // Display selected image if available
//    if (selectedImageBitmap != null) {
//        Image(
//            bitmap = selectedImageBitmap.asImageBitmap(),
//            contentDescription = "Event Image",
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp)
//                .clip(RoundedCornerShape(12.dp))
//                .background(Color.LightGray)
//                .padding(8.dp),
//            contentScale = ContentScale.Crop
//        )
//    }
//
//    // Button to trigger image picker
//    Button(
//        onClick = { imagePickerLauncher.launch("image/*") },
//        modifier = Modifier.fillMaxWidth(),
//        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF0D6EFD))
//    ) {
//        Text("Pick Event Image", color = Color.White)
//    }
//}
//
//fun Uri.toBitmap(contentResolver: ContentResolver): Bitmap? {
//    return try {
//        MediaStore.Images.Media.getBitmap(contentResolver, this)
//    } catch (e: Exception) {
//        e.printStackTrace() // Log any error for debugging
//        null
//    }
//}
//

@Composable
fun EventUpdateContent(
    event: Event,
    onUpdate: (Event) -> Unit,
    onCancel: () -> Unit,
    selectedImageBitmap: Bitmap?,
    onImageSelected: (Bitmap) -> Unit
) {
    // State for the updated event details
    var updatedEvent by remember { mutableStateOf(event) }
    var selectedDate by remember { mutableStateOf(event.date) }
    var selectedTime by remember { mutableStateOf(event.time) }

    // Color theme
    val primaryBlue = Color(0xFF0D6EFD)
    val backgroundColor = Color(0xFFF8F9FA)

    // Time picker state
    val timePickerDialogState = remember { mutableStateOf(false) }

    // Date picker state
    val datePickerDialogState = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)

    // Main UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                // Title
                Text(
                    text = "Update Event",
                    style = MaterialTheme.typography.h4.copy(
                        color = primaryBlue,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event Name
                OutlinedTextField(
                    value = updatedEvent.name,
                    onValueChange = { updatedEvent = updatedEvent.copy(name = it) },
                    label = { Text("Event Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryBlue,
                        cursorColor = primaryBlue
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event Description
                OutlinedTextField(
                    value = updatedEvent.description,
                    onValueChange = { updatedEvent = updatedEvent.copy(description = it) },
                    label = { Text("Event Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryBlue,
                        cursorColor = primaryBlue
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialogState.value = true }
                        .padding(8.dp)
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Event Date: $selectedDate",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pick Date",
                        tint = primaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Event Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePickerDialogState.value = true }
                        .padding(8.dp)
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Event Time: $selectedTime",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Pick Time",
                        tint = primaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Event Status (Checkbox with Help Text)
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = updatedEvent.status == "Public",
                            onCheckedChange = { isPublic ->
                                updatedEvent = updatedEvent.copy(status = if (isPublic) "Public" else "Private")
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = primaryBlue
                            )
                        )
                        Text(
                            text = if (updatedEvent.status == "Public") "Public" else "Private",
                            style = MaterialTheme.typography.body1,
                            color = Color.Black
                        )
                    }

                    // Help Text
                    Text(
                        text = if (updatedEvent.status == "Public") {
                            "This event will be visible on the public feed."
                        } else {
                            "This event will not be suggested on the public feed."
                        },
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 48.dp, top = 4.dp) // Indent to align with checkbox
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))


                // Event Image (Current or New)
                if (selectedImageBitmap != null) {
                    // Display the newly selected image
                    Image(
                        bitmap = selectedImageBitmap.asImageBitmap(),
                        contentDescription = "New Event Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                } else if (updatedEvent.imageUrl.isNotEmpty()) {
                    // Display the current image if no new image is selected
                    Image(
                        painter = rememberImagePainter(updatedEvent.imageUrl),
                        contentDescription = "Current Event Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder if no image is available
                    Text(
                        text = "No Image Selected",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image Picker Button
                ImagePicker(
                    selectedImageBitmap = selectedImageBitmap,
                    onImageSelected = { bitmap ->
                        onImageSelected(bitmap) // Notify parent composable about the new image
                        updatedEvent = updatedEvent.copy(imageUrl = "") // Optionally clear the current image URL
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons for Update and Cancel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onUpdate(updatedEvent) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = primaryBlue)
                    ) {
                        Text("Update Event", color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryBlue)
                    ) {
                        Text("Cancel")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ImagePicker(
    selectedImageBitmap: Bitmap?,
    onImageSelected: (Bitmap) -> Unit
) {
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = uri.toBitmap(context.contentResolver)
                bitmap?.let { onImageSelected(it) }
            } catch (e: Exception) {
                Log.e("ImagePicker", "Error converting URI to Bitmap", e)
            }
        }
    }

    // Button to pick an image
    Button(
        onClick = { imagePickerLauncher.launch("image/*") },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF0D6EFD))
    ) {
        Text("Pick Event Image", color = Color.White)
    }
}

fun Uri.toBitmap(contentResolver: ContentResolver): Bitmap? {
    return try {
        MediaStore.Images.Media.getBitmap(contentResolver, this)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
