package com.lensnap.app.ui.theme.screens.createEventScreen

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
                title = { Text("Event Details") }
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
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = eventDescription,
                    onValueChange = { eventDescription = it },
                    label = { Text("Event Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = eventLocation,
                    onValueChange = { eventLocation = it },
                    label = { Text("Event Location") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF9C27B0)
                    )
                ) {
                    Text("Select Event Image")
                }
                eventImageBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Event Image",
                        modifier = Modifier.size(200.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    onNext(eventName, eventDescription, eventLocation, eventImageUri, eventImageBitmap)
                }) {
                    Text("Next")
                }
            }
        }
    )
}
