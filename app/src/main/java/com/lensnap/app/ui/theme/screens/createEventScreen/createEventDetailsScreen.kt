package com.lensnap.app.ui.theme.screens.createEventScreen

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDetailsScreen(
    onNext: (String, String, String, Uri?, Bitmap?) -> Unit
) {
    var eventName by rememberSaveable { mutableStateOf("") }
    var eventDescription by rememberSaveable { mutableStateOf("") }
    var eventLocation by rememberSaveable { mutableStateOf("") }
    var eventImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var eventImageBitmap by rememberSaveable { mutableStateOf<Bitmap?>(null) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        eventImageUri = uri
        uri?.let {
            coroutineScope.launch {
                val request = ImageRequest.Builder(context)
                    .data(it)
                    .build()
                val result = ImageLoader(context).execute(request)
                eventImageBitmap = result.drawable?.toBitmap()
                Log.d("CreateEventDetailsScreen", "Selected image URI: $uri")
            }
        }
    }

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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()), // Enable vertical scrolling
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF0d6efd),
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color(0xFF0d6efd),
                        cursorColor = Color(0xFF0d6efd)
                    ),
                    isError = showErrors && eventName.isEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showErrors && eventName.isEmpty()) {
                    Text("Event name cannot be empty", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = eventDescription,
                    onValueChange = { eventDescription = it },
                    label = { Text("Event Description") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF0d6efd),
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color(0xFF0d6efd),
                        cursorColor = Color(0xFF0d6efd)
                    ),
                    isError = showErrors && eventDescription.isEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showErrors && eventDescription.isEmpty()) {
                    Text("Event description cannot be empty", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = eventLocation,
                    onValueChange = { eventLocation = it },
                    label = { Text("Event Location") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF0d6efd),
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color(0xFF0d6efd),
                        cursorColor = Color(0xFF0d6efd)
                    ),
                    isError = showErrors && eventLocation.isEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showErrors && eventLocation.isEmpty()) {
                    Text("Event location cannot be empty", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
//                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0d6efd)
                    ),
                    shape = RoundedCornerShape(8.dp), // Less border radius
                    border = BorderStroke(1.dp, Color(0xFF0d6efd))
                ) {
                    Text("Select Event Image")
                }
                eventImageBitmap?.let { bitmap ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.size(200.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Event Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        showErrors = true
                        if (eventName.isNotEmpty() && eventDescription.isNotEmpty() && eventLocation.isNotEmpty()) {
                            onNext(eventName, eventDescription, eventLocation, eventImageUri, eventImageBitmap)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0d6efd),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp), // Less border radius
                ) {
                    Text("Next")
                }
            }
        }
    )
}
