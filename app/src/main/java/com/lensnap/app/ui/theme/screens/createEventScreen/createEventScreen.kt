//package com.lensnap.app.ui.theme.screens.createEventScreen
//
//import Event
//import android.graphics.Bitmap
//import android.net.Uri
//import android.util.Log
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.unit.dp
//import androidx.core.graphics.drawable.toBitmap
//import androidx.lifecycle.viewmodel.compose.viewModel
//import coil.ImageLoader
//import coil.request.ImageRequest
//import coil.transform.CircleCropTransformation
//import com.lensnap.app.R
//import kotlinx.coroutines.launch
//import androidx.navigation.NavController
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CreateEventScreen(
//    navController: NavController,
//    viewModel: EventViewModel = viewModel(),
//    onEventCreated: (String, String, String) -> Unit,
//    onCancel: () -> Unit
//) {
//    var eventName by remember { mutableStateOf("") }
//    var eventDate by remember { mutableStateOf("") }
//    var eventTime by remember { mutableStateOf("") }
//    var eventLocation by remember { mutableStateOf("") }
//    var eventDescription by remember { mutableStateOf("") }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//    var eventImageUri by remember { mutableStateOf<Uri?>(null) }
//    var eventImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    // State for captured images
//    val capturedImages = remember { mutableStateListOf<Bitmap>() }
//
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        eventImageUri = uri
//        uri?.let {
//            coroutineScope.launch {
//                val request = ImageRequest.Builder(context)
//                    .data(it)
//                    .transformations(CircleCropTransformation())
//                    .build()
//                val result = ImageLoader(context).execute(request)
//                eventImageBitmap = result.drawable?.toBitmap()
//                Log.d("CreateEventScreen", "Selected image URI: $uri")
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Create Event") },
//                navigationIcon = {
//                    IconButton(onClick = onCancel) {
//                        Icon(painter = painterResource(id = R.drawable.arrowback), contentDescription = "Back")
//                    }
//                }
//            )
//        },
//        content = { paddingValues ->
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black)
//                    .padding(paddingValues),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                item {
//                    TextField(
//                        value = eventName,
//                        onValueChange = { eventName = it },
//                        label = { Text("Event Name") },
//                        modifier = Modifier.fillMaxWidth(),
//                        textStyle = TextStyle(color = Color.White)
//                    )
//                }
//                item {
//                    TextField(
//                        value = eventDate,
//                        onValueChange = { eventDate = it },
//                        label = { Text("Event Date") },
//                        modifier = Modifier.fillMaxWidth(),
//                        textStyle = TextStyle(color = Color.White)
//                    )
//                }
//                item {
//                    TextField(
//                        value = eventTime,
//                        onValueChange = { eventTime = it },
//                        label = { Text("Event Time") },
//                        modifier = Modifier.fillMaxWidth(),
//                        textStyle = TextStyle(color = Color.White)
//                    )
//                }
//                item {
//                    TextField(
//                        value = eventLocation,
//                        onValueChange = { eventLocation = it },
//                        label = { Text("Event Location") },
//                        modifier = Modifier.fillMaxWidth(),
//                        textStyle = TextStyle(color = Color.White)
//                    )
//                }
//                item {
//                    TextField(
//                        value = eventDescription,
//                        onValueChange = { eventDescription = it },
//                        label = { Text("Event Description") },
//                        modifier = Modifier.fillMaxWidth(),
//                        textStyle = TextStyle(color = Color.White)
//                    )
//                }
//                item {
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//                item {
//                    Button(
//                        onClick = {
//                            imagePickerLauncher.launch("image/*")
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(50.dp)
//                            .padding(8.dp)
//                            .clip(RoundedCornerShape(8.dp)),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color.White,
//                            contentColor = Color(0xFF9C27B0)
//                        )
//                    ) {
//                        Text("Select Event Image")
//                    }
//                }
//                item {
//                    eventImageBitmap?.let { bitmap ->
//                        Image(
//                            bitmap = bitmap.asImageBitmap(),
//                            contentDescription = "Event Image",
//                            modifier = Modifier
//                                .size(200.dp)
//                                .clip(RoundedCornerShape(8.dp))
//                        )
//                    }
//                }
//                item {
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//                item {
//                    errorMessage?.let {
//                        Text(
//                            text = it,
//                            color = Color.Red,
//                            modifier = Modifier.padding(top = 8.dp)
//                        )
//                    }
//                }
//                item {
//                    Button(
//                        onClick = {
//                            Log.d("CreateEventScreen", "Creating event")
//                            val event = Event(
//                                name = eventName,
//                                date = eventDate,
//                                time = eventTime,
//                                location = eventLocation,
//                                description = eventDescription,
//                                imageUrl = eventImageUri?.toString() ?: ""
//                            )
//                            viewModel.createEvent(
//                                event,
//                                capturedImages, // Pass captured images here
//                                onSuccess = { pairingCode, qrCodeUrl ->
//                                    Log.d("CreateEventScreen", "Event created successfully: $eventName, $pairingCode, $qrCodeUrl")
//                                    val encodedEventName = eventName.replace(" ", "%20")
//                                    val encodedQrCodeUrl = Uri.encode(qrCodeUrl)
//                                    val route = "eventSuccess/$encodedEventName/$pairingCode/$encodedQrCodeUrl"
//                                    Log.d("CreateEventScreen", "Navigating to route: $route")
//                                    navController.navigate(route) {
//                                        popUpTo(navController.graph.startDestinationId) {
//                                            saveState = true
//                                        }
//                                        launchSingleTop = true
//                                        restoreState = true
//                                    }
//                                },
//                                onError = { error ->
//                                    Log.e("CreateEventScreen", "Error creating event: $error")
//                                    errorMessage = error
//                                }
//                            )
//                        },
//                        modifier = Modifier.fillMaxWidth(),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color.White,
//                            contentColor = Color(0xFF9C27B0)
//                        )
//                    ) {
//                        Text("Create Event")
//                    }
//                }
//            }
//        }
//    )
//}
